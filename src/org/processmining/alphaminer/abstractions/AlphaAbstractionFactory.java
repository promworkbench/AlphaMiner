package org.processmining.alphaminer.abstractions;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.alphaminer.abstractions.impl.AlphaClassicAbstractionImpl;
import org.processmining.alphaminer.abstractions.impl.AlphaPlusAbstractionImpl;
import org.processmining.alphaminer.abstractions.impl.AlphaPlusPlusAbstractionImpl;
import org.processmining.alphaminer.abstractions.impl.AlphaRobustAbstractionImpl;
import org.processmining.alphaminer.abstractions.impl.AlphaSharpAbstractionImpl;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.factories.ActivityCountAbstractionFactory;
import org.processmining.logabstractions.factories.CausalAbstractionFactory;
import org.processmining.logabstractions.factories.DirectlyFollowsAbstractionFactory;
import org.processmining.logabstractions.factories.LongTermFollowsAbstractionFactory;
import org.processmining.logabstractions.factories.LoopAbstractionFactory;
import org.processmining.logabstractions.factories.StartEndActivityFactory;
import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.CausalSuccessionAbstraction;
import org.processmining.logabstractions.util.XEventClassUtils;

public class AlphaAbstractionFactory {

	public static AlphaClassicAbstraction<XEventClass> createAlphaClassicAbstraction(XLog log,
			XEventClassifier classifier) {
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
		double[][] dfa = new double[classes.size()][classes.size()]; // directly follows
		double[] starts = new double[classes.size()]; // start activity
		double[] ends = new double[classes.size()]; // end activity
		double[] lol = new double[classes.size()]; // length one loop
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				starts[classes.getClassOf(trace.get(0))
						.getIndex()] += StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				ends[classes.getClassOf(trace.get(trace.size() - 1))
						.getIndex()] += StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				for (int i = 0; i < trace.size() - 1; i++) {
					XEventClass from = classes.getClassOf(trace.get(i));
					XEventClass to = classes.getClassOf(trace.get(i + 1));
					dfa[from.getIndex()][to.getIndex()] = DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					if (from.equals(to)) {
						lol[from.getIndex()] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					}
				}
			}
		}
		XEventClass[] arr = XEventClassUtils.toArray(classes);
		return new AlphaClassicAbstractionImpl<>(arr,
				DirectlyFollowsAbstractionFactory.constructDirectlyFollowsAbstraction(arr, dfa,
						DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructStartActivityAbstraction(arr, starts,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructEndActivityAbstraction(arr, ends,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				LoopAbstractionFactory.constructLengthOneLoopAbstraction(arr, lol,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN));
	}

	public static AlphaPlusAbstraction<XEventClass> createAlphaPlusAbstraction(XLog log, XEventClassifier classifier) {
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
		AlphaClassicAbstraction<XEventClass> aca = createAlphaClassicAbstraction(log, classifier);
		Pair<XEventClass[], int[]> reducedClasses = XEventClassUtils
				.stripLengthOneLoops(XEventClassUtils.toArray(classes), aca.getLengthOneLoopAbstraction());
		double[][] ltl = new double[classes.getClasses().size()][classes.getClasses().size()];
		double[][] dfaLf = new double[reducedClasses.getFirst().length][reducedClasses.getFirst().length];
		double[][] ltlLf = new double[reducedClasses.getFirst().length][reducedClasses.getFirst().length];
		double[] startsLf = new double[reducedClasses.getFirst().length];
		double[] endsLf = new double[reducedClasses.getFirst().length];
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				XEventClass first, second, third, firstLf, secondLf, thirdLf, current;
				first = second = third = firstLf = secondLf = thirdLf = current = null;
				for (int i = 0; i < trace.size(); i++) {
					current = classes.getClassOf(trace.get(i));
					if (i == 0) {
						first = current;
					} else if (i == 1) {
						second = current;
					} else if (i == 2) {
						third = current;
					} else {
						// shift
						first = second;
						second = third;
						third = current;
					}
					if (first != null && second != null && third != null && first.equals(third)) {
						ltl[first.getIndex()][second.getIndex()] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					}
					if (aca.getLengthOneLoopAbstraction().holds(current.getIndex()))
						continue;
					if (firstLf == null) {
						firstLf = current;
						startsLf[reducedClasses.getSecond()[firstLf
								.getIndex()]] = StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
						continue;
					}
					if (secondLf == null) {
						secondLf = current;
						dfaLf[reducedClasses.getSecond()[firstLf.getIndex()]][reducedClasses.getSecond()[secondLf
								.getIndex()]] = DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
						continue;
					}
					if (thirdLf != null) {
						firstLf = secondLf;
						secondLf = thirdLf;
					}
					thirdLf = current;
					dfaLf[reducedClasses.getSecond()[secondLf.getIndex()]][reducedClasses.getSecond()[thirdLf
							.getIndex()]] = DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					if (firstLf.equals(thirdLf)) {
						ltlLf[reducedClasses.getSecond()[firstLf.getIndex()]][reducedClasses.getSecond()[secondLf
								.getIndex()]] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					}
				}
				if (thirdLf != null) {
					endsLf[reducedClasses.getSecond()[thirdLf
							.getIndex()]] = StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				} else if (secondLf != null) {
					endsLf[reducedClasses.getSecond()[secondLf
							.getIndex()]] = StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				} else if (firstLf != null) {
					endsLf[reducedClasses.getSecond()[firstLf
							.getIndex()]] = StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				}
			}
		}
		XEventClass[] arr = XEventClassUtils.toArray(classes);
		return new AlphaPlusAbstractionImpl<>(aca,
				LoopAbstractionFactory.constructLengthTwoLoopAbstraction(arr, ltl,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				reducedClasses,
				DirectlyFollowsAbstractionFactory.constructDirectlyFollowsAbstraction(reducedClasses.getFirst(), dfaLf,
						DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructStartActivityAbstraction(reducedClasses.getFirst(), startsLf,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructEndActivityAbstraction(reducedClasses.getFirst(), endsLf,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				LoopAbstractionFactory.constructLengthTwoLoopAbstraction(reducedClasses.getFirst(), ltlLf,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN));

	}

	public static AlphaPlusPlusAbstraction<XEventClass> createAlphaPlusPlusAbstraction(XLog log,
			XEventClassifier classifier) {
		AlphaPlusAbstraction<XEventClass> apa = createAlphaPlusAbstraction(log, classifier);
		CausalPrecedenceAbstraction<XEventClass> cpa = CausalAbstractionFactory
				.constructAlphaPlusPlusCausalPrecedenceAbstraction(apa.getLengthOneLoopFreeCausalRelationAbstraction(),
						apa.getLengthOneLoopFreeUnrelatedAbstraction());
		CausalSuccessionAbstraction<XEventClass> csa = CausalAbstractionFactory
				.constructAlphaPlusPlusCausalSuccessionAbstraction(apa.getLengthOneLoopFreeCausalRelationAbstraction(),
						apa.getLengthOneLoopFreeUnrelatedAbstraction());

		return new AlphaPlusPlusAbstractionImpl<XEventClass>(apa, cpa, csa,
				LongTermFollowsAbstractionFactory.constructAlphaPlusPlusLengthOneLoopFreeLongTermFollowsAbstraction(log,
						XEventClasses.deriveEventClasses(classifier, log),
						apa.getLengthOneLoopFreeDirectlyFollowsAbstraction(), cpa, csa,
						apa.getLengthOneLoopAbstraction()));
	}

	public static AlphaSharpAbstraction<XEventClass> createAlphaSharpAbstraction(XLog log,
			XEventClassifier classifier) {
		return new AlphaSharpAbstractionImpl<>(createAlphaPlusAbstraction(log, classifier));
	}
	
	// NEW FOR ROBUST
	public static AlphaRobustAbstraction<XEventClass> createAlphaRobustAbstraction(XLog log,
			XEventClassifier classifier, AlphaRobustMinerParameters parameters) {
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
		double[][] dfa = new double[classes.size()][classes.size()]; // directly follows (count)
		double[] starts = new double[classes.size()]; // start activity
		double[] ends = new double[classes.size()]; // end activity
		double[] lol = new double[classes.size()]; // length one loop
		double[] ac = new double[classes.size()]; // activity count
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				starts[classes.getClassOf(trace.get(0))
						.getIndex()] += StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				ends[classes.getClassOf(trace.get(trace.size() - 1))
						.getIndex()] += StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				for (int i = 0; i < trace.size() - 1; i++) {
					XEventClass from = classes.getClassOf(trace.get(i));
					XEventClass to = classes.getClassOf(trace.get(i + 1));
					dfa[from.getIndex()][to.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					if (from.equals(to)) {
						lol[from.getIndex()] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					}
					ac[from.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					if (i == trace.size() - 2) { // count final activity as well
						ac[to.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					}
				}
			}
		}
		XEventClass[] arr = XEventClassUtils.toArray(classes);
		return new AlphaRobustAbstractionImpl<>(arr,
				DirectlyFollowsAbstractionFactory.constructDirectlyFollowsAbstraction(arr, dfa,
						DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructStartActivityAbstraction(arr, starts,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructEndActivityAbstraction(arr, ends,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				LoopAbstractionFactory.constructLengthOneLoopAbstraction(arr, lol,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				ActivityCountAbstractionFactory.constructActivityCountAbstraction(arr, ac,
						ActivityCountAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				parameters.getCausalThreshold(),
				parameters.getNoiseThresholdLeastFreq(),
				parameters.getNoiseThresholdMostFreq());
	}
	// END NEW

}
