package org.processmining.alphaminer.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.alphaminer.abstractions.AlphaAbstractionFactory;
import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.algorithms.abstr.AbstractAlphaMiner;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.search.MultiThreadedSearcher;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Implementation of the original alpha algorithm. Code based on: "Workflow
 * Mining: Discovering Process Models from Event Logs"; Wil van der Aalst, Ton
 * Weijters, and Laura Maruster
 *
 * @author svzelst
 * @author bfvdongen -> node expander concept
 *
 */
public class AlphaClassicMinerImpl<E, A extends AlphaClassicAbstraction<E>, P extends AlphaMinerParameters>
		extends AbstractAlphaMiner<E, A, P> {

	public AlphaClassicMinerImpl(P parameters, A abstraction, PluginContext context) {
		super(context, parameters, abstraction);
	}

	protected Stack<AlphaPairImpl<Collection<E>, Collection<E>>> createInitialCausalStack(CausalAbstraction<E> cra,
			UnrelatedAbstraction<E> ua) {
		Stack<AlphaPairImpl<Collection<E>, Collection<E>>> tuples = new Stack<>();
		for (int row = 0; row < cra.getMatrix().length; row++) {
			for (int col = 0; col < cra.getMatrix()[row].length; col++) {
				// prune for length one loops
				if (!ua.holds(row, row) || !ua.holds(col, col))
					continue;
				if (cra.holds(row, col)) {
					Collection<E> left = new HashSet<>();
					left.add(cra.getEventClass(row));
					Collection<E> right = new HashSet<>();
					right.add(cra.getEventClass(col));
					AlphaPairImpl<Collection<E>, Collection<E>> tuple = new AlphaPairImpl<>(left, right, row, col);
					tuples.push(tuple);
				}
			}
		}

		return tuples;
	}

	protected List<AlphaPairImpl<Collection<E>, Collection<E>>> alphaExpansion(CausalAbstraction<E> cra,
			UnrelatedAbstraction<E> ua, LengthOneLoopAbstraction<E> lola) {
		final Stack<AlphaPairImpl<Collection<E>, Collection<E>>> tuples = createInitialCausalStack(cra, ua);
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> result = new ArrayList<>();
		AlphaClassicNodeExpanderImpl<E> expander = new AlphaClassicNodeExpanderImpl<>(cra, ua,
				lola.getAllGEQThreshold());
		MultiThreadedSearcher<AlphaPairImpl<Collection<E>, Collection<E>>> searcher = new MultiThreadedSearcher<>(
				expander, MultiThreadedSearcher.BREADTHFIRST);
		searcher.addInitialNodes(tuples);
		try {
			searcher.startSearch(getExecutor(), getProgress(), result);
			return result;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Pair<Petrinet, Marking> run() {
		AcceptingPetriNet apn = runAccPN();
		return new Pair<Petrinet, Marking>(apn.getNet(), apn.getInitialMarking());
	}

	/**
	 * This method includes the final place in a final marking object empedded
	 * in the accepting petri net
	 * 
	 * @return
	 */
	public AcceptingPetriNet runAccPN() {
		getProgress().setMinimum(0);
		getProgress().setMaximum(4);
		getProgress().setIndeterminate(false);
		getProgress().inc();
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> result = alphaExpansion(
				getAbstraction().getCausalAbstraction(), getAbstraction().getUnrelatedAbstraction(),
				getAbstraction().getLengthOneLoopAbstraction());
		Petrinet net = PetrinetFactory.newPetrinet("Petri net (Alpha)");
		Marking iMarking = new Marking();
		Marking fMarking = new Marking();
		addTransitions(net);
		getProgress().inc();
		addPlaces(net, result);
		getProgress().inc();
		addInitialPlace(net, getAbstraction().getStartActivityAbstraction(), iMarking);
		addFinalPlace(net, getAbstraction().getEndActivityAbstraction(), fMarking);
		getProgress().inc();
		return AcceptingPetriNetFactory.createAcceptingPetriNet(net, iMarking, fMarking);
	}

	public static Pair<Petrinet, Marking> run(XLog log, XEventClassifier classifier, AlphaMinerParameters params) {
		return run(null, log, classifier, params);
	}

	public static Pair<Petrinet, Marking> run(PluginContext context, XLog log, XEventClassifier classifier,
			AlphaMinerParameters params) {
		AlphaClassicAbstraction<XEventClass> abstraction = AlphaAbstractionFactory.createAlphaClassicAbstraction(log,
				classifier);
		AlphaMiner<XEventClass, AlphaClassicAbstraction<XEventClass>, AlphaMinerParameters> miner = new AlphaClassicMinerImpl<>(
				params, abstraction, context);
		return miner.run();
	}
}
