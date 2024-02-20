package org.processmining.alphaminer.algorithms;

import java.util.Collection;
import java.util.HashSet;

import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.framework.plugin.Progress;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

public class AlphaPlusPlusSetCausalToReferenceNodeExpanderImpl<E> extends AlphaClassicNodeExpanderImpl<E> {

	public AlphaPlusPlusSetCausalToReferenceNodeExpanderImpl(CausalAbstraction<E> cra,
			UnrelatedAbstraction<E> ua, Collection<E> ignore) {
		super(cra, ua, ignore);
	}

	@Override
	public Collection<AlphaPairImpl<Collection<E>, Collection<E>>> expandNode(AlphaPairImpl<Collection<E>, Collection<E>> toExpand,
			Progress progress, Collection<AlphaPairImpl<Collection<E>, Collection<E>>> unmodifiableResultCollection) {
		Collection<AlphaPairImpl<Collection<E>, Collection<E>>> pairs = new HashSet<>();

		int startIndex = toExpand.getMaxIndexOfFirst() + 1;
		for (int i = startIndex; i < getCandidates().length; i++) {
			AlphaPairImpl<Collection<E>, Collection<E>> expand = expandLeft(getCandidates()[i], toExpand);
			if (expand != null) {
				pairs.add(expand);
			}
		}
		return pairs;
	}
}
