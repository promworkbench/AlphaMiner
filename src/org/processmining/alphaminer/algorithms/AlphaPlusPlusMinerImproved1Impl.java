package org.processmining.alphaminer.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import org.processmining.alphaminer.abstractions.AlphaPlusPlusAbstraction;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.models.AlphaTripleImpl;
import org.processmining.alphaminer.models.Union;
import org.processmining.alphaminer.models.UnionImpl;
import org.processmining.alphaminer.parameters.AlphaPlusMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.search.MultiThreadedSearcher;
import org.processmining.logabstractions.factories.CausalAbstractionFactory;
import org.processmining.logabstractions.factories.EventuallyFollowsAbstractionFactory;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.CausalSuccessionAbstraction;
import org.processmining.logabstractions.models.EventuallyFollowsAbstraction;
import org.processmining.logabstractions.models.LongTermFollowsAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Implementation of the alpha++ algorithm. Code based on:
 * "Mining Models with Non-Free-Choice Constructs"; Lijie Wen, Wil M.P. van der
 * Aalst, Jianmin Wang, and Jiaguang Sun
 * "Detecting Implicit Dependencies Between Tasks from Event Logs"; Lijie Wen,
 * Jianmin Wang, and Jiaguang Sun
 * 
 * The algorithmic skeleton is adopted from the first paper. However steps 8, 10
 * and 14 are based on theorems 1,2, and 3 respectively of the second paper.
 * Special thanks to Liejie Wen for clarification of the relation between the
 * two papers
 * 
 * change w.r.t. orginal: step 8 of the algorithm is performed with all causal
 * dependencies including type 1 and 2.
 * 
 *
 * @author svzelst
 * @author bfvdongen -> node expander concept
 *
 */
