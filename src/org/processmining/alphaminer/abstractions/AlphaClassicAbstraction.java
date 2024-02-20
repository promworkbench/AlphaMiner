package org.processmining.alphaminer.abstractions;

import org.processmining.logabstractions.models.Abstraction;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.EndActivityAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.StartActivityAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

public interface AlphaClassicAbstraction<E> extends Abstraction<E> {

	CausalAbstraction<E> getCausalAbstraction();

	DirectlyFollowsAbstraction<E> getDirectlyFollowsAbstraction();

	EndActivityAbstraction<E> getEndActivityAbstraction();

	LengthOneLoopAbstraction<E> getLengthOneLoopAbstraction();

	ParallelAbstraction<E> getParallelAbstraction();

	StartActivityAbstraction<E> getStartActivityAbstraction();

	UnrelatedAbstraction<E> getUnrelatedAbstraction();
}
