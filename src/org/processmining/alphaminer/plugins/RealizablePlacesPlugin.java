package org.processmining.alphaminer.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.alphaminer.parameters.RealizablePlacesParameters;
import org.processmining.alphaminer.plugins.ui.RealizablePlacesWizardStep;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

@Plugin(
        name = "Realizable Places", 
        parameterLabels = { "Petri net", "Log", "Parameters"},
        returnLabels = { "Petri net" },
        returnTypes = { Petrinet.class },
        userAccessible = true,
        help = "Deletes places with low realizability"
)
public class RealizablePlacesPlugin {
        
        @UITopiaVariant(
                affiliation = "Eindhoven University of Technology", 
                author = "L.M.A. Tonnaer", 
                email = "l.m.a.tonnaer@student.tue.nl"
        )
        @PluginVariant(requiredParameterLabels = { 0 , 1 })
        public static Petrinet realizablePlaces(UIPluginContext context, Petrinet petri, XLog log) {
        	RealizablePlacesWizardStep wizStep = new RealizablePlacesWizardStep(context, log);
        	List<ProMWizardStep<RealizablePlacesParameters>> wizStepList = new ArrayList<>();
        	wizStepList.add(wizStep);
        	ListWizard<RealizablePlacesParameters> listWizard = new ListWizard<>(wizStepList);
        	RealizablePlacesParameters params = ProMWizardDisplay.show(context, listWizard, new RealizablePlacesParameters());
        	if (params != null) {
        		return apply(context, petri, log, params, wizStep.getEventClassifier());
        	} else {
        		context.getFutureResult(0).cancel(true);
        		return null;
        	}
        }
        
        @PluginVariant(requiredParameterLabels = { 0, 1, 2})
        public static Petrinet apply(UIPluginContext context, Petrinet petriOriginal, XLog log,
        		RealizablePlacesParameters parameters, XEventClassifier classifier) {
        	// HV: Use clone of Petri net instead of original Petri net. A plug-in should not change any of its inputs.
        	Petrinet petri = PetrinetFactory.clonePetrinet(petriOriginal);
        	// progress indicator
        	context.getProgress().setMinimum(0);
        	context.getProgress().setMaximum(petri.getPlaces().size());
        	context.getProgress().setCaption("Checking places for realizability");
        	context.getProgress().setIndeterminate(false);
        	// obtain incoming and outgoing transitions for each place
        	HashMap<Place,ArrayList<Transition>> incomingTransitions = new HashMap<Place,ArrayList<Transition>>();
        	HashMap<Place,ArrayList<Transition>> outgoingTransitions = new HashMap<Place,ArrayList<Transition>>();
        	for (Place p : petri.getPlaces()){
        		incomingTransitions.put(p, new ArrayList<Transition>());
        		outgoingTransitions.put(p, new ArrayList<Transition>());
        	}
        	for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : petri.getEdges()){
        		if (e.getSource() instanceof Transition && e.getTarget() instanceof Place){
        			incomingTransitions.get(e.getTarget()).add((Transition)e.getSource());
        		} else if (e.getSource() instanceof Place && e.getTarget() instanceof Transition) {
        			outgoingTransitions.get(e.getSource()).add((Transition)e.getTarget());
        		}
        	}
        	// check realizability per place
        	XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
        	double realThres = parameters.getRealizabilityThreshold();
        	int trueTraceThres = (int)Math.round(parameters.getUnrealizableTracesThreshold()*log.size());
        	ArrayList<Place> placesToRemove = new ArrayList<Place>();
        	for (Place p : petri.getPlaces()){
        		// update progress indicator
        		context.getProgress().inc();
        		// if p=Start or p=End: skip
        		if (p.getLabel().equals("Start") || p.getLabel().equals("End")) {
        			break;
        		}
        		int unrealizable = 0;
        		for (XTrace trace : log) {
        			int incoming = 0;
            		int outgoing = 0;
        			for (XEvent event : trace) {
        				
        				// count incoming and outgoing events for this trace
        				for (Transition inTransition : incomingTransitions.get(p)) {
        					// HV: Use equals instead of ==, as the latter is too strict.
        					if (inTransition.getLabel().equals(classes.getClassOf(event).toString())) { // TODO: make proper comparison
        						incoming++;
        						break;
        					}
        				}
        				for (Transition outTransition : outgoingTransitions.get(p)) {
        					// HV: Use equals instead of ==, as the latter is too strict.
        					if (outTransition.getLabel().equals(classes.getClassOf(event).toString())) { // TODO: make proper comparison
        						outgoing++;
        						break;
        					}
        				}
        			}
        			// TODO: compute proper true threshold
    				int trueRealThres = (int)Math.round(realThres*Math.max(incoming, outgoing));
    				// check if trace is not roughly realizable
    				if (Math.abs(outgoing-incoming) > trueRealThres) {
    					unrealizable++;
    					System.out.println(" Unrealizable trace:");
    					for (XEvent e : trace) {
    						System.out.println("    " + e.getAttributes().get("concept:name").toString());
    					}
    				}
    				// remove place if too many unrealizable traces
    				if (unrealizable > trueTraceThres) {
    					// maintain list to avoid ConcurrentModificationException
    					placesToRemove.add(p);
    					System.out.println("Place " + p.getLabel() + " is not realizable");
    					break;
    				}
        		}
        	}
        	context.log("Removing " + placesToRemove.size() + " unrealizable places.");
        	for (Place p : placesToRemove) {
        		petri.removePlace(p);
        	}
        	return petri;
        }
}