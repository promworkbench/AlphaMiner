package org.processmining.alphaminer.algorithms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.models.AlphaPairOfAlphaPairCollection;
import org.processmining.alphaminer.models.AlphaSharpConITDataModel;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

public class AlphaSharpNodeExpanderImpl<E>
		implements NodeExpander<AlphaPairImpl<Collection<Object>, Collection<Object>>> {

	private final CausalAbstraction<E> ca;
	private final DirectlyFollowsAbstraction<E> dfa;
	private final UnrelatedAbstraction<E> ua;
	private final AlphaSharpConITDataModel<E> conITResult;
	private final Object[] candidates;

	public AlphaSharpNodeExpanderImpl(CausalAbstraction<E> ca, DirectlyFollowsAbstraction<E> dfa,
			UnrelatedAbstraction<E> ua, AlphaSharpConITDataModel<E> conITResult) {
		super();
		this.ca = ca;
		this.dfa = dfa;
		this.ua = ua;
		this.conITResult = conITResult;
		candidates = new Object[ca.getEventClasses().length + conITResult.getInvisibleTransitions().size()];
		for (int i = 0; i < ca.getEventClasses().length; i++) {
			candidates[i] = ca.getEventClasses()[i];
		}
		int i = ca.getEventClasses().length;
		Iterator<AlphaPairOfAlphaPairCollection<E>> iterator = conITResult.getInvisibleTransitions().iterator();
		while (iterator.hasNext()) {
			candidates[i] = iterator.next();
			i++;
		}
	}

	public CausalAbstraction<E> getCausalAbstraction() {
		return ca;
	}

	public DirectlyFollowsAbstraction<E> getDirectlyFollowsAbstraction() {
		return dfa;
	}

	public UnrelatedAbstraction<E> getUnrelatedAbstraction() {
		return ua;
	}

	public AlphaSharpConITDataModel<E> getConITResult() {
		return conITResult;
	}

	public Collection<AlphaPairImpl<Collection<Object>, Collection<Object>>> expandNode(
			AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand, Progress progress,
			Collection<AlphaPairImpl<Collection<Object>, Collection<Object>>> unmodifiableResultCollection) {
		Collection<AlphaPairImpl<Collection<Object>, Collection<Object>>> pairs = new HashSet<>();

		int startIndex = toExpand.getMaxIndexOfFirst() + 1;
		for (int i = startIndex; i < candidates.length; i++) {
			AlphaPairImpl<Collection<Object>, Collection<Object>> expand = expandLeft(candidates[i], toExpand);
			if (expand != null) {
				pairs.add(expand);
			}
		}

		startIndex = toExpand.getMaxIndexOfSecond() + 1;
		for (int i = startIndex; i < candidates.length; i++) {
			AlphaPairImpl<Collection<Object>, Collection<Object>> expand = expandRight(candidates[i], toExpand);
			if (expand != null) {
				pairs.add(expand);
			}
		}
		return pairs;
	}

	protected AlphaPairImpl<Collection<Object>, Collection<Object>> expandLeft(Object eventClass,
			AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand) {
		AlphaPairImpl<Collection<Object>, Collection<Object>> result = null;
		if (eventClass != null && canExpandLeft(toExpand, eventClass)) {
			result = new AlphaPairImpl<Collection<Object>, Collection<Object>>(new HashSet<Object>(toExpand.getFirst()),
					new HashSet<Object>(toExpand.getSecond()), toExpand.getMaxIndexOfFirst(),
					toExpand.getMaxIndexOfSecond());
			result.getFirst().add(eventClass);
			result.setMaxIndexOfFirst(Arrays.asList(candidates).indexOf(eventClass));
		}
		return result;
	}

	protected AlphaPairImpl<Collection<Object>, Collection<Object>> expandRight(Object eventClass,
			AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand) {
		AlphaPairImpl<Collection<Object>, Collection<Object>> result = null;
		if (eventClass != null && canExpandRight(toExpand, eventClass)) {
			result = new AlphaPairImpl<Collection<Object>, Collection<Object>>(new HashSet<Object>(toExpand.getFirst()),
					new HashSet<Object>(toExpand.getSecond()), toExpand.getMaxIndexOfFirst(),
					toExpand.getMaxIndexOfSecond());
			result.getSecond().add(eventClass);
			result.setMaxIndexOfSecond(Arrays.asList(candidates).indexOf(eventClass));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected boolean canExpandRight(AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand, Object toAdd) {
		if (toExpand.getSecond().contains(toAdd))
			return false;
		if (toAdd instanceof AlphaPairOfAlphaPairCollection) {
			return canExpandRightInvisible(toExpand, (AlphaPairOfAlphaPairCollection<?>) toAdd);
		} else {
			return canExpandRightVisible(toExpand, (E) toAdd);
		}
	}

	@SuppressWarnings("unchecked")
	protected boolean canExpandLeft(AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand, Object toAdd) {
		if (toExpand.getFirst().contains(toAdd))
			return false;
		// to add is "invisible"?
		if (toAdd instanceof AlphaPairOfAlphaPairCollection) {
			return canExpandLeftInvisible(toExpand, (AlphaPairOfAlphaPairCollection<?>) toAdd);
		} else {
			return canExpandLeftVisible(toExpand, (E) toAdd);
		}
	}

	@SuppressWarnings("unchecked")
	protected boolean canExpandLeftVisible(AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand, E toAdd) {
		for (Object b : toExpand.getSecond()) {
			if (b instanceof AlphaPairOfAlphaPairCollection) {
				if (!checkSequentialMandaciousDependency(toAdd, b)) {
					return false;
				}
			} else {
				if (!ca.holds(toAdd, (E) b)) {
					return false;
				}
			}
			aLoop: for (Object a : toExpand.getFirst()) {
				if (a instanceof AlphaPairOfAlphaPairCollection) {
					if (isParallelMendaciousDependency(a, toAdd)) {
						return false;
					}
				} else {
					if (ua.holds((E) a, toAdd))
						continue aLoop;
					else if (ca.holds((E) a, toAdd) && dfa.holds(toAdd, toAdd))
						continue aLoop;
					else if (ca.holds(toAdd, (E) a) && dfa.holds((E) a, (E) a))
						continue aLoop;
					else
						return false;
				}
			}
		}
		return true;

	}

	@SuppressWarnings("unchecked")
	protected boolean canExpandRightVisible(AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand, E toAdd) {
		for (Object a : toExpand.getFirst()) {
			if (a instanceof AlphaPairOfAlphaPairCollection) {
				if (!checkSequentialMandaciousDependency(a, toAdd))
					return false;
			} else {
				if (!ca.holds((E) a, toAdd))
					return false;
			}
		}
		bLoop: for (Object b : toExpand.getSecond()) {
			if (b instanceof AlphaPairOfAlphaPairCollection) {
				if (isParallelMendaciousDependency(b, toAdd))
					return false;
			} else {
				if (ua.holds((E) b, toAdd))
					continue bLoop;
				else if (ca.holds((E) b, toAdd) && dfa.holds((E) b, (E) b))
					continue bLoop;
				else if (ca.holds(toAdd, (E) b) && dfa.holds(toAdd, toAdd))
					continue bLoop;
				else
					return false;
			}
		}
		return true;
	}

	protected boolean isParallelMendaciousDependency(Object from, Object to) {
		for (Pair<Object, Object> mDependency : conITResult.getParallelDependencies()) {
			if ((mDependency.getFirst().equals(from) && mDependency.getSecond().equals(to))
					|| (mDependency.getFirst().equals(to) && mDependency.getSecond().equals(from))) {
				return true;
			}
		}
		return false;
	}

	protected boolean checkSequentialMandaciousDependency(Object from, Object to) {
		for (Pair<Object, Object> mDependency : conITResult.getSequentialDependencies()) {
			if (mDependency.getSecond().equals(to) && mDependency.getFirst().equals(from)) {
				return true;
			}
		}
		return false;
	}

	protected boolean canExpandLeftInvisible(AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand,
			AlphaPairOfAlphaPairCollection<?> toAdd) {
		for (Object b : toExpand.getSecond()) {
			if (!checkSequentialMandaciousDependency(toAdd, b)) {
				return false;
			}
		}
		for (Object a : toExpand.getFirst()) {
			if (isParallelMendaciousDependency(a, toAdd))
				return false;
		}
		return true;
	}

	protected boolean canExpandRightInvisible(AlphaPairImpl<Collection<Object>, Collection<Object>> toExpand,
			AlphaPairOfAlphaPairCollection<?> toAdd) {
		for (Object a : toExpand.getFirst()) {
			if (!checkSequentialMandaciousDependency(a, toAdd)) {
				return false;
			}
		}
		for (Object b : toExpand.getSecond()) {
			if (isParallelMendaciousDependency(b, toAdd)) {
				return false;
			}
		}
		return true;
	}

	public void processLeaf(AlphaPairImpl<Collection<Object>, Collection<Object>> leaf, Progress progress,
			Collection<AlphaPairImpl<Collection<Object>, Collection<Object>>> resultCollection) {
		synchronized (resultCollection) {
			Iterator<AlphaPairImpl<Collection<Object>, Collection<Object>>> it = resultCollection.iterator();
			boolean largerFound = false;
			while (!largerFound && it.hasNext()) {
				AlphaPairImpl<Collection<Object>, Collection<Object>> t = it.next();
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
