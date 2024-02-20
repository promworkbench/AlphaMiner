package org.processmining.alphaminer.abstractions;

import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.EndActivityAbstraction;
import org.processmining.logabstractions.models.LengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.StartActivityAbstraction;
import org.processmining.logabstractions.models.TwoWayLengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

/**
 * Abstraction, intended for the alpha+ mining algorithm. Conceptually the
 * ...LengthOneLoopFree... functions represent the relations based on an event
 * log in which each length-one-loop activity is removed.
 * 
 * @param <E>
 */
public interface AlphaPlusAbstraction<E> extends AlphaClassicAbstraction<E> {

	CausalAbstraction<E> getLengthOneLoopFreeCausalRelationAbstraction();

	DirectlyFollowsAbstraction<E> getLengthOneLoopFreeDirectlyFollowsAbstraction();

	EndActivityAbstraction<E> getLengthOneLoopFreeEndActivityAbstraction();

	Pair<E[], int[]> getLengthOneLoopFreeEventClasses();
	
	LengthTwoLoopAbstraction<E> getLengthOneLoopFreeLengthTwoLoopAbstraction();

	ParallelAbstraction<E> getLengthOneLoopFreeParallelAbstraction();

	StartActivityAbstraction<E> getLengthOneLoopFreeStartActivityAbstraction();

	TwoWayLengthTwoLoopAbstraction<E> getLengthOneLoopFreeTwoWayLengthTwoLoopAbstraction();

	UnrelatedAbstraction<E> getLengthOneLoopFreeUnrelatedAbstraction();

	LengthTwoLoopAbstraction<E> getLengthTwoLoopAbstraction();

	TwoWayLengthTwoLoopAbstraction<E> getTwoWayLengthTwoLoopAbstraction();
}
