package org.processmining.alphaminer.algorithms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.models.Union;
import org.processmining.alphaminer.models.UnionImpl;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.LongTermFollowsAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

/**
 * Assumes one causal relation abstraction including both the results of th. 1.
 * and th. 2. Does only check for "unrelated" option for members of the same
 * set. (see code, change indicated, lines 54-56 and 72-74))
 * 
 * @author svzelst
 *
 * @param <E>
 */
public class AlphaPlusPlusTh1Th2NodeExpanderImproved1Impl<E>
		implements NodeExpander<AlphaPairImpl<Union<E>, Union<E>>> {

	private final CausalAbstraction<E> cra;
	private final UnrelatedAbstraction<E> ua;
	//	private final LongTermFollowsAbstraction<E> ltfa;
	private final E[] candidates;

	public AlphaPlusPlusTh1Th2NodeExpanderImproved1Impl(final CausalAbstraction<E> cra,
			UnrelatedAbstraction<E> ua, LongTermFollowsAbstraction<E> ltfa, final Collection<E> ignore) {
		this.cra = cra;
		this.ua = ua;
		//		this.ltfa = ltfa;
		candidates = Arrays.copyOf(cra.getEventClasses(), cra.getEventClasses().length);
		for (int i = 0; i < candidates.length; i++) {
			candidates[i] = ignore.contains(candidates[i]) ? null : candidates[i];
		}
	}

	protected boolean canExpandLeft(E e, AlphaPairImpl<Union<E>, Union<E>> toExpand) {
		if (toExpand.getFirst().getLeft().contains(e))
			return false;
		for (E b : toExpand.getSecond()) {
			if (!cra.holds(e, b)) {
				return false;
			}
		}
		for (E a : toExpand.getFirst().getLeft()) {
			// change w.r.t. original algorithmic description
			// if (!ua.holds(e, a) || ltfa.holds(e, a)) {
			if (!ua.holds(e, a)) {
				return false;
			}
		}
		return true;
	}

	protected boolean canExpandRight(E e, AlphaPairImpl<Union<E>, Union<E>> toExpand) {
		if (toExpand.getSecond().getLeft().contains(e))
			return false;
		for (E a : toExpand.getFirst()) {
			if (!cra.holds(a, e)) {
				return false;
			}
		}
		for (E b : toExpand.getSecond().getLeft()) {
			// change w.r.t. original algorithmic description
			// if (!ua.holds(b, e) || ltfa.holds(b, e)) {
			if (!ua.holds(b, e)) {
				return false;
			}
		}
		return true;
	}

	protected AlphaPairImpl<Union<E>, Union<E>> expandLeft(E e, AlphaPairImpl<Union<E>, Union<E>> toExpand) {
		AlphaPairImpl<Union<E>, Union<E>> result = null;
		if (e != null && canExpandLeft(e, toExpand)) {
			Union<E> first = new UnionImpl<>(toExpand.getFirst().getLeft(), toExpand.getFirst().getRight());
			first.getRight().add(e);
			Union<E> second = new UnionImpl<>(toExpand.getSecond().getLeft(), toExpand.getSecond().getRight());
			result = new AlphaPairImpl<Union<E>, Union<E>>(first, second, cra.getIndex(e),
					toExpand.getMaxIndexOfSecond());
		}
		return result;
	}

	public Collection<AlphaPairImpl<Union<E>, Union<E>>> expandNode(AlphaPairImpl<Union<E>, Union<E>> toExpand,
			Progress progress, Collection<AlphaPairImpl<Union<E>, Union<E>>> unmodifiableResultCollection) {
		Collection<AlphaPairImpl<Union<E>, Union<E>>> newPairs = new HashSet<>();

		int startIndex = toExpand.getMaxIndexOfFirst() + 1;
		for (int i = startIndex; i < candidates.length; i++) {
			AlphaPairImpl<Union<E>, Union<E>> expand = expandLeft(candidates[i], toExpand);
			if (expand != null) {
				newPairs.add(expand);
			}
		}

		startIndex = toExpand.getMaxIndexOfSecond() + 1;
		for (int i = startIndex; i < candidates.length; i++) {
			AlphaPairImpl<Union<E>, Union<E>> expand = expandRight(candidates[i], toExpand);
			if (expand != null) {
				newPairs.add(expand);
			}
		}
		return newPairs;
	}

	protected AlphaPairImpl<Union<E>, Union<E>> expandRight(E e, AlphaPairImpl<Union<E>, Union<E>> toExpand) {
		AlphaPairImpl<Union<E>, Union<E>> result = null;
		if (e != null && canExpandRight(e, toExpand)) {
			Union<E> first = new UnionImpl<>(toExpand.getFirst().getLeft(), toExpand.getFirst().getRight());
			Union<E> second = new UnionImpl<>(toExpand.getSecond().getLeft(), toExpand.getSecond().getRight());
			second.getRight().add(e);
			result = new AlphaPairImpl<Union<E>, Union<E>>(first, second, toExpand.getMaxIndexOfFirst(),
					cra.getIndex(e));
		}
		return result;
	}

	public void processLeaf(AlphaPairImpl<Union<E>, Union<E>> leaf, Progress progress,
			Collection<AlphaPairImpl<Union<E>, Union<E>>> resultCollection) {
		synchronized (resultCollection) {
			Iterator<AlphaPairImpl<Union<E>, Union<E>>> it = resultCollection.iterator();
			boolean largerFound = false;
			while (!largerFound && it.hasNext()) {
				AlphaPairImpl<Union<E>, Union<E>> t = it.next();
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
