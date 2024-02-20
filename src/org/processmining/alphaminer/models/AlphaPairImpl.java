package org.processmining.alphaminer.models;

import java.util.Collection;

import org.processmining.framework.util.Pair;

/**
 * Alpha Tuple Impl, inspired by previous alpha miner implementation by
 * Boudewijn van Dongen. The tuple assumes that there is some order possible on
 * the elements of F, S respectively. The maxint variables can be used to ignore
 * some previously inspected elements in creating the maximal sets A and B of
 * the alpha algorithm.
 * 
 * @author svzelst
 *
 * @param <F>
 * @param <S>
 */
public class AlphaPairImpl<F extends Collection<?>, S extends Collection<?>> extends Pair<F, S> {

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

	public AlphaPairImpl(final F first, final S second, final int firstMaxInt, final int secondMaxInt) {
		super(first, second);
		this.firstMaxInt = firstMaxInt;
		this.secondMaxInt = secondMaxInt;
	}

}
