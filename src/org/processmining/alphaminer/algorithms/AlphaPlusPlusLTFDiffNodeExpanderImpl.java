package org.processmining.alphaminer.algorithms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.LongTermFollowsAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;

/**
 * Node Expander for theorem 3 of paper:
 * "Detecting Implicit Dependencies Between Tasks from Event Logs"; Lijie Wen,
 * Jianmin Wang, and Jiaguang Sun
 * 
 * @author svzelst
 *
 * @param <E>
 */
public class AlphaPlusPlusLTFDiffNodeExpanderImpl<E>
		implements NodeExpander<AlphaPairImpl<Collection<E>, Collection<E>>> {

	private final E a;
	private final E b;
	private final E[] candidates;
	private final CausalPrecedenceAbstraction<E> cpa;
	private final LongTermFollowsAbstraction<E> ltfa;
	private final ParallelAbstraction<E> pa;

	public AlphaPlusPlusLTFDiffNodeExpanderImpl(LongTermFollowsAbstraction<E> ltfa, CausalPrecedenceAbstraction<E> cpa,
			ParallelAbstraction<E> pa, E a, E b, Collection<E> ignore) {
		this.ltfa = ltfa;
		this.cpa = cpa;
		this.pa = pa;
		this.a = a;
		this.b = b;
		candidates = Arrays.copyOf(ltfa.getEventClasses(), ltfa.getEventClasses().length);
		for (int i = 0; i < candidates.length; i++) {
			candidates[i] = ignore.contains(candidates[i]) ? null : candidates[i];
		}
	}

	protected boolean canExpandLeft(E a1, AlphaPairImpl<Collection<E>, Collection<E>> toExpand) {
		if (a == null)
			return false;
		if (toExpand.getFirst().contains(a1)) {
			return false;
		}
		if (!ltfa.holds(a, a1) || ltfa.holds(b, a1))
			return false;
		if (!toExpand.getSecond().isEmpty()) {
			boolean a1PrecedesWithAtLeastOneBinB = false;
			for (E b1 : toExpand.getSecond()) {
				if (cpa.holds(b1, a1)) {
					a1PrecedesWithAtLeastOneBinB = true;
					break;
				}
			}
			if (!a1PrecedesWithAtLeastOneBinB)
				return false;
		}
		for (E a2 : toExpand.getFirst()) {
			if (!pa.holds(a1, a2))
				return false;
		}
		return true;
	}

	protected boolean canExpandRight(E b1, AlphaPairImpl<Collection<E>, Collection<E>> toExpand) {
		if (b1 == null)
			return false;
		if (toExpand.getSecond().contains(b1)) {
			return false;
		}
		if (ltfa.holds(a, b1) || !ltfa.holds(b, b1))
			return false;
		if (!toExpand.getFirst().isEmpty()) {
			boolean b1IsPrecededByAtLeastOneAinA = false;
			for (E a1 : toExpand.getFirst()) {
				if (cpa.holds(a1, b1)) {
					b1IsPrecededByAtLeastOneAinA = true;
					break;
				}
			}
			if (!b1IsPrecededByAtLeastOneAinA)
				return false;
		}
		for (E b2 : toExpand.getSecond()) {
			if (!pa.holds(b1, b2)) {
				return false;
			}
		}
		return true;
	}

	protected AlphaPairImpl<Collection<E>, Collection<E>> expandLeft(E a1,
			AlphaPairImpl<Collection<E>, Collection<E>> toExpand) {
		AlphaPairImpl<Collection<E>, Collection<E>> newPair = null;
		if (canExpandLeft(a1, toExpand)) {
			newPair = new AlphaPairImpl<Collection<E>, Collection<E>>(new HashSet<>(toExpand.getFirst()),
					new HashSet<>(toExpand.getSecond()), ltfa.getIndex(a1), toExpand.getMaxIndexOfSecond());
			newPair.getFirst().add(a1);
		}
		return newPair;
	}

	public Collection<AlphaPairImpl<Collection<E>, Collection<E>>> expandNode(
			AlphaPairImpl<Collection<E>, Collection<E>> toExpand, Progress progress,
			Collection<AlphaPairImpl<Collection<E>, Collection<E>>> unmodifiableResultCollection) {
		Collection<AlphaPairImpl<Collection<E>, Collection<E>>> expand = new HashSet<>();
		int startIndex = toExpand.getMaxIndexOfFirst() + 1;
		for (int i = startIndex; i < candidates.length; i++) {
			AlphaPairImpl<Collection<E>, Collection<E>> pair = expandLeft(candidates[i], toExpand);
			if (pair != null) {
				expand.add(pair);
			}
		}

		startIndex = toExpand.getMaxIndexOfSecond() + 1;
		for (int i = startIndex; i < candidates.length; i++) {
			AlphaPairImpl<Collection<E>, Collection<E>> pair = expandRight(candidates[i], toExpand);
			if (pair != null) {
				expand.add(pair);
			}
		}
		return expand;
	}

	protected AlphaPairImpl<Collection<E>, Collection<E>> expandRight(E b1,
			AlphaPairImpl<Collection<E>, Collection<E>> toExpand) {
		AlphaPairImpl<Collection<E>, Collection<E>> newPair = null;
		if (canExpandRight(b1, toExpand)) {
			newPair = new AlphaPairImpl<Collection<E>, Collection<E>>(new HashSet<>(toExpand.getFirst()),
					new HashSet<>(toExpand.getSecond()), toExpand.getMaxIndexOfFirst(), ltfa.getIndex(b1));
			newPair.getSecond().add(b1);
		}
		return newPair;
	}

	public void processLeaf(AlphaPairImpl<Collection<E>, Collection<E>> leaf, Progress progress,
			Collection<AlphaPairImpl<Collection<E>, Collection<E>>> resultCollection) {
		synchronized (resultCollection) {
			Iterator<AlphaPairImpl<Collection<E>, Collection<E>>> it = resultCollection.iterator();
			boolean largerFound = false;
			while (!largerFound && it.hasNext()) {
				AlphaPairImpl<Collection<E>, Collection<E>> t = it.next();
				// check "t is smaller than leaf"
				if (leaf.getFirst().containsAll(t.getFirst()) && leaf.getSecond().containsAll(t.getSecond())) {
					it.remove();
					continue;
				}
				largerFound = t.getFirst().containsAll(leaf.getFirst()) && t.getSecond().containsAll(leaf.getSecond());
			}
			if (!largerFound) {
				resultCollection.add(leaf);
			}
		}
	}

}
