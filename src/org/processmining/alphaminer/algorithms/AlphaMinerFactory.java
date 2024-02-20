package org.processmining.alphaminer.algorithms;

import java.util.Random;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.alphaminer.abstractions.AlphaAbstractionFactory;
import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.abstractions.AlphaPlusAbstraction;
import org.processmining.alphaminer.abstractions.AlphaPlusPlusAbstraction;
import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.alphaminer.abstractions.AlphaSharpAbstraction;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaPlusMinerParameters;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.log.logfilters.LogFilterException;
import org.processmining.plugins.log.logfilters.impl.AddArtificialEndFilter;
import org.processmining.plugins.log.logfilters.impl.AddArtificialStartFilter;

public class AlphaMinerFactory {
	public static <P extends AlphaMinerParameters> AlphaMiner<XEventClass, ? extends AlphaClassicAbstraction<XEventClass>, ? extends AlphaMinerParameters> createAlphaMiner(
			XLog log, XEventClassifier classifier, P parameters) {
		return createAlphaMiner(null, log, classifier, parameters);
	}

	public static <P extends AlphaMinerParameters> AlphaMiner<XEventClass, ? extends AlphaClassicAbstraction<XEventClass>, ? extends AlphaMinerParameters> createAlphaMiner(
			PluginContext context, XLog log, XEventClassifier classifier, P parameters) {
		switch (parameters.getVersion()) {
			case PLUS_PLUS :
				return createAlphaPlusPlusMiner(context, new AlphaPlusMinerParameters(parameters.getVersion()),
						AlphaAbstractionFactory.createAlphaPlusPlusAbstraction(log, classifier));
			case PLUS :
				return createAlphaPlusMiner(context, new AlphaPlusMinerParameters(parameters.getVersion()),
						AlphaAbstractionFactory.createAlphaPlusAbstraction(log, classifier));
			case SHARP :
				Pair<XLog, Pair<XEventClass, XEventClass>> startEndLog = addArtificialStartEndToLog(context, log,
						classifier);
				return createAlphaSharpMiner(context, parameters,
						AlphaAbstractionFactory.createAlphaSharpAbstraction(startEndLog.getFirst(), classifier),
						startEndLog.getSecond().getFirst(), startEndLog.getSecond().getSecond());
			case ROBUST :
				return createAlphaRobustMiner(context, parameters, 
						AlphaAbstractionFactory.createAlphaRobustAbstraction(log, classifier,
								(AlphaRobustMinerParameters) parameters));
			case CLASSIC :
			default :
				AlphaClassicAbstraction<XEventClass> abstrClassic = AlphaAbstractionFactory
						.createAlphaClassicAbstraction(log, classifier);
				return createAlphaClassicMiner(context, parameters, abstrClassic);
		}
	}

	private static Pair<XLog, Pair<XEventClass, XEventClass>> addArtificialStartEndToLog(PluginContext context,
			XLog log, XEventClassifier classifier) {
		String startClean = AlphaSharpMinerImpl.UNIQUE_ARTIFICIAL_START_IDENTIFIER;
		String startTarget = startClean;
		String startAppendix = "";
		String endClean = AlphaSharpMinerImpl.UNIQUE_ARTIFICIAL_END_IDENTIFIER;
		String endTarget = endClean;
		String endAppendix = "";
		for (int i = 1; i < classifier.getDefiningAttributeKeys().length; i++) {
			startTarget += "+";
			endTarget += "+";
		}
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
		while (classes.getByIdentity(startTarget) != null) {
			Random r = new Random();
			int a = r.nextInt();
			startTarget += a;
			startAppendix += a;
		}
		while (classes.getByIdentity(endTarget) != null) {
			Random r = new Random();
			int a = r.nextInt();
			endTarget += a;
			endAppendix += a;
		}
		Pair<XEvent, XEvent> startEndEvent = createStartEndEvents(classifier, startClean, endClean, startAppendix,
				endAppendix);
		AddArtificialStartFilter startFilter = new AddArtificialStartFilter();
		AddArtificialEndFilter endFilter = new AddArtificialEndFilter();
		try {
			XLog started = startFilter.filter(context, log, startEndEvent.getFirst());
			XLog ended = endFilter.filter(context, started, startEndEvent.getSecond());
			return new Pair<>(ended, getArtificialStartAndEnd(ended, classifier, startTarget, endTarget));
		} catch (LogFilterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Pair<>(log, new Pair<XEventClass, XEventClass>(null, null));
	}

	private static Pair<XEventClass, XEventClass> getArtificialStartAndEnd(XLog log, XEventClassifier classifier,
			String start, String end) {
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
		XEventClass startClass, endClass;
		startClass = null;
		endClass = null;
		for (XEventClass clazz : classes.getClasses()) {
			if (clazz.toString().equals(start)) {
				startClass = clazz;
			}
			if (clazz.toString().equals(end)) {
				endClass = clazz;
			}
		}
		return new Pair<>(startClass, endClass);
	}

	private static Pair<XEvent, XEvent> createStartEndEvents(XEventClassifier classifier, String startClean,
			String endClean, String startAppendix, String endAppendix) {
		XEvent startEvent = XFactoryRegistry.instance().currentDefault().createEvent();
		XEvent endEvent = XFactoryRegistry.instance().currentDefault().createEvent();
		for (int i = 0; i < classifier.getDefiningAttributeKeys().length; i++) {
			String valStart = "";
			String valEnd = "";
			if (i == 0) {
				valStart += startClean;
				valEnd += endClean;
			}
			if (i == classifier.getDefiningAttributeKeys().length - 1) {
				valStart += startAppendix;
				valEnd += endAppendix;
			}
			String key = classifier.getDefiningAttributeKeys()[i];
			startEvent.getAttributes().put(key, new XAttributeLiteralImpl(key, valStart));
			endEvent.getAttributes().put(key, new XAttributeLiteralImpl(key, valEnd));
		}
		return new Pair<>(startEvent, endEvent);
	}

	public static <E, A extends AlphaClassicAbstraction<E>, P extends AlphaMinerParameters> AlphaMiner<E, A, P> createAlphaClassicMiner(
			PluginContext context, P parameters, A abstraction) {
		return new AlphaClassicMinerImpl<>(parameters, abstraction, context);
	}

	public static <E, A extends AlphaPlusAbstraction<E>, P extends AlphaPlusMinerParameters> AlphaMiner<E, A, P> createAlphaPlusMiner(
			PluginContext context, P parameters, A abstraction) {
		return new AlphaPlusMinerImpl<>(parameters, abstraction, context);
	}

	public static <E, A extends AlphaPlusPlusAbstraction<E>, P extends AlphaPlusMinerParameters> AlphaMiner<E, A, P> createAlphaPlusPlusMiner(
			PluginContext context, P parameters, A abstraction) {
		return new AlphaPlusPlusMinerImproved1Impl<E, A, P>(parameters, abstraction, context);
	}

	public static <E, A extends AlphaSharpAbstraction<E>, P extends AlphaMinerParameters> AlphaMiner<E, A, P> createAlphaSharpMiner(
			PluginContext context, P parameters, A abstraction, E artificialStart, E artificialEnd) {
		return new AlphaSharpMinerImpl<E, A, P>(context, parameters, abstraction, artificialStart, artificialEnd);
	}
	
	public static <E, A extends AlphaRobustAbstraction<E>, P extends AlphaMinerParameters> AlphaMiner<E, A, P> createAlphaRobustMiner(
			PluginContext context, P parameters, A abstraction) {
		return new AlphaRobustMinerImpl<>(parameters, abstraction, context);
	}
}
