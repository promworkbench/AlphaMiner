package org.processmining.alphaminer.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import org.processmining.alphaminer.abstractions.AlphaSharpAbstraction;
import org.processmining.alphaminer.algorithms.abstr.AbstractAlphaMiner;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.models.AlphaPairOfAlphaPairCollection;
import org.processmining.alphaminer.models.AlphaSharpConITDataModel;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.search.MultiThreadedSearcher;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

public class AlphaSharpMinerImpl<E, A extends AlphaSharpAbstraction<E>, P extends AlphaMinerParameters>
		extends AbstractAlphaMiner<E, A, P> {

	private final Map<Object, Transition> invisibleTransitions = new HashMap<>();
	private final Map<AlphaPairImpl<Collection<Object>, Collection<Object>>, Place> sharpTuplesToPlace = new HashMap<>();

	public static String UNIQUE_ARTIFICIAL_START_IDENTIFIER = "[start>";
	public static String UNIQUE_ARTIFICIAL_END_IDENTIFIER = "[end]";

	private final E artificialStartIdentifier;
	private final E artificialEndIdentifier;

	public AlphaSharpMinerImpl(PluginContext context, P parameters, A abstraction, E artificialStartIdentifier,
			E artificialEndIdentifier) {
		super(context, parameters, abstraction);
		this.artificialStartIdentifier = artificialStartIdentifier;
		this.artificialEndIdentifier = artificialEndIdentifier;
	}

	public Pair<Petrinet, Marking> run() {
		AlphaSharpConItAlgorithmImpl<E> conIT = new AlphaSharpConItAlgorithmImpl<>(getContext(), getAbstraction());
		AlphaSharpConITDataModel<E> conITResult = conIT.run();
		List<AlphaPairImpl<Collection<Object>, Collection<Object>>> places = applyAlphaSharpNodeExpansion(conITResult);

		Petrinet net = PetrinetFactory.newPetrinet("Petri net (Alpha #)");
		Marking marking = new Marking();

		addTransitions(net);
		net = addInvisibleTransition(net, conITResult);
		net = addPlaces(net, places);
		addInitialPlace(net, marking);
		addFinalPlace(net);
		return new Pair<Petrinet, Marking>(net, marking);
	}

	private void addInitialPlace(Petrinet net, Marking marking) {
		Transition artificialStart = invisibleTransitions.get(artificialStartIdentifier);
		boolean removeArtificialStart = true;
		if (net.getOutEdges(artificialStart).size() <= 1) {
			for (Place p : net.getPlaces()) {
				if (net.getArc(artificialStart, p) != null) {
					if (net.getInEdges(p).size() > 1) {
						removeArtificialStart = false;
						break; // can only be one!
					}
				}
			}
		} else {
			removeArtificialStart = false;
		}
		Place initialPlace = null;
		if (removeArtificialStart) {
			for (Place p : net.getPlaces()) {
				if (net.getArc(artificialStart, p) != null) {
					initialPlace = p;
					break; // can only be one!
				}
			}
			net.removeTransition(artificialStart);
		} else {
			initialPlace = net.addPlace("source");
			net.addArc(initialPlace, artificialStart);
		}
		marking.add(initialPlace);
	}

	private void addFinalPlace(Petrinet net) {
		Transition artificialEnd = invisibleTransitions.get(artificialEndIdentifier);
		boolean removeArtificialEnd = true;
		if (net.getInEdges(artificialEnd).size() <= 1) {
			for (Place p : net.getPlaces()) {
				if (net.getArc(p, artificialEnd) != null) {
					if (net.getOutEdges(p).size() > 1) {
						removeArtificialEnd = false;
						break; // can only be one
					}
				}
			}
		} else {
			removeArtificialEnd = false;
		}
		Place finalPlace = null;
		if (removeArtificialEnd) {
			for (Place p : net.getPlaces()) {
				if (net.getArc(p, artificialEnd) != null) {
					finalPlace = p;
					break; // can only be one
				}
			}
			net.removeTransition(artificialEnd);
		} else {
			finalPlace = net.addPlace("sink");
			net.addArc(artificialEnd, finalPlace);
		}
	}

	private Petrinet addInvisibleTransition(Petrinet net, AlphaSharpConITDataModel<E> conITResult) {
		for (AlphaPairOfAlphaPairCollection<E> invTr : conITResult.getInvisibleTransitions()) {
			Transition t = net.addTransition("inv_" + invisibleTransitions.size());
			t.setInvisible(true);
			invisibleTransitions.put(invTr, t);
		}
		return net;
	}

	@Override
	protected void addTransitions(Petrinet net, Collection<E> ignore) {
		for (int i = 0; i < getAbstraction().getEventClasses().length; i++) {
			E eventClass = getAbstraction().getEventClass(i);
			if (!ignore.contains(eventClass)) {
				Transition transition = net.addTransition(eventClass.toString());
				if (eventClass.equals(artificialStartIdentifier) || eventClass.equals(artificialEndIdentifier)) {
					transition.setInvisible(true);
					invisibleTransitions.put(eventClass, transition);
				} else {
					getEventClassToTransitionMapping().put(eventClass, transition);
				}
			}
		}
	}

	private Petrinet addPlaces(Petrinet net, List<AlphaPairImpl<Collection<Object>, Collection<Object>>> places) {
		for (AlphaPairImpl<Collection<Object>, Collection<Object>> pair : places) {
			Place p = net.addPlace("p" + sharpTuplesToPlace.size());
			sharpTuplesToPlace.put(pair, p);
			for (Object in : pair.getFirst()) {
				Transition t = getEventClassToTransitionMapping().containsKey(in)
						? getEventClassToTransitionMapping().get(in) : invisibleTransitions.get(in);
				net.addArc(t, p);
			}
			for (Object out : pair.getSecond()) {
				Transition t = getEventClassToTransitionMapping().containsKey(out)
						? getEventClassToTransitionMapping().get(out) : invisibleTransitions.get(out);
				net.addArc(p, t);
			}
		}
		return net;
	}

	private List<AlphaPairImpl<Collection<Object>, Collection<Object>>> applyAlphaSharpNodeExpansion(
			AlphaSharpConITDataModel<E> conITResult) {
		AlphaSharpNodeExpanderImpl<E> expander = new AlphaSharpNodeExpanderImpl<>(
				getAbstraction().getRepairedCausalAbstraction(), getAbstraction().getDirectlyFollowsAbstraction(),
				getAbstraction().getUnrelatedAbstraction(), conITResult);
		Stack<AlphaPairImpl<Collection<Object>, Collection<Object>>> stack = createInitialCausalStack(
				getAbstraction().getRepairedCausalAbstraction(), getAbstraction().getUnrelatedAbstraction(),
				conITResult);
		MultiThreadedSearcher<AlphaPairImpl<Collection<Object>, Collection<Object>>> searcher = new MultiThreadedSearcher<>(
				expander, MultiThreadedSearcher.BREADTHFIRST);
		List<AlphaPairImpl<Collection<Object>, Collection<Object>>> result = new ArrayList<>();
		searcher.addInitialNodes(stack);
		try {
			searcher.startSearch(getExecutor(), getProgress(), result);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private Stack<AlphaPairImpl<Collection<Object>, Collection<Object>>> createInitialCausalStack(
			CausalAbstraction<E> ca, UnrelatedAbstraction<E> ua, AlphaSharpConITDataModel<E> conITResult) {
		Stack<AlphaPairImpl<Collection<Object>, Collection<Object>>> stack = new Stack<>();
		// add all causal pairs		
		for (int row = 0; row < ca.getMatrix().length; row++) {
			for (int col = 0; col < ca.getMatrix()[row].length; col++) {
				if (ca.holds(row, col)) {
					Collection<Object> left = new HashSet<>();
					left.add(ca.getEventClass(row));
					Collection<Object> right = new HashSet<>();
					right.add(ca.getEventClass(col));
					AlphaPairImpl<Collection<Object>, Collection<Object>> tuple = new AlphaPairImpl<>(left, right, row,
							col);
					stack.push(tuple);
				}
			}
		}
		// add all invisible sequence pairs
		for (Pair<Object, Object> seq : conITResult.getSequentialDependencies()) {
			int leftIndex = -1;
			int rightIndex = -1;
			if (!(seq.getFirst() instanceof AlphaPairOfAlphaPairCollection<?>)) {
				leftIndex = ca.getIndex((E) seq.getFirst());
			}
			if (!(seq.getSecond() instanceof AlphaPairOfAlphaPairCollection<?>)) {
				rightIndex = ca.getIndex((E) seq.getSecond());
			}
			Collection<Object> left = new HashSet<>();
			left.add(seq.getFirst());
			Collection<Object> right = new HashSet<>();
			right.add(seq.getSecond());
			AlphaPairImpl<Collection<Object>, Collection<Object>> tuple = new AlphaPairImpl<Collection<Object>, Collection<Object>>(
					left, right, leftIndex, rightIndex);
			stack.push(tuple);
		}

		return stack;
	}
}
