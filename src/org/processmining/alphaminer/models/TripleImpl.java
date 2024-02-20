package org.processmining.alphaminer.models;

public class TripleImpl<F, S, T> implements Triple<F, S, T> {

	private final F first;

	private final S second;

	private final T third;

	public TripleImpl(F first, S second, T third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public F getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}

	public T getThird() {
		return third;
	}

	@Override
	public boolean equals(Object other) {
		boolean res = other instanceof Triple;
		if (res) {
			Triple<?, ?, ?> cast = (Triple<?, ?, ?>) other;
			res &= getFirst().equals(cast.getFirst());
			res &= getSecond().equals(cast.getSecond());
			res &= getThird().equals(cast.getThird());
		}
		return res;
	}

	@Override
	public int hashCode() {
		if (first == null) {
			if (second == null) {
				return third == null ? 0 : third.hashCode() + 1;
			} else {
				return third == null ? second.hashCode() + 2 : second.hashCode() * 17 + second.hashCode();
			}
		} else {
			if (second == null) {
				return third == null ? first.hashCode() + 3 : first.hashCode() * 33 + third.hashCode();
			} else {
				return third == null ? first.hashCode() * 54 + second.hashCode() * 23
						: first.hashCode() * 3 + second.hashCode() * 35 + third.hashCode();
			}
		}
	}

	@Override
	public String toString() {
		return "(" + first + "," + second + "," + third + ")";
	}

}
