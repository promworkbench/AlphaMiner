package org.processmining.alphaminer.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.alphaminer.abstractions.AlphaPlusAbstraction;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.parameters.AlphaPlusMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Implementation of the alpha+ algorithm. Code based on:
 * "Process Mining for Ubiquitous Mobile Systems: An Overview and a Concrete Algorithm"
 * A.K.A. de Medeiros, B.F van Dongen, W.M.P van der Aalst, and A.J.M.M.
 * Weijters
 *
 * @author svzelst
 * @author bfvdongen -> node expander concept
 *
 */
public class AlphaPlusMinerImpl<E, A extends AlphaPlusAbstraction<E>, P extends AlphaPlusMinerParameters>
		extends AlphaClassicMinerImpl<E, A, P> {

	public AlphaPlusMinerImpl(P parameters, A abstraction, PluginContext context) {
		super(parameters, abstraction, context);
	}

	protected void connectLenghtOneLoops(Petrinet net, LengthOneLoopAbstraction<E> lola,
			DirectlyFollowsAbstraction<E> dfa) {
		for (E t : lola.getAllGEQThreshold()) {
			Set<E> A = new HashSet<>(dfa.getAllGeqForColumn(t));
			A.removeAll(lola.getAllGEQThreshold());
			Set<E> B = new HashSet<>(dfa.getAllGeqForRow(t));
			B.removeAll(lola.getAllGEQThreshold());
			Set<E> AminB = new HashSet<>(A);
			AminB.removeAll(B);
			Set<E> BminA = new HashSet<>(B);
			BminA.removeAll(A);
			for (AlphaPairImpl<Collection<E>, Collection<E>> place : getAlphaPairToPlaceMapping().keySet()) {
				if (place.getFirst().containsAll(AminB) && place.getSecond().containsAll(BminA)) {
					net.addArc(getAlphaPairToPlaceMapping().get(place), getEventClassToTransitionMapping().get(t));
					net.addArc(getEventClassToTransitionMapping().get(t), getAlphaPairToPlaceMapping().get(place));
				}
			}
		}
	}

	public Pair<Petrinet, Marking> run() {
		getProgress().setMinimum(0);
		getProgress().setMaximum(5);
		getProgress().setIndeterminate(false);
		getProgress().inc();
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> result = alphaExpansion(
				getAbstraction().getLengthOneLoopFreeCausalRelationAbstraction(),
				getAbstraction().getLengthOneLoopFreeUnrelatedAbstraction(),
				getAbstraction().getLengthOneLoopAbstraction());
		Petrinet net = PetrinetFactory.newPetrinet("Petri net (Alpha+)");
		Marking marking = new Marking();
		addTransitions(net);
		getProgress().inc();
		addPlaces(net, result);
		getProgress().inc();
		addInitialPlace(net, getAbstraction().getStartActivityAbstraction(), marking);
		addFinalPlace(net, getAbstraction().getEndActivityAbstraction());
		getProgress().inc();
		if (!getParameters().isIgnoreLengthOneLoops()) {
			connectLenghtOneLoops(net, getAbstraction().getLengthOneLoopAbstraction(),
					getAbstraction().getDirectlyFollowsAbstraction());
		}
		getProgress().inc();
		return new Pair<Petrinet, Marking>(net, marking);
	}
}