public class AlphaPlusPlusMinerImproved1Impl<E, A extends AlphaPlusPlusAbstraction<E>, P extends AlphaPlusMinerParameters>
		extends AlphaPlusMinerImpl<E, A, P> {

	public AlphaPlusPlusMinerImproved1Impl(P parameters, A abstraction, PluginContext context) {
		super(parameters, abstraction, context);
	}

	protected CausalAbstraction<E> causalRelationsIncludingTheorem(CausalAbstraction<E> current,
			Collection<Pair<E, E>> newRelations) {
		double[][] matrix = new double[current.getMatrix().length][];
		for (int row = 0; row < current.getMatrix().length; row++) {
			matrix[row] = Arrays.copyOf(current.getMatrix()[row], current.getMatrix()[row].length);
		}
		for (Pair<E, E> cr : newRelations) {
			matrix[current.getIndex(cr.getFirst())][current.getIndex(cr.getSecond())] = current.getThreshold();
		}
		return CausalAbstractionFactory.constructCausalAbstraction(current.getEventClasses(), matrix,
				current.getThreshold());
	}

	private boolean checkPrerequisiteForTheoremOne(ParallelAbstraction<E> pa, EventuallyFollowsAbstraction<E> efa, E a,
			Collection<E> APrime) {
		for (E a2 : APrime) {
			if (pa.holds(a2, a) || efa.holds(a2, a)) {
				return false;
			}
		}
		return true;
	}

	protected Collection<AlphaPairImpl<Union<E>, Union<E>>> computeAlphaPairsBasedOnTheoremOneAndTwo(
			AlphaMiner<E, ?, ?> miner, CausalAbstraction<E> th1th2WithOriginal, UnrelatedAbstraction<E> ua,
			LongTermFollowsAbstraction<E> ltfa) {

		final Stack<AlphaPairImpl<Union<E>, Union<E>>> stack = new Stack<>();
		for (AlphaPairImpl<Collection<E>, Collection<E>> place : miner.getAlphaPairToPlaceMapping().keySet()) {
			Union<E> firstUnion = new UnionImpl<>(place.getFirst(), new HashSet<E>());
			Union<E> secondUnion = new UnionImpl<>(place.getSecond(), new HashSet<E>());
			stack.add(new AlphaPairImpl<Union<E>, Union<E>>(firstUnion, secondUnion, -1, -1));
		}
		AlphaPlusPlusTh1Th2NodeExpanderImproved1Impl<E> expander = new AlphaPlusPlusTh1Th2NodeExpanderImproved1Impl<E>(
				th1th2WithOriginal, ua, ltfa, getAbstraction().getLengthOneLoopAbstraction().getAllGEQThreshold());
		final List<AlphaPairImpl<Union<E>, Union<E>>> result = new ArrayList<>();
		MultiThreadedSearcher<AlphaPairImpl<Union<E>, Union<E>>> searcher = new MultiThreadedSearcher<>(expander,
				MultiThreadedSearcher.BREADTHFIRST);
		searcher.addInitialNodes(stack);
		try {
			searcher.startSearch(getExecutor(), getProgress(), result);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}

	protected List<AlphaPairImpl<Collection<E>, Collection<E>>> computeCandidateSetsForTheoremTwoCaseTwo(
			Collection<E> candidates, CausalAbstraction<E> cra, ParallelAbstraction<E> pa, UnrelatedAbstraction<E> ua,
			Collection<E> ignore) {
		final Stack<AlphaPairImpl<Collection<E>, Collection<E>>> pairs = new Stack<>();
		for (E t : candidates) {
			Set<E> setContainingT = new HashSet<>();
			setContainingT.add(t);
			pairs.add(new AlphaPairImpl<Collection<E>, Collection<E>>(new HashSet<E>(), setContainingT, -1,
					cra.getIndex(t)));
		}
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> nodeExpanderResult = new ArrayList<>();
		AlphaPlusPlusSetCausalToReferenceNodeExpanderImpl<E> expander = new AlphaPlusPlusSetCausalToReferenceNodeExpanderImpl<>(
				cra, ua, ignore);
		MultiThreadedSearcher<AlphaPairImpl<Collection<E>, Collection<E>>> searcher = new MultiThreadedSearcher<>(
				expander, MultiThreadedSearcher.BREADTHFIRST);
		searcher.addInitialNodes(pairs);
		try {
			searcher.startSearch(getExecutor(), getProgress(), nodeExpanderResult);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return nodeExpanderResult;
	}

	protected List<AlphaPairImpl<Collection<E>, Collection<E>>> computeCandidateSetsForTheoremTwoCaseOne(
			Collection<E> candidates, CausalAbstraction<E> cra, ParallelAbstraction<E> pa, UnrelatedAbstraction<E> ua) {
		final Stack<AlphaPairImpl<Collection<E>, Collection<E>>> pairs = new Stack<>();
		for (E t : candidates) {
			Set<E> setContainingT = new HashSet<>();
			setContainingT.add(t);
			pairs.add(new AlphaPairImpl<Collection<E>, Collection<E>>(setContainingT, new HashSet<E>(),
					getAbstraction().getIndex(t), -1));
		}
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> nodeExpanderResult = new ArrayList<>();
		AlphaPlusPlusReferenceCausalToSetNodeExpanderImpl<E> expander = new AlphaPlusPlusReferenceCausalToSetNodeExpanderImpl<E>(
				cra, ua, getAbstraction().getLengthOneLoopAbstraction().getAllGEQThreshold());
		MultiThreadedSearcher<AlphaPairImpl<Collection<E>, Collection<E>>> searcher = new MultiThreadedSearcher<>(
				expander, MultiThreadedSearcher.BREADTHFIRST);
		searcher.addInitialNodes(pairs);
		try {
			searcher.startSearch(getExecutor(), getProgress(), nodeExpanderResult);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return nodeExpanderResult;
	}

	private List<AlphaPairImpl<Collection<E>, Collection<E>>> computeEventClassSetsForTheoremOne() {
		final Stack<AlphaPairImpl<Collection<E>, Collection<E>>> pairs = new Stack<>();
		for (int i = 0; i < getAbstraction().getLengthOneLoopFreeEventClasses().getFirst().length; i++) {
			E e = getAbstraction().getLengthOneLoopFreeEventClasses().getFirst()[i];
			Collection<E> singleton = new HashSet<>();
			singleton.add(e);
			Collection<E> preEvent = getAbstraction().getLengthOneLoopFreeCausalRelationAbstraction()
					.getAllGeqForColumn(i);
			if (preEvent.size() > 1) {
				boolean candidate = false;
				loop: for (E e1 : preEvent) {
					for (E e2 : preEvent) {
						if (!e1.equals(e2) && getAbstraction().getLengthOneLoopFreeUnrelatedAbstraction().holds(e1, e2)
								&& !getAbstraction().getLengthOneLoopFreeCausalRelationAbstraction().getAllGeqForRow(e1)
										.equals(getAbstraction().getLengthOneLoopFreeCausalRelationAbstraction()
												.getAllGeqForRow(e2))) {
							candidate = true;
							break loop;
						}
					}
				}
				if (candidate) {
					pairs.add(new AlphaPairImpl<Collection<E>, Collection<E>>(new HashSet<E>(), singleton, -1, -1));
				}
			}
		}
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> searchResult = new ArrayList<>();
		AlphaClassicNodeExpanderImpl<E> expander = new AlphaClassicNodeExpanderImpl<>(
				getAbstraction().getLengthOneLoopFreeCausalRelationAbstraction(),
				getAbstraction().getLengthOneLoopFreeUnrelatedAbstraction(), new HashSet<E>(), false);
		MultiThreadedSearcher<AlphaPairImpl<Collection<E>, Collection<E>>> searcher = new MultiThreadedSearcher<>(
				expander, MultiThreadedSearcher.BREADTHFIRST);
		searcher.addInitialNodes(pairs);
		try {
			searcher.startSearch(getExecutor(), getProgress(), searchResult);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return searchResult;
	}

	/**
	 * Step 8 of alpha++. Note that "theorem 1" refers to Theorem 1 from the
	 * paper "Detecting Implicit Dependencies Between Tasks from Event Logs".
	 * 
	 * @param abstraction
	 * @param l1ls
	 * @param executor
	 * @param progress
	 * @return
	 */
	protected Collection<Pair<E, E>> computeImplicitDependenciesByTheoremOne(
			final AlphaPlusPlusAbstraction<E> abstraction) {
		final Collection<Pair<E, E>> result = new HashSet<>();
		List<AlphaPairImpl<Collection<E>, Collection<E>>> candidates = computeEventClassSetsForTheoremOne();
		for (AlphaPairImpl<Collection<E>, Collection<E>> pair1 : candidates) {
			for (AlphaPairImpl<Collection<E>, Collection<E>> pair2 : candidates) {
				for (E a : pair1.getFirst()) {
					if (!pair2.getFirst().contains(a)) {
						if (checkPrerequisiteForTheoremOne(abstraction.getLengthOneLoopFreeParallelAbstraction(),
								abstraction.getLengthOneLoopFreeEventuallyFollowsAbstraction(), a, pair2.getFirst())) {
							Collection<E> postA = abstraction.getLengthOneLoopFreeCausalRelationAbstraction()
									.getAllGeqForRow(a);
							for (E b : pair2.getSecond()) {
								if (!postA.contains(b)) {
									result.add(new Pair<E, E>(a, b));
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	protected Collection<Pair<E, E>> computeImplicitDependenciesByTheoremThree(
			CausalAbstraction<E> craIncludingTh1Th2) {
		Collection<Pair<E, E>> newDependencies = new HashSet<>();
		CausalPrecedenceAbstraction<E> cpa = CausalAbstractionFactory.constructAlphaPlusPlusCausalPrecedenceAbstraction(
				craIncludingTh1Th2, getAbstraction().getLengthOneLoopFreeUnrelatedAbstraction());
		CausalSuccessionAbstraction<E> csa = CausalAbstractionFactory.constructAlphaPlusPlusCausalSuccessionAbstraction(
				craIncludingTh1Th2, getAbstraction().getLengthOneLoopFreeUnrelatedAbstraction());
		EventuallyFollowsAbstraction<E> efa = EventuallyFollowsAbstractionFactory
				.constructAlphaPlusPlusEventuallyFollowsAbstraction(craIncludingTh1Th2,
						getAbstraction().getLengthOneLoopFreeLongTermFollowsAbstraction());
		for (E a : getAbstraction().getEventClasses()) {
			for (E b : getAbstraction().getEventClasses()) {
				if (!(getAbstraction().getLengthOneLoopAbstraction().holds(a)
						|| getAbstraction().getLengthOneLoopAbstraction().holds(b)) && !a.equals(b)
						&& csa.holds(a, b)) {
					final Stack<AlphaPairImpl<Collection<E>, Collection<E>>> stack = new Stack<>();
					stack.add(new AlphaPairImpl<Collection<E>, Collection<E>>(new HashSet<E>(), new HashSet<E>(), -1,
							-1));
					AlphaPlusPlusLTFDiffNodeExpanderImpl<E> expander = new AlphaPlusPlusLTFDiffNodeExpanderImpl<E>(
							getAbstraction().getLengthOneLoopFreeLongTermFollowsAbstraction(), cpa,
							getAbstraction().getLengthOneLoopFreeParallelAbstraction(), a, b,
							getAbstraction().getLengthOneLoopAbstraction().getAllGEQThreshold());
					final List<AlphaPairImpl<Collection<E>, Collection<E>>> res = new ArrayList<>();
					MultiThreadedSearcher<AlphaPairImpl<Collection<E>, Collection<E>>> searcher = new MultiThreadedSearcher<>(
							expander, MultiThreadedSearcher.BREADTHFIRST);
					searcher.addInitialNodes(stack);
					try {
						searcher.startSearch(getExecutor(), getProgress(), res);
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
					newDependencies.addAll(processResultSetForTheorem(res, a, b, craIncludingTh1Th2, csa, efa,
							getAbstraction().getLengthOneLoopAbstraction().getAllGEQThreshold()));
				}
			}
		}
		return newDependencies;
	}

	protected Collection<Pair<E, E>> computeImplicitDependenciesByTheoremTwo(CausalAbstraction<E> updatedCRA,
			AlphaPlusPlusAbstraction<E> abstraction) {
		Set<Pair<E, E>> result = new HashSet<>();
		Collection<E> candidatesCaseOne = getCandidatesForTheoremTwoCaseOne(updatedCRA);
		Collection<E> candidatesCaseTwo = getCandidatesForTheoremTwoCaseTwo(updatedCRA);
		result.addAll(computeImplicitDependenciesByTheoremTwoCaseOne(candidatesCaseOne, updatedCRA,
				abstraction.getLengthOneLoopFreeParallelAbstraction(),
				abstraction.getLengthOneLoopFreeUnrelatedAbstraction(),
				abstraction.getLengthOneLoopFreeEventuallyFollowsAbstraction(),
				abstraction.getLengthOneLoopFreeLongTermFollowsAbstraction(),
				abstraction.getLengthOneLoopFreeCausalPrecedenceAbstraction()));
		result.addAll(computeImplicitDependenciesByTheoremTwoCaseTwo(candidatesCaseTwo, updatedCRA,
				abstraction.getLengthOneLoopFreeParallelAbstraction(),
				abstraction.getLengthOneLoopFreeUnrelatedAbstraction(),
				abstraction.getLengthOneLoopFreeEventuallyFollowsAbstraction(),
				abstraction.getLengthOneLoopFreeLongTermFollowsAbstraction(),
				abstraction.getLengthOneLoopFreeCausalSuccessionAbstraction()));
		return result;
	}

	protected Collection<Pair<E, E>> computeImplicitDependenciesByTheoremTwoCaseOne(Collection<E> candidates,
			CausalAbstraction<E> cra, ParallelAbstraction<E> pa, UnrelatedAbstraction<E> ua,
			EventuallyFollowsAbstraction<E> efa, LongTermFollowsAbstraction<E> ltfa,
			CausalPrecedenceAbstraction<E> cpa) {
		final Set<Pair<E, E>> newCausalRelations = new HashSet<>();
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> nodeExpanderResult = computeCandidateSetsForTheoremTwoCaseOne(
				candidates, cra, pa, ua);
		for (AlphaPairImpl<Collection<E>, Collection<E>> alphaPair : nodeExpanderResult) {
			Collection<E> Y = alphaPair.getSecond();
			for (E a : getAbstraction().getEventClasses()) {
				if (!getAbstraction().getLengthOneLoopAbstraction().holds(a)) {
					for (E b : getAbstraction().getEventClasses()) {
						if (!getAbstraction().getLengthOneLoopAbstraction().holds(b)
								&& cpa.getValue(a, b) >= cpa.getThreshold()) {
							boolean elemParallelBOrEventuallyB = false;
							cond1: for (E y : Y) {
								if (pa.getValue(y, b) >= pa.getThreshold()
										|| efa.getValue(y, b) >= efa.getThreshold()) {
									elemParallelBOrEventuallyB = true;
									break cond1;
								}
							}
							if (elemParallelBOrEventuallyB) {
								boolean elemParallelAorEventuallyA = false;
								cond2: for (E y : Y) {
									if (pa.getValue(y, a) >= pa.getThreshold()
											|| efa.getValue(y, a) >= efa.getThreshold()) {
										elemParallelAorEventuallyA = true;
										break cond2;
									}
								}
								if (!elemParallelAorEventuallyA) {
									@SuppressWarnings("unchecked")
									E t = (E) alphaPair.getFirst().toArray()[0];
									if (ltfa.getValue(t, a) >= ltfa.getThreshold()) {
										newCausalRelations.add(new Pair<E, E>(t, a));
									}
								}
							}
						}
					}
				}
			}
		}
		return newCausalRelations;
	}

	protected Collection<Pair<E, E>> computeImplicitDependenciesByTheoremTwoCaseTwo(Collection<E> candidates,
			CausalAbstraction<E> cra, ParallelAbstraction<E> pa, UnrelatedAbstraction<E> ua,
			EventuallyFollowsAbstraction<E> efa, LongTermFollowsAbstraction<E> ltfa,
			CausalSuccessionAbstraction<E> csa) {
		final Set<Pair<E, E>> newCausalRelations = new HashSet<>();
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> nodeExpanderResult = computeCandidateSetsForTheoremTwoCaseTwo(
				candidates, cra, pa, ua, getAbstraction().getLengthOneLoopAbstraction().getAllGEQThreshold());
		for (AlphaPairImpl<Collection<E>, Collection<E>> alphaPair : nodeExpanderResult) {
			Collection<E> Y = alphaPair.getFirst();
			for (E a : getAbstraction().getEventClasses()) {
				if (!getAbstraction().getLengthOneLoopAbstraction().holds(a)) {
					for (E b : getAbstraction().getEventClasses()) {
						if (!getAbstraction().getLengthOneLoopAbstraction().holds(b)
								&& csa.getValue(a, b) >= csa.getThreshold()) {
							boolean elemParallelBOrEventuallyB = false;
							cond1: for (E y : Y) {
								if (pa.getValue(b, y) >= pa.getThreshold()
										|| efa.getValue(b, y) >= efa.getThreshold()) {
									elemParallelBOrEventuallyB = true;
									break cond1;
								}
							}
							if (elemParallelBOrEventuallyB) {
								boolean elemParallelAorEventuallyA = false;
								cond2: for (E y : Y) {
									if (pa.getValue(a, y) >= pa.getThreshold()
											|| efa.getValue(a, y) >= efa.getThreshold()) {
										elemParallelAorEventuallyA = true;
										break cond2;
									}
								}
								if (!elemParallelAorEventuallyA) {
									@SuppressWarnings("unchecked")
									E t = (E) alphaPair.getSecond().toArray()[0];
									if (ltfa.getValue(a, t) >= ltfa.getThreshold()) {
										newCausalRelations.add(new Pair<E, E>(a, t));
									}
								}
							}
						}
					}
				}
			}
		}
		return newCausalRelations;
	}

	protected Collection<AlphaPairImpl<Collection<E>, Collection<E>>> computeMaximumTransitionSetsBasedOnTheorem3(
			CausalAbstraction<E> craTh3) {
		final Stack<AlphaPairImpl<Collection<E>, Collection<E>>> stack = new Stack<>();
		for (Pair<E, E> cr : craTh3.getAllGEQThreshold()) {
			Collection<E> left = new HashSet<>();
			left.add(cr.getFirst());
			Collection<E> right = new HashSet<>();
			right.add(cr.getSecond());
			stack.add(new AlphaPairImpl<Collection<E>, Collection<E>>(left, right, -1, -1));
		}
		AlphaClassicNodeExpanderImpl<E> expander = new AlphaClassicNodeExpanderImpl<>(craTh3,
				getAbstraction().getUnrelatedAbstraction(),
				getAbstraction().getLengthOneLoopAbstraction().getAllGEQThreshold());
		MultiThreadedSearcher<AlphaPairImpl<Collection<E>, Collection<E>>> searcher = new MultiThreadedSearcher<>(
				expander, MultiThreadedSearcher.BREADTHFIRST);
		searcher.addInitialNodes(stack);
		List<AlphaPairImpl<Collection<E>, Collection<E>>> result = new ArrayList<>();
		try {
			searcher.startSearch(getExecutor(), getProgress(), result);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}

	protected Petrinet connectLengthOneLoops(Petrinet net,
			Collection<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> lengthOneLoopTriples) {
		Collection<AlphaPairImpl<Collection<E>, Collection<E>>> castToPair = new HashSet<>();
		for (AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> triple : lengthOneLoopTriples) {
			Collection<E> first = new HashSet<E>(triple.getFirst());
			first.addAll(triple.getThird());
			Collection<E> second = new HashSet<E>(triple.getSecond());
			second.addAll(triple.getThird());
			castToPair.add(new AlphaPairImpl<Collection<E>, Collection<E>>(first, second,
					Math.max(triple.getMaxIndexOfFirst(), triple.getMaxIndexOfThird()),
					Math.max(triple.getMaxIndexOfSecond(), triple.getMaxIndexOfThird())));
		}
		return addPlaces(net, castToPair);
	}

	/**
	 * step 4 and step 5 of alpha++, constructing L_{W}
	 */
	protected List<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> createLengthOneLoopTriples(
			final AlphaPlusPlusAbstraction<E> abstraction) {
		final Stack<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> triples = new Stack<>();
		for (E l1l : abstraction.getLengthOneLoopAbstraction().getAllGEQThreshold()) {
			Set<E> l1lSet = new HashSet<E>();
			l1lSet.add(l1l);
			triples.add(new AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>(new HashSet<E>(),
					new HashSet<E>(), l1lSet, -1, -1, abstraction.getIndex(l1l)));
		}
		final List<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> result = new ArrayList<>();
		AlphaPlusPlusL1LNodeExpanderImpl<E> expander = new AlphaPlusPlusL1LNodeExpanderImpl<E>(abstraction);
		MultiThreadedSearcher<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> searcher = new MultiThreadedSearcher<>(
				expander, MultiThreadedSearcher.BREADTHFIRST);
		searcher.addInitialNodes(triples);
		try {
			searcher.startSearch(getExecutor(), getProgress(), result);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}

	protected Collection<E> expandPrimeSetTheorem3(Collection<E> primeSet, Collection<E> set,
			Collection<E> oppositionSet, E t, CausalSuccessionAbstraction<E> csa, EventuallyFollowsAbstraction<E> efa) {
		if (!set.contains(t)) {
			boolean causalSucc = false;
			for (E e : oppositionSet) {
				if (csa.getValue(e, t) >= csa.getThreshold()) {
					causalSucc = true;
					break;
				}
			}
			if (causalSucc) {
				boolean eventually = false;
				for (E e : set) {
					if (efa.getValue(e, t) >= efa.getThreshold()) {
						eventually = true;
						break;
					}
				}
				if (eventually) {
					primeSet.add(t);
				}
			}
		}
		return primeSet;
	}

	protected Collection<E> getCandidatesForTheoremTwoCaseOne(CausalAbstraction<E> updatedCRA) {
		Set<E> candidatesCaseOne = new HashSet<>();
		for (E t : updatedCRA.getEventClasses()) {
			Collection<E> causalDependent = updatedCRA.getAllGeqForRow(t);
			candidatesOneLoop: for (E t1 : causalDependent) {
				for (E t2 : causalDependent) {
					if (getAbstraction().getLengthOneLoopFreeParallelAbstraction().holds(t1, t2)) {
						candidatesCaseOne.add(t);
						break candidatesOneLoop;
					}
				}
			}
		}
		return candidatesCaseOne;
	}

	protected Collection<E> getCandidatesForTheoremTwoCaseTwo(CausalAbstraction<E> updatedCRA) {
		Set<E> candidatesCaseTwo = new HashSet<>();
		for (E t : updatedCRA.getEventClasses()) {
			Collection<E> causalDependent = updatedCRA.getAllGeqForColumn(t);
			candidatesTwoLoop: for (E t1 : causalDependent) {
				for (E t2 : causalDependent) {
					if (getAbstraction().getLengthOneLoopFreeParallelAbstraction().holds(t1, t2)) {
						candidatesCaseTwo.add(t);
						break candidatesTwoLoop;
					}
				}
			}
		}
		return candidatesCaseTwo;
	}

	protected CausalAbstraction<E> getCausalRelationsIncludingTheoremTwo(CausalAbstraction<E> original,
			Collection<Pair<E, E>> newCausalRelations) {
		double[][] matrix = original.getMatrix();
		for (Pair<E, E> cr : newCausalRelations) {
			matrix[original.getIndex(cr.getFirst())][original.getIndex(cr.getSecond())] = original.getThreshold();
		}
		return CausalAbstractionFactory.constructCausalAbstraction(original.getEventClasses(), matrix,
				original.getThreshold());
	}

	protected CausalAbstraction<E> getCausalRelationsOfTheoremOnly(Collection<Pair<E, E>> craTh) {
		double[][] matrix = new double[getAbstraction().getEventClasses().length][getAbstraction()
				.getEventClasses().length];
		for (Pair<E, E> cr : craTh) {
			matrix[getAbstraction().getIndex(cr.getFirst())][getAbstraction()
					.getIndex(cr.getSecond())] = CausalAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
		}
		return CausalAbstractionFactory.constructCausalAbstraction(getAbstraction().getEventClasses(), matrix,
				CausalAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN);
	}

	protected Collection<Pair<E, E>> processResultSetForTheorem(
			Collection<AlphaPairImpl<Collection<E>, Collection<E>>> resSet, E a, E b, CausalAbstraction<E> cra,
			CausalSuccessionAbstraction<E> csa, EventuallyFollowsAbstraction<E> efa, Collection<E> ignore) {
		Collection<Pair<E, E>> result = new HashSet<>();
		for (AlphaPairImpl<Collection<E>, Collection<E>> alphaPair : resSet) {
			Collection<E> APrime = new HashSet<>();
			Collection<E> BPrime = new HashSet<>();
			for (E t : getAbstraction().getEventClasses()) {
				if (!ignore.contains(t)) {
					APrime = expandPrimeSetTheorem3(APrime, alphaPair.getFirst(), alphaPair.getSecond(), t, csa, efa);
					BPrime = expandPrimeSetTheorem3(BPrime, alphaPair.getSecond(), alphaPair.getFirst(), t, csa, efa);
				}
			}
			Collection<E> preBBPrime = cra.getAllGeqForColumns(alphaPair.getSecond());
			preBBPrime.addAll(cra.getAllGeqForColumns(BPrime));
			for (E ai : alphaPair.getFirst()) {
				if (preBBPrime.containsAll(cra.getAllGeqForColumn(ai))) {
					result.add(new Pair<E, E>(a, ai));
				}
			}
			Collection<E> preAAPrime = cra.getAllGeqForColumns(alphaPair.getFirst());
			preAAPrime.addAll(cra.getAllGeqForColumns(APrime));
			for (E bi : alphaPair.getSecond()) {
				if (preAAPrime.containsAll(cra.getAllGeqForColumn(bi))) {
					result.add(new Pair<E, E>(b, bi));
				}
			}
		}
		return result;
	}

	protected Collection<Pair<E, E>> reduceCausalRelationsOfTheorem2(Collection<Pair<E, E>> dependencies,
			EventuallyFollowsAbstraction<E> efa) {
		Collection<Pair<E, E>> result = new HashSet<>(dependencies);
		for (Pair<E, E> cr1 : dependencies) {
			for (Pair<E, E> cr2 : dependencies) {
				if (cr1.getFirst().equals(cr2.getFirst())
						&& efa.getValue(cr1.getSecond(), cr2.getSecond()) >= efa.getThreshold()) {
					result.remove(cr2);
				}
				if (cr1.getSecond().equals(cr2.getSecond())
						&& efa.getValue(cr1.getFirst(), cr2.getFirst()) >= efa.getThreshold()) {
					result.remove(cr1);
				}
			}
		}
		return result;
	}

	protected CausalAbstraction<E> reduceCausalRelationsOfTheorem3(Collection<Pair<E, E>> relations) {
		Map<E, Integer> indices = new HashMap<>();
		for (Pair<E, E> pair : relations) {
			indices.put(pair.getFirst(), getAbstraction().getIndex(pair.getFirst()));
			indices.put(pair.getSecond(), getAbstraction().getIndex(pair.getSecond()));
		}
		CausalAbstraction<E> cra = CausalAbstractionFactory.constructCausalAbstraction(relations, indices,
				getAbstraction().getEventClasses());
		double[][] newCraMatrix = cra.getMatrix();
		Set<E> sourcesAnalyzed = new HashSet<>();
		for (Pair<E, E> relation : relations) {
			if (!sourcesAnalyzed.contains(relation.getFirst())) {
				sourcesAnalyzed.add(relation.getFirst());
				Set<E> visited = new HashSet<>();
				Set<E> reached = new HashSet<>();
				Queue<E> candidates = new LinkedList<>();
				for (E candidate : cra.getAllGeqForRow(relation.getFirst())) {
					candidates.offer(candidate);
				}
				E candidate;
				while ((candidate = candidates.poll()) != null) {
					visited.add(candidate);
					for (E newCandidate : cra.getAllGeqForRow(candidate)) {
						reached.add(newCandidate);
						if (!visited.contains(newCandidate)) {
							candidates.offer(newCandidate);
						}
					}
				}
				for (E indirectlyReached : reached) {
					newCraMatrix[cra.getIndex(relation.getFirst())][cra.getIndex(indirectlyReached)] = cra
							.getThreshold() - 1;
				}
			}
		}
		return CausalAbstractionFactory.constructCausalAbstraction(cra.getEventClasses(), newCraMatrix,
				cra.getThreshold());
	}

	protected void removeAllAlphaPairsIncludingLengthOneLoops(
			Collection<AlphaPairImpl<Union<E>, Union<E>>> alphaPairsTh1Th2,
			Collection<AlphaPairImpl<Collection<E>, Collection<E>>> alphaPairsTh3,
			Collection<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> lengthOneLoopTriples) {
		// remove th1/th2 result
		Collection<AlphaPairImpl<Union<E>, Union<E>>> th1Th2ToRemove = new HashSet<>();
		for (AlphaPairImpl<Union<E>, Union<E>> pair : alphaPairsTh1Th2) {
			for (AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> triple : lengthOneLoopTriples) {
				if (triple.getFirst().containsAll(pair.getFirst())
						&& triple.getSecond().containsAll(pair.getSecond())) {
					th1Th2ToRemove.add(pair);
					break;
				}
			}
		}
		alphaPairsTh1Th2.removeAll(th1Th2ToRemove);

		// remove th3 result
		Collection<AlphaPairImpl<Collection<E>, Collection<E>>> th3ToRemove = new HashSet<>();
		for (AlphaPairImpl<Collection<E>, Collection<E>> pair : alphaPairsTh3) {
			for (AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> triple : lengthOneLoopTriples) {
				if (triple.getFirst().containsAll(pair.getFirst())
						&& triple.getSecond().containsAll(pair.getSecond())) {
					th3ToRemove.add(pair);
					break;
				}
			}
		}
		alphaPairsTh3.removeAll(th3ToRemove);
	}

	/**
	 * code based on algorithmic sketch presented in:
	 * "Mining Models with Non-Free-Choice Constructs"; Lijie Wen, Wil M.P. van
	 * der Aalst, Jianmin Wang, and Jiaguang Sun
	 * 
	 * some changes applied to the sketch, documented here:
	 * 
	 * 
	 */
	public Pair<Petrinet, Marking> run() {
		// steps 1 - 5
		Collection<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> lengthOneLoopTriples = createLengthOneLoopTriples(
				getAbstraction());

		// step 8
		Collection<Pair<E, E>> causalRelTh1 = computeImplicitDependenciesByTheoremOne(getAbstraction());

		// step 9
		AlphaPlusMinerParameters plusParams = new AlphaPlusMinerParameters(AlphaVersion.PLUS, true);
		AlphaMiner<E, A, ?> alphaPlusMiner = AlphaMinerFactory.createAlphaPlusMiner(getContext(), plusParams,
				getAbstraction());
		alphaPlusMiner.run();

		// step 10
		CausalAbstraction<E> craIncludingTh1 = causalRelationsIncludingTheorem(
				getAbstraction().getLengthOneLoopFreeCausalRelationAbstraction(), causalRelTh1);

		Collection<Pair<E, E>> causalRelTh2 = computeImplicitDependenciesByTheoremTwo(craIncludingTh1,
				getAbstraction());

		causalRelTh2 = reduceCausalRelationsOfTheorem2(causalRelTh2,
				getAbstraction().getLengthOneLoopFreeEventuallyFollowsAbstraction());

		CausalAbstraction<E> craIncludingTh1AndTh2 = causalRelationsIncludingTheorem(craIncludingTh1, causalRelTh2);

		CausalAbstraction<E> craIncludingOnlyTh2 = causalRelationsIncludingTheorem(
				getAbstraction().getLengthOneLoopFreeCausalRelationAbstraction(), causalRelTh2);

		Collection<AlphaPairImpl<Union<E>, Union<E>>> alphaPairsTh1Th2 = computeAlphaPairsBasedOnTheoremOneAndTwo(
				alphaPlusMiner, craIncludingTh1AndTh2, getAbstraction().getLengthOneLoopFreeUnrelatedAbstraction(),
				getAbstraction().getLengthOneLoopFreeLongTermFollowsAbstraction());

		Collection<Pair<E, E>> causalRelTh3 = computeImplicitDependenciesByTheoremThree(craIncludingOnlyTh2);

		CausalAbstraction<E> craTh3Only = reduceCausalRelationsOfTheorem3(causalRelTh3);

		Collection<AlphaPairImpl<Collection<E>, Collection<E>>> alphaPairsTh3 = computeMaximumTransitionSetsBasedOnTheorem3(
				craTh3Only);

		removeAllAlphaPairsIncludingLengthOneLoops(alphaPairsTh1Th2, alphaPairsTh3, lengthOneLoopTriples);
		Collection<AlphaPairImpl<Collection<E>, Collection<E>>> l1lPlaces = createLengthOneLoopBasedAlphaPairs(
				lengthOneLoopTriples);

		Petrinet net = PetrinetFactory.newPetrinet("Petri net (Alpha)");
		Marking marking = new Marking();
		addTransitions(net);
		getProgress().inc();
		addPlaces(net, alphaPairsTh1Th2);
		addPlaces(net, alphaPairsTh3);
		addPlaces(net, l1lPlaces);
		getProgress().inc();
		addInitialPlace(net, getAbstraction().getStartActivityAbstraction(), marking);
		addFinalPlace(net, getAbstraction().getEndActivityAbstraction());
		getProgress().inc();
		return new Pair<Petrinet, Marking>(net, marking);
	}

	private Collection<AlphaPairImpl<Collection<E>, Collection<E>>> createLengthOneLoopBasedAlphaPairs(
			Collection<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> l1lTriples) {
		Collection<AlphaPairImpl<Collection<E>, Collection<E>>> result = new HashSet<>();
		for (AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> triple : l1lTriples) {
			AlphaPairImpl<Collection<E>, Collection<E>> correspondingPair = new AlphaPairImpl<Collection<E>, Collection<E>>(
					new HashSet<E>(triple.getFirst()), new HashSet<E>(triple.getSecond()), -1, -1);
			correspondingPair.getFirst().addAll(triple.getThird());
			correspondingPair.getSecond().addAll(triple.getThird());
			result.add(correspondingPair);
		}
		return result;
	}

}
