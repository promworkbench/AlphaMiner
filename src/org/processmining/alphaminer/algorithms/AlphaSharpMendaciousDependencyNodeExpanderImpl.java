package org.processmining.alphaminer.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.models.AlphaPairOfAlphaPairCollection;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.logabstractions.models.MendaciousAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;

public class AlphaSharpMendaciousDependencyNodeExpanderImpl<E>
		implements NodeExpander<AlphaPairOfAlphaPairCollection<E>> {

	private final MendaciousAbstraction<E> ma;
	private final ParallelAbstraction<E> pa;
	private final List<AlphaPairImpl<Collection<E>, Collection<E>>> postSetsList = new ArrayList<>();
	private final List<AlphaPairImpl<Collection<E>, Collection<E>>> preSetsList = new ArrayList<>();

	public AlphaSharpMendaciousDependencyNodeExpanderImpl(MendaciousAbstraction<E> ma, ParallelAbstraction<E> pa,
			Map<E, Collection<AlphaPairImpl<Collection<E>, Collection<E>>>> postSets,
			Map<E, Collection<AlphaPairImpl<Collection<E>, Collection<E>>>> preSets) {
		this.ma = ma;
		this.pa = pa;
		for (E e : preSets.keySet()) {
			preSetsList.addAll(preSets.get(e));
		}
		for (E e : postSets.keySet()) {
			postSetsList.addAll(postSets.get(e));
		}
	}

	public Collection<AlphaPairOfAlphaPairCollection<E>> expandNode(AlphaPairOfAlphaPairCollection<E> toExpand,
			Progress progress, Collection<AlphaPairOfAlphaPairCollection<E>> unmodifiableResultCollection) {
		Collection<AlphaPairOfAlphaPairCollection<E>> expanded = new HashSet<>();
		AlphaPairOfAlphaPairCollection<E> newPair;
		int startIndex = toExpand.getMaxIndexOfFirst() + 1;
		for (int i = startIndex; i < postSetsList.size(); i++) {
			if ((newPair = expandLeft(postSetsList.get(i), toExpand, i)) != null) {
				expanded.add(newPair);
			}
		}

		startIndex = toExpand.getMaxIndexOfSecond() + 1;
		for (int i = startIndex; i < preSetsList.size(); i++) {
			if ((newPair = expandRight(preSetsList.get(i), toExpand, i)) != null) {
				expanded.add(newPair);
			}
		}
		return expanded;
	}

	protected AlphaPairOfAlphaPairCollection<E> expandLeft(AlphaPairImpl<Collection<E>, Collection<E>> AX,
			AlphaPairOfAlphaPairCollection<E> toExpand, int index) {
		AlphaPairOfAlphaPairCollection<E> pair = null;
		if (canExpandLeft(AX, toExpand)) {
			pair = new AlphaPairOfAlphaPairCollection<>(toExpand.getFirst(), toExpand.getSecond(), index,
					toExpand.getMaxIndexOfSecond());
			pair.getFirst().add(AX);
		}
		return pair;
	}

	protected AlphaPairOfAlphaPairCollection<E> expandRight(AlphaPairImpl<Collection<E>, Collection<E>> YB,
			AlphaPairOfAlphaPairCollection<E> toExpand, int index) {
		AlphaPairOfAlphaPairCollection<E> pair = null;
		if (canExpandRight(YB, toExpand)) {
			pair = new AlphaPairOfAlphaPairCollection<>(toExpand.getFirst(), toExpand.getSecond(),
					toExpand.getMaxIndexOfFirst(), index);
			pair.getSecond().add(YB);
		}
		return pair;
	}

	protected boolean canExpandRight(AlphaPairImpl<Collection<E>, Collection<E>> YB,
			AlphaPairOfAlphaPairCollection<E> toExpand) {
		if (toExpand.getSecond().contains(YB))
			return false;
		if (!checkMendaciousYB(YB, toExpand))
			return false;
		if (!checkNonParallelismYBtoAX(YB, toExpand))
			return false;
		if (!checkParallelismExistsInYB(YB, toExpand))
			return false;
		return true;
	}

	protected boolean canExpandLeft(AlphaPairImpl<Collection<E>, Collection<E>> AX,
			AlphaPairOfAlphaPairCollection<E> toExpand) {
		if (toExpand.getFirst().contains(AX))
			return false;
		if (!checkMendaciousAX(AX, toExpand))
			return false;
		if (!checkNonParallelismAXtoYB(AX, toExpand))
			return false;
		if (!checkParallelismExistsInAX(AX, toExpand))
			return false;
		return true;
	}

	protected boolean checkMendaciousAX(AlphaPairImpl<Collection<E>, Collection<E>> AX,
			AlphaPairOfAlphaPairCollection<E> toExpand) {
		for (E a : AX.getFirst()) {
			for (AlphaPairImpl<Collection<E>, Collection<E>> YB : toExpand.getSecond()) {
				for (E b : YB.getSecond()) {
					if (!ma.holds(a, b))
						return false;
				}
			}
		}
		return true;
	}

	protected boolean checkMendaciousYB(AlphaPairImpl<Collection<E>, Collection<E>> YB,
			AlphaPairOfAlphaPairCollection<E> toExpand) {
		for (E b : YB.getSecond()) {
			for (AlphaPairImpl<Collection<E>, Collection<E>> AX : toExpand.getFirst()) {
				for (E a : AX.getFirst()) {
					if (!ma.holds(a, b)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected boolean checkNonParallelismAXtoYB(AlphaPairImpl<Collection<E>, Collection<E>> AX,
			AlphaPairOfAlphaPairCollection<E> toExpand) {
		// check x in X from (A,X) not parallel with any y in (Y,B)
		for (AlphaPairImpl<Collection<E>, Collection<E>> YB : toExpand.getSecond()) {
			for (E y : YB.getFirst()) {
				for (E x : AX.getSecond()) {
					if (pa.holds(x, y))
						return false;
				}
			}
		}
		return true;
	}

	protected boolean checkNonParallelismYBtoAX(AlphaPairImpl<Collection<E>, Collection<E>> YB,
			AlphaPairOfAlphaPairCollection<E> toExpand) {
		// check y in Y from (Y,B) not parallel with any x in any (A,X)
		for (AlphaPairImpl<Collection<E>, Collection<E>> AX : toExpand.getFirst()) {
			for (E x : AX.getSecond()) {
				for (E y : YB.getFirst()) {
					if (pa.holds(x, y)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected boolean checkParallelismExistsInAX(AlphaPairImpl<Collection<E>, Collection<E>> AX,
			AlphaPairOfAlphaPairCollection<E> toExpand) {
		// check parallelism among other firsts
		for (AlphaPairImpl<Collection<E>, Collection<E>> otherAX : toExpand.getFirst()) {
			if (!otherAX.equals(AX)) {
				boolean found = false;
				loop: for (E a1 : AX.getFirst()) {
					for (E a2 : otherAX.getFirst()) {
						if (pa.holds(a1, a2)) {
							found = true;
							break loop;
						}
					}
				}
				if (!found)
					return false;
			}
		}
		return true;
	}

	protected boolean checkParallelismExistsInYB(AlphaPairImpl<Collection<E>, Collection<E>> YB,
			AlphaPairOfAlphaPairCollection<E> toExpand) {
		// check parallelism among other YB's
		for (AlphaPairImpl<Collection<E>, Collection<E>> otherYB : toExpand.getSecond()) {
			if (!otherYB.equals(YB)) {
				boolean found = false;
				loop: for (E b1 : YB.getSecond()) {
					for (E b2 : otherYB.getSecond()) {
						if (pa.holds(b1, b2)) {
							found = true;
							break loop;
						}
					}
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;
	}

	public void processLeaf(AlphaPairOfAlphaPairCollection<E> leaf, Progress progress,
			Collection<AlphaPairOfAlphaPairCollection<E>> resultCollection) {
		synchronized (resultCollection) {
			Iterator<AlphaPairOfAlphaPairCollection<E>> it = resultCollection.iterator();
			boolean largerFound = !allSetsInResultNonEmpty(leaf);
			while (!largerFound && it.hasNext()) {
				AlphaPairOfAlphaPairCollection<E> t = it.next();
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

	protected boolean allSetsInResultNonEmpty(AlphaPairOfAlphaPairCollection<E> leaf) {
		for (AlphaPairImpl<Collection<E>, Collection<E>> pair : leaf.getFirst()) {
			if (pair.getFirst().isEmpty() || pair.getSecond().isEmpty()) {
				return false;
			}
		}
		for (AlphaPairImpl<Collection<E>, Collection<E>> pair : leaf.getSecond()) {
			if (pair.getFirst().isEmpty() || pair.getSecond().isEmpty()) {
				return false;
			}
		}
		return true;
	}

}
