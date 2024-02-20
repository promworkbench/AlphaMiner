package org.processmining.alphaminer.models;

import java.util.Collection;
import java.util.HashSet;

import org.processmining.framework.util.Pair;

/**
 * Model representing the result of the "ConIT" method of the alpha sharp
 * algorithm as presented in "Mining Process Models with Prime Invisible Task".
 * 
 * @author svzelst
 *
 * @param <E>
 */
public class AlphaSharpConITDataModel<E> {

	@SuppressWarnings("unchecked")
	public AlphaSharpConITDataModel(Collection<Pair<Object, Object>> sequentialDependencies,
			Collection<Pair<Object, Object>> parallelDependencies) {
		super();
		this.sequentialDependencies = sequentialDependencies;
		this.parallelDependencies = parallelDependencies;
		invisibleTransitions = new HashSet<>();
		for (Pair<Object, Object> pair : sequentialDependencies) {
			if (pair.getFirst() instanceof AlphaPairOfAlphaPairCollection) {
				invisibleTransitions.add((AlphaPairOfAlphaPairCollection<E>) pair.getFirst());
			}
			if (pair.getSecond() instanceof AlphaPairOfAlphaPairCollection) {
				invisibleTransitions.add((AlphaPairOfAlphaPairCollection<E>) pair.getSecond());
			}
		}
		for (Pair<Object, Object> pair : parallelDependencies) {
			if (pair.getFirst() instanceof AlphaPairOfAlphaPairCollection) {
				invisibleTransitions.add((AlphaPairOfAlphaPairCollection<E>) pair.getFirst());
			}
			if (pair.getSecond() instanceof AlphaPairOfAlphaPairCollection) {
				invisibleTransitions.add((AlphaPairOfAlphaPairCollection<E>) pair.getSecond());
			}
		}
	}

	private final Collection<Pair<Object, Object>> sequentialDependencies;

	private final Collection<Pair<Object, Object>> parallelDependencies;

	private final Collection<AlphaPairOfAlphaPairCollection<E>> invisibleTransitions;

	/**
	 * @return a collection of pairs of objects. if the object of a pair is an
	 *         instance of AlphaPairOfAlphaPairCollection, it refers to an
	 *         invisible transition. If not, it refers to a visible transition
	 *         of type E.
	 */
	public Collection<Pair<Object, Object>> getParallelDependencies() {
		return parallelDependencies;
	}

	/**
	 * @return a collection of pairs of objects. if the object of a pair is an
	 *         instance of AlphaPairOfAlphaPairCollection, it refers to an
	 *         invisible transition. If not, it refers to a visible transition
	 *         of type E.
	 */
	public Collection<Pair<Object, Object>> getSequentialDependencies() {
		return sequentialDependencies;
	}

	/**
	 * @return the invisibleTransitions
	 */
	public Collection<AlphaPairOfAlphaPairCollection<E>> getInvisibleTransitions() {
		return invisibleTransitions;
	}

}
