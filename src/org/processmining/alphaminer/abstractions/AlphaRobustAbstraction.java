package org.processmining.alphaminer.abstractions;

import org.processmining.logabstractions.models.ActivityCountAbstraction;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

public interface AlphaRobustAbstraction<E> extends AlphaClassicAbstraction<E> {
	
	ActivityCountAbstraction<E> getRobustActivityCount();

	CausalAbstraction<E> getRobustCausalAbstraction();
	
	ParallelAbstraction<E> getRobustParallelAbstraction();
	
	UnrelatedAbstraction<E> getRobustUnrelatedAbstraction();
	
	double getCausalThreshold();
	
	double getNoiseThresholdLeastFreq();
	
	double getNoiseThresholdMostFreq();
}
