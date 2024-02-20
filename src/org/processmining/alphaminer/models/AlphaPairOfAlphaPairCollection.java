package org.processmining.alphaminer.models;

import java.util.Collection;

import org.processmining.framework.util.Pair;

/*
 * pair representing the data structure expanded in the ConIT algorithm as
 * presented in "Mining Process Models with Prime Invisible Tasks"
 * 
 * @author svzelst
 *
 * @param <E>
 */
public class AlphaPairOfAlphaPairCollection<E> extends
		Pair<Collection<AlphaPairImpl<Collection<E>, Collection<E>>>, Collection<AlphaPairImpl<Collection<E>, Collection<E>>>> {

	public AlphaPairOfAlphaPairCollection(Collection<AlphaPairImpl<Collection<E>, Collection<E>>> first,
			Collection<AlphaPairImpl<Collection<E>, Collection<E>>> second, final int firstMaxInt,
			final int secondMaxInt) {
		super(first, second);
		this.firstMaxInt = firstMaxInt;
		this.secondMaxInt = secondMaxInt;
	}

	private int firstMaxInt = -1;

	public int getMaxIndexOfFirst() {
		return firstMaxInt;
	}

	public void setMaxIndexOfFirst(int firstMaxInt) {
		this.firstMaxInt = firstMaxInt;
	}

	public int getMaxIndexOfSecond() {
		return secondMaxInt;
	}

	public void setMaxIndexOfSecond(int secondMaxInt) {
		this.secondMaxInt = secondMaxInt;
	}

	private int secondMaxInt = -1;

}
