package org.processmining.alphaminer.abstractions;

import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.CausalSuccessionAbstraction;
import org.processmining.logabstractions.models.EventuallyFollowsAbstraction;
import org.processmining.logabstractions.models.LongTermFollowsAbstraction;

public interface AlphaPlusPlusAbstraction<E> extends AlphaPlusAbstraction<E> {

	CausalPrecedenceAbstraction<E> getLengthOneLoopFreeCausalPrecedenceAbstraction();

	CausalSuccessionAbstraction<E> getLengthOneLoopFreeCausalSuccessionAbstraction();

	LongTermFollowsAbstraction<E> getLengthOneLoopFreeLongTermFollowsAbstraction();

	EventuallyFollowsAbstraction<E> getLengthOneLoopFreeEventuallyFollowsAbstraction();

}
