package org.processmining.alphaminer.plugins;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.algorithms.AlphaMiner;
import org.processmining.alphaminer.algorithms.AlphaMinerFactory;
import org.processmining.alphaminer.help.AlphaMinerHelp;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.alphaminer.plugins.ui.AlphaMinerWizardStep;
import org.processmining.alphaminer.plugins.ui.AlphaRobustMinerWizardStep;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

@Plugin(name = "Alpha Miner", level = PluginLevel.PeerReviewed, parameterLabels = { "Log", "Classifier",
		"Parameters" }, returnLabels = { "Petrinet", "Marking" }, returnTypes = { Petrinet.class,
				Marking.class }, help = AlphaMinerHelp.TEXT, quality = PluginQuality.VeryGood, categories = {
						PluginCategory.Discovery })
public class AlphaMinerPlugin {

	@PluginVariant(requiredParameterLabels = { 0, 1, 2 })
	public static Object[] apply(PluginContext context, XLog log, XEventClassifier classifier,
			AlphaMinerParameters parameters) {
		AlphaMiner<XEventClass, ? extends AlphaClassicAbstraction<XEventClass>, ? extends AlphaMinerParameters> miner = AlphaMinerFactory
				.createAlphaMiner(context, log, classifier, parameters);
		Pair<Petrinet, Marking> markedNet = miner.run();
		if (context.getProgress().isCancelled()) {
			context.getFutureResult(0).cancel(true);
			return new Object[] { null, null };
		}
		context.getConnectionManager()
				.addConnection(new InitialMarkingConnection(markedNet.getFirst(), markedNet.getSecond()));
		return new Object[] { markedNet.getFirst(), markedNet.getSecond() };
	}

	public static Object[] apply(PluginContext context, XLog log, XEventClassifier classifier, AlphaVersion version) {
		return apply(context, log, classifier, new AlphaMinerParameters(version));
	}

	@UITopiaVariant(affiliation = "Eindhoven University of Technology", author = "S.J. van Zelst, B.F. van Dongen, L.M.A. Tonnaer", email = "s.j.v.zelst@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0 })
	public static Object[] apply(UIPluginContext context, XLog log) {
		AlphaMinerWizardStep wizStep = new AlphaMinerWizardStep(log);
		List<ProMWizardStep<AlphaMinerParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		ListWizard<AlphaMinerParameters> listWizard = new ListWizard<>(wizStepList);
		AlphaMinerParameters params = ProMWizardDisplay.show(context, listWizard, new AlphaMinerParameters());
		if (params != null) {
			if (wizStep.getVersion() != AlphaVersion.ROBUST){
				return apply(context, log, wizStep.getEventClassifier(), params);
			} else {
				// TODO: generate Directly Follows relations
				// TODO: visualise dfr result
				AlphaRobustMinerWizardStep wizStepR = new AlphaRobustMinerWizardStep(log, wizStep.getEventClassifier());
				List<ProMWizardStep<AlphaRobustMinerParameters>> wizStepListR = new ArrayList<>();
				wizStepListR.add(wizStepR);
				ListWizard<AlphaRobustMinerParameters> listWizardR = new ListWizard<>(wizStepListR);
				AlphaRobustMinerParameters paramsR = ProMWizardDisplay.show(context, listWizardR, new AlphaRobustMinerParameters(params.getVersion()));
				return apply(context, log, wizStep.getEventClassifier(), paramsR);
			}
		} else {
			context.getFutureResult(0).cancel(true);
			return new Object[] { null, null };
		}
	}

	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public static Object[] applyAlphaClassic(PluginContext context, XLog log, XEventClassifier classifier) {
		return apply(context, log, classifier, AlphaVersion.CLASSIC);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public static Object[] applyAlphaPlus(PluginContext context, XLog log, XEventClassifier classifier) {
		return apply(context, log, classifier, AlphaVersion.PLUS);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public static Object[] applyAlphaPlusPlus(PluginContext context, XLog log, XEventClassifier classifier) {
		return apply(context, log, classifier, AlphaVersion.PLUS_PLUS);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public static Object[] applyAlphaSharp(PluginContext context, XLog log, XEventClassifier classifier) {
		return apply(context, log, classifier, AlphaVersion.SHARP);
	}
	
	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public static Object[] applyAlphaRobust(PluginContext context, XLog log, XEventClassifier classifier) {
		return apply(context, log, classifier, AlphaVersion.ROBUST);
	}
}
