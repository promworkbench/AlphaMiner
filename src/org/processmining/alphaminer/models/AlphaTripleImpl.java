package org.processmining.alphaminer.models;

import java.util.Collection;

public class AlphaTripleImpl<F extends Collection<?>, S extends Collection<?>, T extends Collection<?>>
		extends TripleImpl<F, S, T> {

	private final int maxIndexOfFirst;

	private final int maxIndexOfSecond;

	private final int maxIndexOfThird;

	public AlphaTripleImpl(F first, S second, T third) {
		this(first, second, third, -1, -1, -1);
	}

	public AlphaTripleImpl(F first, S second, T third, int indexF, int indexS, int indexT) {
		super(first, second, third);
		this.maxIndexOfFirst = indexF;
		this.maxIndexOfSecond = indexS;
		this.maxIndexOfThird = indexT;
	}

	public int getMaxIndexOfFirst() {
		return maxIndexOfFirst;
	}

	public int getMaxIndexOfSecond() {
		return maxIndexOfSecond;
	}

	public int getMaxIndexOfThird() {
		return maxIndexOfThird;
	}

}
