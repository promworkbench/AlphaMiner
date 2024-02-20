package org.processmining.alphaminer.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.processmining.alphaminer.abstractions.AlphaSharpAbstraction;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.models.AlphaPairOfAlphaPairCollection;
import org.processmining.alphaminer.models.AlphaSharpConITDataModel;
import org.processmining.alphaminer.models.FakeProgressImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.search.MultiThreadedSearcher;
import org.processmining.logabstractions.models.MendaciousAbstraction;

/**
 * Core of the alpha sharp algorithm, deduces the invisible dependencies. To be
 * honest, some of the functions can be cut up into smaller pieces, I am aware
 * of this. However, as time is always tight, I tried to keep readability
 * decent, yet it is not the cleanest code ever.
 * 
 * @author svzelst
 *
 * @param <E>
 */
public class AlphaSharpConItAlgorithmImpl<E> {

	private final AlphaSharpAbstraction<E> abstraction;

	private final Progress progress;
	private final Executor executor;

	private final Map<E, Collection<AlphaPairImpl<Collection<E>, Collection<E>>>> preSets = new HashMap<>();
	private final Map<E, Collection<AlphaPairImpl<Collection<E>, Collection<E>>>> postSets = new HashMap<>();

	public AlphaSharpConItAlgorithmImpl(PluginContext context, AlphaSharpAbstraction<E> abstr) {
		this.abstraction = abstr;
		progress = context == null ? new FakeProgressImpl() : context.getProgress();
		executor = context == null ? Executors.newCachedThreadPool() : context.getExecutor();
	}

	public AlphaSharpConITDataModel<E> run() {
		setupAllPrePostSets();
		// step 7 & 8
		Collection<AlphaPairOfAlphaPairCollection<E>> nonRedundantMendDep = constructPairOfPlaceSetsWithMendaciousDependencies(
				abstraction.getNonRedundantMendaciousDependencies(),
				new AlphaSharpMendaciousDependencyNodeExpanderImpl<>(
						abstraction.getNonRedundantMendaciousDependencies(), abstraction.getParallelAbstraction(),
						postSets, preSets));

		// step 9 & 10
		Collection<AlphaPairOfAlphaPairCollection<E>> redundantMenDep = constructPairOfPlaceSetsWithMendaciousDependencies(
				abstraction.getRedundantMendaciousDependencies(),
				new AlphaSharpMendaciousDependencyRedundantNodeExpanderImpl<>(
						abstraction.getRedundantMendaciousDependencies(), abstraction.getParallelAbstraction(),
						postSets, preSets, nonRedundantMendDep));

		Collection<AlphaPairOfAlphaPairCollection<E>> allMenDep = new HashSet<>(nonRedundantMendDep);
		allMenDep.addAll(redundantMenDep);

		return new AlphaSharpConITDataModel<>(deduceSequentialDependencies(allMenDep),
				deduceParallelDependencies(allMenDep));
	}

	/**
	 * step 12 of the ConIT algorithm, constructs all sequential dependencies
	 * between invisible and (in)visible tasks.
	 * 
	 * @param allMenDep
	 * @return a collection of pairs of objects. if the object of a pair is an
	 *         instance of AlphaPairOfAlphaPairCollection, it refers to an
	 *         invisible transition. If not, it refers to a visible transition
	 *         of type E.
	 */
	private Collection<Pair<Object, Object>> deduceParallelDependencies(
			Collection<AlphaPairOfAlphaPairCollection<E>> allMenDep) {
		Collection<Pair<Object, Object>> result = new HashSet<>();

		// invisible || invisible
		for (AlphaPairOfAlphaPairCollection<E> t1 : allMenDep) {
			t2Loop: for (AlphaPairOfAlphaPairCollection<E> t2 : allMenDep) {
				for (AlphaPairImpl<Collection<E>, Collection<E>> pair1 : t1.getFirst()) {
					for (AlphaPairImpl<Collection<E>, Collection<E>> pair2 : t2.getFirst()) {
						for (E a : pair1.getFirst()) {
							for (E a2 : pair2.getFirst()) {
								if (abstraction.getParallelAbstraction().holds(a, a2)) {
									result.add(new Pair<Object, Object>(t1, t2));
									break t2Loop;
								}
							}
						}
						for (E x : pair1.getSecond()) {
							for (E x2 : pair2.getSecond()) {
								if (abstraction.getParallelAbstraction().holds(x, x2)) {
									result.add(new Pair<Object, Object>(t1, t2));
									break t2Loop;
								}
							}
						}
					}
				}
			}
		}

		// visible || invisible && invisible || visible (== symmetrical)
		for (AlphaPairOfAlphaPairCollection<E> t : allMenDep) {
			eLoop: for (E e : abstraction.getEventClasses()) {
				for (AlphaPairImpl<Collection<E>, Collection<E>> pair : t.getFirst()) {
					boolean parallel = false;
					e1Loop: for (E e1 : pair.getFirst()) {
						if (abstraction.getParallelAbstraction().holds(e, e1)) {
							parallel = true;
							break e1Loop;
						}
					}
					if (!parallel) {
						xLoop: for (E x : pair.getSecond()) {
							if (abstraction.getParallelAbstraction().holds(e, x)) {
								parallel = true;
								break xLoop;
							}
						}
					}
					if (!parallel) {
						continue eLoop;
					}
				}
				result.add(new Pair<Object, Object>(e, t));
				result.add(new Pair<Object, Object>(t, e));
			}
		}
		return result;
	}

