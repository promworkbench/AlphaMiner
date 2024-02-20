package org.processmining.alphaminer.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.models.AlphaPairOfAlphaPairCollection;
import org.processmining.framework.plugin.Progress;
import org.processmining.logabstractions.models.MendaciousAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;

import com.google.common.collect.Sets;

public class AlphaSharpMendaciousDependencyRedundantNodeExpanderImpl<E>
		extends AlphaSharpMendaciousDependencyNodeExpanderImpl<E> {

	private final Collection<AlphaPairOfAlphaPairCollection<E>> nonRedundant;
	private Collection<AlphaPairOfAlphaPairCollection<E>> redundantCandidates = null;

	public AlphaSharpMendaciousDependencyRedundantNodeExpanderImpl(MendaciousAbstraction<E> ma,
			ParallelAbstraction<E> pa, Map<E, Collection<AlphaPairImpl<Collection<E>, Collection<E>>>> postSets,
			Map<E, Collection<AlphaPairImpl<Collection<E>, Collection<E>>>> preSets,
			Collection<AlphaPairOfAlphaPairCollection<E>> nonRedundant) {
		super(ma, pa, postSets, preSets);
		this.nonRedundant = nonRedundant;
	}

	public void processLeaf(AlphaPairOfAlphaPairCollection<E> leaf, Progress progress,
			Collection<AlphaPairOfAlphaPairCollection<E>> resultCollection) {
		synchronized (resultCollection) {
			Iterator<AlphaPairOfAlphaPairCollection<E>> it = resultCollection.iterator();
			boolean largerFound = !super.allSetsInResultNonEmpty(leaf);
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
				// check for path in nonRedundant collection together with current collection.
				Collection<AlphaPairOfAlphaPairCollection<E>> union = new HashSet<>(nonRedundant);
				union.addAll(redundantCandidates);
				Collection<List<AlphaPairOfAlphaPairCollection<E>>> lists = new HashSet<>();
				Set<AlphaPairImpl<Collection<E>, Collection<E>>> leafPInSet, leafPOutSet, altPInSet, altPOutSet;
				leafPInSet = new HashSet<>(leaf.getFirst());
				leafPOutSet = new HashSet<>(leaf.getSecond());

				for (AlphaPairOfAlphaPairCollection<E> p1 : union) {
					if (p1.equals(leaf))
						continue;
					altPInSet = new HashSet<>(p1.getFirst());
					altPOutSet = new HashSet<>(p1.getSecond());
					if (!Sets.intersection(leafPInSet, altPInSet).isEmpty()) {
						for (AlphaPairOfAlphaPairCollection<E> p2 : union) {
							if (p2.equals(leaf) || p2.equals(p1)) {
								continue;
							}
							altPInSet = new HashSet<>(p2.getFirst());
							if (!Sets.intersection(altPOutSet, altPInSet).isEmpty()) {
								List<AlphaPairOfAlphaPairCollection<E>> candidateList = new ArrayList<>();
								candidateList.add(p1);
								candidateList.add(p2);
								lists.add(candidateList);
							}
						}
					}
				}
				boolean nonEmptySequence = false;
				connecting: while (!lists.isEmpty() && !nonEmptySequence) {
					for (List<AlphaPairOfAlphaPairCollection<E>> list : lists) {
						altPOutSet = new HashSet<>(list.get(list.size() - 1).getSecond());
						if (!Sets.intersection(altPOutSet, leafPOutSet).isEmpty()) {
							nonEmptySequence = true;
							break connecting;
						}
					}
					Collection<List<AlphaPairOfAlphaPairCollection<E>>> newLists = new HashSet<>();
					for (List<AlphaPairOfAlphaPairCollection<E>> list : lists) {
						altPOutSet = new HashSet<>(list.get(list.size() - 1).getSecond());
						for (AlphaPairOfAlphaPairCollection<E> pAlt : union) {
							if (pAlt.equals(leaf) || list.contains(pAlt)) {
								continue;
							}
							altPInSet = new HashSet<>(pAlt.getFirst());
							if (!Sets.intersection(altPOutSet, altPInSet).isEmpty()) {
								List<AlphaPairOfAlphaPairCollection<E>> candidateList = new ArrayList<>(list);
								candidateList.add(pAlt);
								newLists.add(candidateList);
							}
						}
					}
					lists.clear();
					lists.addAll(newLists);
				}

				if (!nonEmptySequence) {
					resultCollection.add(leaf);
				}
			}
		}
	}

	/**
	 * @return the redundantCandidates
	 */
	public Collection<AlphaPairOfAlphaPairCollection<E>> getRedundantCandidates() {
		return redundantCandidates;
	}

	/**
	 * @param redundantCandidates
	 *            the redundantCandidates to set
	 */
	public void setRedundantCandidates(Collection<AlphaPairOfAlphaPairCollection<E>> redundantCandidates) {
		this.redundantCandidates = redundantCandidates;
	}

}
