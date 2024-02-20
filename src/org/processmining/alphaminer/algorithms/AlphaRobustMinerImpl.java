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
import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.alphaminer.algorithms.abstr.AbstractAlphaMiner;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
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
 * Implementation of a more robust version the original alpha algorithm. Original code based on: "Workflow
 * Mining: Discovering Process Models from Event Logs"; Wil van der Aalst, Ton
 * Weijters, and Laura Maruster.
 *
 * @author svzelst
 * @author bfvdongen -> node expander concept
 * @author lmatonnaer -> robust version
 *
 */
public class AlphaRobustMinerImpl<E, A extends AlphaRobustAbstraction<E>, P extends AlphaMinerParameters>
		extends AbstractAlphaMiner<E, A, P> {

	public AlphaRobustMinerImpl(P parameters, A abstraction, PluginContext context) {
		super(context, parameters, abstraction);
	}

	protected Stack<AlphaPairImpl<Collection<E>, Collection<E>>> createInitialCausalStack(CausalAbstraction<E> caR,
			UnrelatedAbstraction<E> uaR) {
		Stack<AlphaPairImpl<Collection<E>, Collection<E>>> tuples = new Stack<>();
		for (int row = 0; row < caR.getMatrix().length; row++) {
			for (int col = 0; col < caR.getMatrix()[row].length; col++) {
				// prune for length one loops
				if (!uaR.holds(row, row) || !uaR.holds(col, col))
					continue;
				if (caR.holds(row, col)) {
					Collection<E> left = new HashSet<>();
					left.add(caR.getEventClass(row));
					Collection<E> right = new HashSet<>();
					right.add(caR.getEventClass(col));
					AlphaPairImpl<Collection<E>, Collection<E>> tuple = new AlphaPairImpl<>(left, right, row, col);
					tuples.push(tuple);
				}
			}
		}

		return tuples;
	}

	protected List<AlphaPairImpl<Collection<E>, Collection<E>>> alphaExpansion(CausalAbstraction<E> caR,
			UnrelatedAbstraction<E> uaR, LengthOneLoopAbstraction<E> lola) {
		final Stack<AlphaPairImpl<Collection<E>, Collection<E>>> tuples = createInitialCausalStack(caR, uaR);
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> result = new ArrayList<>();
		AlphaClassicNodeExpanderImpl<E> expander = new AlphaClassicNodeExpanderImpl<>(caR, uaR,
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
	 * This method includes the final place in a final marking object embedded
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
				getAbstraction().getRobustCausalAbstraction(), getAbstraction().getRobustUnrelatedAbstraction(),
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
		AlphaRobustAbstraction<XEventClass> abstraction = AlphaAbstractionFactory.createAlphaRobustAbstraction(log,
				classifier, (AlphaRobustMinerParameters) params);
		AlphaMiner<XEventClass, AlphaRobustAbstraction<XEventClass>, AlphaMinerParameters> miner = new AlphaRobustMinerImpl<>(
				params, abstraction, context);
		return miner.run();
	}
}
