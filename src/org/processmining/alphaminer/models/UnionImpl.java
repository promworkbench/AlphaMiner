package org.processmining.alphaminer.models;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Iterators;

public class UnionImpl<T> implements Union<T> {

	private final Collection<T> left;

	private final Collection<T> right;

	public UnionImpl(Collection<T> left, Collection<T> right) {
		this.left = new HashSet<>(left);
		this.right = new HashSet<>(right);
	}

	public int size() {
		return left.size() + right.size();
	}

	public boolean isEmpty() {
		return left.isEmpty() && right.isEmpty();
	}

	public boolean contains(Object o) {
		return left.contains(o) || right.contains(o);
	}

	public Iterator<T> iterator() {
		return Iterators.concat(left.iterator(), right.iterator());
	}

	public Object[] toArray() {
		return ArrayUtils.addAll(left.toArray(), right.toArray());
	}

	public <E> E[] toArray(E[] a) {
		return ArrayUtils.addAll(left.toArray(Arrays.copyOf(a, left.size())), Arrays.copyOf(a, right.size()));
	}

	public boolean add(T e) {
		return left.add(e) && right.add(e);
	}

	public boolean remove(Object o) {
		return left.remove(o) && right.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		boolean res = true;
		for (Object o : c) {
			if (!(left.contains(o) || right.contains(o))) {
				res = false;
				break;
			}
		}
		return res;
	}

	public boolean addAll(Collection<? extends T> c) {
		return left.addAll(c) && right.addAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return left.removeAll(c) && right.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return left.retainAll(c) && right.retainAll(c);
	}

	public void clear() {
		left.clear();
		right.clear();
	}

	public Collection<T> getLeft() {
		return left;
	}

	public Collection<T> getRight() {
		return right;
	}

}
