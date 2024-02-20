package org.processmining.alphaminer.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.alphaminer.abstractions.AlphaPlusPlusAbstraction;
import org.processmining.alphaminer.models.AlphaTripleImpl;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.search.NodeExpander;

public class AlphaPlusPlusL1LNodeExpanderImpl<E>
		implements NodeExpander<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> {

	private final AlphaPlusPlusAbstraction<E> abstraction;

	public AlphaPlusPlusL1LNodeExpanderImpl(AlphaPlusPlusAbstraction<E> abstraction) {
		this.abstraction = abstraction;
	}

	protected boolean canExpandFirst(E eventClass,
			AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> toExpand) {
		if (abstraction.getLengthOneLoopAbstraction().holds(eventClass))
			return false;
		for (E c : toExpand.getThird()) {
			if (!abstraction.getDirectlyFollowsAbstraction().holds(eventClass, c)
					|| abstraction.getLengthTwoLoopAbstraction().holds(c, eventClass)) {
				return false;
			}
		}
		for (E b : toExpand.getSecond()) {
			if (abstraction.getParallelAbstraction().holds(eventClass, b))
				return false;
		}
		for (E a : toExpand.getFirst()) {
			if (!abstraction.getUnrelatedAbstraction().holds(eventClass, a))
				return false;
		}
		return true;
	}

	protected boolean canExpandSecond(E eventClass,
			AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> toExpand) {
		if (abstraction.getLengthOneLoopAbstraction().holds(eventClass))
			return false;
		for (E c : toExpand.getThird()) {
			if (!abstraction.getDirectlyFollowsAbstraction().holds(c, eventClass)
					|| abstraction.getLengthTwoLoopAbstraction().holds(c, eventClass))
				return false;
		}
		for (E a : toExpand.getFirst()) {
			if (abstraction.getParallelAbstraction().holds(eventClass, a))
				return false;
		}
		for (E b : toExpand.getSecond()) {
			if (!abstraction.getUnrelatedAbstraction().holds(eventClass, b))
				return false;
		}
		return true;
	}

	protected boolean canExpandThird(E eventClass,
			AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> toExpand) {
		if (!abstraction.getLengthOneLoopAbstraction().holds(eventClass))
			return false;
		for (E a : toExpand.getFirst()) {
			if (!abstraction.getDirectlyFollowsAbstraction().holds(a, eventClass)
					|| abstraction.getLengthTwoLoopAbstraction().holds(eventClass, a))
				return false;

		}
		for (E b : toExpand.getThird()) {
			if (!abstraction.getDirectlyFollowsAbstraction().holds(eventClass, b)
					|| abstraction.getLengthTwoLoopAbstraction().holds(eventClass, b))
				return false;
		}
		return true;
	}

	protected AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> expandFirst(E eventClass,
			AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> toExpand) {
		AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> res = null;
		if (canExpandFirst(eventClass, toExpand)) {
			Collection<E> first = new HashSet<E>(toExpand.getFirst());
			first.add(eventClass);
			Collection<E> second = new HashSet<E>(toExpand.getSecond());
			Collection<E> third = new HashSet<E>(toExpand.getThird());
			res = new AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>(first, second, third,
					abstraction.getIndex(eventClass), toExpand.getMaxIndexOfSecond(), toExpand.getMaxIndexOfThird());
		}
		return res;
	}

	public Collection<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> expandNode(
			AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> toExpand, Progress progress,
			Collection<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> unmodifiableResultCollection) {
		Collection<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> res = new HashSet<>();
		AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> expand;
		int start = toExpand.getMaxIndexOfFirst() + 1;
		for (int i = start; i < abstraction.getEventClasses().length; i++) {
			if ((expand = expandFirst(abstraction.getEventClasses()[i], toExpand)) != null) {
				res.add(expand);
			}
		}
		start = toExpand.getMaxIndexOfSecond() + 1;
		for (int i = start; i < abstraction.getEventClasses().length; i++) {
			if ((expand = expandSecond(abstraction.getEventClasses()[i], toExpand)) != null) {
				res.add(expand);
			}
		}

		start = toExpand.getMaxIndexOfThird() + 1;
		for (int i = start; i < abstraction.getEventClasses().length; i++) {
			if ((expand = expandThird(abstraction.getEventClasses()[i], toExpand)) != null) {
				res.add(expand);
			}
		}
		return res;
	}

	protected AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> expandSecond(E eventClass,
			AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> toExpand) {
		AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> res = null;
		if (canExpandSecond(eventClass, toExpand)) {
			Set<E> first = new HashSet<>(toExpand.getFirst());
			Set<E> second = new HashSet<>(toExpand.getSecond());
			second.add(eventClass);
			Set<E> third = new HashSet<>(toExpand.getThird());
			res = new AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>(first, second, third,
					toExpand.getMaxIndexOfFirst(), abstraction.getIndex(eventClass), toExpand.getMaxIndexOfThird());
		}
		return res;
	}

	protected AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> expandThird(E eventClass,
			AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> toExpand) {
		AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> res = null;
		if (canExpandThird(eventClass, toExpand)) {
			Set<E> first = new HashSet<>(toExpand.getFirst());
			Set<E> second = new HashSet<>(toExpand.getSecond());
			Set<E> third = new HashSet<>(toExpand.getThird());
			third.add(eventClass);
			res = new AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>(first, second, third,
					toExpand.getMaxIndexOfFirst(), toExpand.getMaxIndexOfSecond(), abstraction.getIndex(eventClass));
		}
		return res;
	}

	public void processLeaf(AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> leaf, Progress progress,
			Collection<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> resultCollection) {
		synchronized (resultCollection) {
			Iterator<AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>>> it = resultCollection.iterator();
			boolean largerFound = false;
			while (!largerFound && it.hasNext()) {
				AlphaTripleImpl<Collection<E>, Collection<E>, Collection<E>> t = it.next();
				// check "t is smaller than leaf"
				if (leaf.getFirst().containsAll(t.getFirst()) && leaf.getSecond().containsAll(t.getSecond())
						&& leaf.getThird().containsAll(t.getThird())) {
					it.remove();
					continue;
				}
				largerFound = t.getFirst().containsAll(leaf.getFirst()) && t.getSecond().containsAll(leaf.getSecond())
						&& t.getThird().containsAll(leaf.getThird());
			}
			if (!largerFound) {
				resultCollection.add(leaf);
			}
		}
	}
}