	/**
	 * Step 11 of the ConIT algorithm, constructs all sequential dependencies
	 * between invisible and (in)visible tasks.
	 * 
	 * @param allMenDep
	 * @return a collection of pairs of objects. if the object of a pair is an
	 *         instance of AlphaPairOfAlphaPairCollection, it refers to an
	 *         invisible transition. If not, it refers to a visible transition
	 *         of type E
	 * 
	 */
	private Collection<Pair<Object, Object>> deduceSequentialDependencies(
			Collection<AlphaPairOfAlphaPairCollection<E>> allMenDep) {
		Collection<Pair<Object, Object>> result = new HashSet<>();

		// invisible -> invisible
		for (AlphaPairOfAlphaPairCollection<E> t1 : allMenDep) {
			for (AlphaPairOfAlphaPairCollection<E> t2 : allMenDep) {
				for (AlphaPairImpl<Collection<E>, Collection<E>> pair : t1.getSecond()) {
					if (t2.getFirst().contains(pair)) {
						result.add(new Pair<Object, Object>(t1, t2));
					}
				}
			}
		}

		// visible -> invisible		
		for (AlphaPairOfAlphaPairCollection<E> t : allMenDep) {
			for (AlphaPairImpl<Collection<E>, Collection<E>> pair : t.getFirst()) {
				for (E a : pair.getFirst()) {
					result.add(new Pair<Object, Object>(a, t));
				}
			}
		}

		// invisible -> visible
		for (AlphaPairOfAlphaPairCollection<E> t : allMenDep) {
			for (AlphaPairImpl<Collection<E>, Collection<E>> pair : t.getSecond()) {
				for (E b : pair.getSecond()) {
					result.add(new Pair<Object, Object>(t, b));
				}
			}
		}
		return result;
	}

	private Collection<AlphaPairOfAlphaPairCollection<E>> constructPairOfPlaceSetsWithMendaciousDependencies(
			MendaciousAbstraction<E> ma, AlphaSharpMendaciousDependencyNodeExpanderImpl<E> expander) {
		final Stack<AlphaPairOfAlphaPairCollection<E>> stack = setupStackForPairOfPlaceSetsWithMandaciousDependencies(
				ma);
		if (expander instanceof AlphaSharpMendaciousDependencyRedundantNodeExpanderImpl<?>) {
			((AlphaSharpMendaciousDependencyRedundantNodeExpanderImpl<E>) expander)
					.setRedundantCandidates(new HashSet<AlphaPairOfAlphaPairCollection<E>>(stack));
		}
		MultiThreadedSearcher<AlphaPairOfAlphaPairCollection<E>> searcher = new MultiThreadedSearcher<>(expander,
				MultiThreadedSearcher.BREADTHFIRST);
		List<AlphaPairOfAlphaPairCollection<E>> result = new ArrayList<>();
		searcher.addInitialNodes(stack);
		try {
			searcher.startSearch(executor, progress, result);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}

	private final Stack<AlphaPairOfAlphaPairCollection<E>> setupStackForPairOfPlaceSetsWithMandaciousDependencies(
			MendaciousAbstraction<E> ma) {
		final Stack<AlphaPairOfAlphaPairCollection<E>> stack = new Stack<>();
		for (E a : abstraction.getEventClasses()) {
			for (E b : abstraction.getEventClasses()) {
				if (ma.holds(a, b)) {
					for (AlphaPairImpl<Collection<E>, Collection<E>> postPair : getPreOrPostSet(a, postSets, false)) {
						prePairLoop: for (AlphaPairImpl<Collection<E>, Collection<E>> prePair : getPreOrPostSet(b,
								preSets, true)) {
							for (E x : postPair.getSecond()) {
								for (E y : prePair.getFirst()) {
									if (abstraction.getParallelAbstraction().holds(x, y)) {
										continue prePairLoop;
									}
								}
							}
							Collection<AlphaPairImpl<Collection<E>, Collection<E>>> first = new HashSet<>();
							Collection<AlphaPairImpl<Collection<E>, Collection<E>>> second = new HashSet<>();
							first.add(postPair);
							second.add(prePair);
							stack.add(new AlphaPairOfAlphaPairCollection<>(first, second, -1, -1));
						}
					}
				}
			}
		}
		return stack;
	}

	private void setupAllPrePostSets() {
		for (E e : abstraction.getEventClasses()) {
			getPreOrPostSet(e, preSets, true);
			getPreOrPostSet(e, postSets, false);
		}
	}

	private Collection<AlphaPairImpl<Collection<E>, Collection<E>>> getPreOrPostSet(E e,
			Map<E, Collection<AlphaPairImpl<Collection<E>, Collection<E>>>> set, boolean pre) {
		if (set.containsKey(e))
			return set.get(e);
		final Stack<AlphaPairImpl<Collection<E>, Collection<E>>> stack = new Stack<>();
		Collection<E> first = new HashSet<E>();
		Collection<E> second = new HashSet<E>();
		if (pre)
			second.add(e);
		else
			first.add(e);
		stack.add(new AlphaPairImpl<Collection<E>, Collection<E>>(first, second, -1, -1));
		AlphaClassicNodeExpanderImpl<E> expander = new AlphaClassicNodeExpanderImpl<E>(
				abstraction.getRepairedCausalAbstraction(), abstraction.getUnrelatedAbstraction(),false);
		List<AlphaPairImpl<Collection<E>, Collection<E>>> result = new ArrayList<>();
		MultiThreadedSearcher<AlphaPairImpl<Collection<E>, Collection<E>>> searcher = new MultiThreadedSearcher<>(
				expander, MultiThreadedSearcher.BREADTHFIRST);
		searcher.addInitialNodes(stack);
		try {
			searcher.startSearch(executor, progress, result);
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		set.put(e, result);
		return result;
	}

}
