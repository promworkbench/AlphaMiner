package org.processmining.alphaminer.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

public class AlphaClassicNodeExpanderImpl<E> implements NodeExpander<AlphaPairImpl<Collection<E>, Collection<E>>> {

	private final E[] candidates;
	private final CausalAbstraction<E> cra;
	private final UnrelatedAbstraction<E> ua;
	private final boolean resultMayContainEmptySet;

	public AlphaClassicNodeExpanderImpl(CausalAbstraction<E> cra, UnrelatedAbstraction<E> ua,
			boolean resultMayContainEmptySet) {
		this(cra, ua, null, resultMayContainEmptySet);
	}

	public AlphaClassicNodeExpanderImpl(final CausalAbstraction<E> cra, final UnrelatedAbstraction<E> ua) {
		this(cra, ua, null, false);
	}

	public AlphaClassicNodeExpanderImpl(final CausalAbstraction<E> cra, final UnrelatedAbstraction<E> ua,
			final Collection<E> ignore) {
		this(cra, ua, ignore, false);
	}

	public AlphaClassicNodeExpanderImpl(final CausalAbstraction<E> cra, final UnrelatedAbstraction<E> ua,
			final Collection<E> ignore, boolean resultMayContainEmptySet) {
		this.cra = cra;
		this.ua = ua;
		candidates = createCandidateEventClasses(cra.getEventClasses(), ignore);
		this.resultMayContainEmptySet = resultMayContainEmptySet;
	}

	private E[] createCandidateEventClasses(E[] allEventClasses, Collection<E> ignore) {
		E[] result = allEventClasses.clone();
		if (ignore != null) {
			for (int i = 0; i < result.length; i++) {
				result[i] = ignore.contains(allEventClasses[i]) ? null : result[i];
			}
		}
		return result;
	}

	protected boolean canExpandLeft(AlphaPairImpl<Collection<E>, Collection<E>> toExpand, E toAdd) {
		if (toExpand.getFirst().contains(toAdd))
			return false;
		for (E right : toExpand.getSecond()) {
			if (cra.getValue(toAdd, right) < cra.getThreshold()) {
				return false;
			}
		}
		for (E left : toExpand.getFirst()) {
			if (ua.getValue(toAdd, left) < ua.getThreshold()) {
				return false;
			}
		}
		return true;
	}

	protected boolean canExpandRight(AlphaPairImpl<Collection<E>, Collection<E>> toExpand, E toAdd) {
		if (toExpand.getSecond().contains(toAdd))
			return false;
		for (E left : toExpand.getFirst()) {
			if (cra.getValue(left, toAdd) < cra.getThreshold()) {
				return false;
			}
		}
		for (E right : toExpand.getSecond()) {
			if (ua.getValue(right, toAdd) < ua.getThreshold()) {
				return false;
			}
		}
		return true;
	}

	protected AlphaPairImpl<Collection<E>, Collection<E>> expandLeft(E eventClass,
			AlphaPairImpl<Collection<E>, Collection<E>> toExpand) {
		AlphaPairImpl<Collection<E>, Collection<E>> result = null;
		if (eventClass != null && canExpandLeft(toExpand, eventClass)) {
			result = new AlphaPairImpl<Collection<E>, Collection<E>>(new HashSet<E>(toExpand.getFirst()),
					new HashSet<E>(toExpand.getSecond()), toExpand.getMaxIndexOfFirst(),
					toExpand.getMaxIndexOfSecond());
			result.getFirst().add(eventClass);
			result.setMaxIndexOfFirst(cra.getIndex(eventClass));
		}
		return result;
	}

	public Collection<AlphaPairImpl<Collection<E>, Collection<E>>> expandNode(
			AlphaPairImpl<Collection<E>, Collection<E>> toExpand, Progress progress,
			Collection<AlphaPairImpl<Collection<E>, Collection<E>>> unmodifiableResultCollection) {
		Collection<AlphaPairImpl<Collection<E>, Collection<E>>> pairs = new HashSet<>();

		int startIndex = toExpand.getMaxIndexOfFirst() + 1;
		for (int i = startIndex; i < candidates.length; i++) {
			AlphaPairImpl<Collection<E>, Collection<E>> expand = expandLeft(candidates[i], toExpand);
			if (expand != null) {
				pairs.add(expand);
			}
		}

		startIndex = toExpand.getMaxIndexOfSecond() + 1;
		for (int i = startIndex; i < candidates.length; i++) {
			AlphaPairImpl<Collection<E>, Collection<E>> expand = expandRight(candidates[i], toExpand);
			if (expand != null) {
				pairs.add(expand);
			}
		}
		return pairs;
	}

	protected AlphaPairImpl<Collection<E>, Collection<E>> expandRight(E eventClass,
			AlphaPairImpl<Collection<E>, Collection<E>> toExpand) {
		AlphaPairImpl<Collection<E>, Collection<E>> result = null;
		if (eventClass != null && canExpandRight(toExpand, eventClass)) {
			result = new AlphaPairImpl<Collection<E>, Collection<E>>(new HashSet<E>(toExpand.getFirst()),
					new HashSet<E>(toExpand.getSecond()), toExpand.getMaxIndexOfFirst(),
					toExpand.getMaxIndexOfSecond());
			result.getSecond().add(eventClass);
			result.setMaxIndexOfSecond(cra.getIndex(eventClass));
		}
		return result;
	}

	public E[] getCandidates() {
		return candidates;
	}

	public CausalAbstraction<E> getCra() {
		return cra;
	}

	public UnrelatedAbstraction<E> getUa() {
		return ua;
	}

	public void processLeaf(AlphaPairImpl<Collection<E>, Collection<E>> leaf, Progress progress,
			Collection<AlphaPairImpl<Collection<E>, Collection<E>>> resultCollection) {
		synchronized (resultCollection) {
			Iterator<AlphaPairImpl<Collection<E>, Collection<E>>> it = resultCollection.iterator();
			boolean largerFound = !resultMayContainEmptySet
					&& (leaf.getFirst().isEmpty() || leaf.getSecond().isEmpty());
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
