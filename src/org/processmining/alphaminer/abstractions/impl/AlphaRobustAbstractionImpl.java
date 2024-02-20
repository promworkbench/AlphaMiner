package org.processmining.alphaminer.abstractions.impl;

import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.logabstractions.factories.CausalAbstractionFactory;
import org.processmining.logabstractions.factories.ParallelAbstractionFactory;
import org.processmining.logabstractions.factories.UnrelatedAbstractionFactory;
import org.processmining.logabstractions.models.ActivityCountAbstraction;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.EndActivityAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.StartActivityAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

public class AlphaRobustAbstractionImpl<E> extends AlphaClassicAbstractionImpl<E> implements AlphaRobustAbstraction<E> {
	
	private final ActivityCountAbstraction<E> ac;
	private final CausalAbstraction<E> caR;
	private final ParallelAbstraction<E> paR;
	private final UnrelatedAbstraction<E> uaR;
	private final double causalThreshold;
	private final double noiseThresholdLeastFreq;
	private final double noiseThresholdMostFreq;

	public AlphaRobustAbstractionImpl(AlphaRobustAbstraction<E> ara) {
		super(ara);
		this.ac = ara.getRobustActivityCount();
		this.caR = ara.getRobustCausalAbstraction();
		this.paR = ara.getRobustParallelAbstraction();
		this.uaR = ara.getRobustUnrelatedAbstraction();
		this.causalThreshold = ara.getCausalThreshold();
		this.noiseThresholdLeastFreq = ara.getNoiseThresholdLeastFreq();
		this.noiseThresholdMostFreq = ara.getNoiseThresholdMostFreq();
	}
	
	public AlphaRobustAbstractionImpl(E[] classes, DirectlyFollowsAbstraction<E> dfa, StartActivityAbstraction<E> sa,
			EndActivityAbstraction<E> ea, LengthOneLoopAbstraction<E> lola, ActivityCountAbstraction<E> ac,
			double causalThreshold, double noiseThresholdLeastFreq, double noiseThresholdMostFreq) {
		super(classes, dfa, sa, ea, lola);
		this.ac = ac;
		this.causalThreshold = causalThreshold;
		this.noiseThresholdLeastFreq = noiseThresholdLeastFreq;
		this.noiseThresholdMostFreq = noiseThresholdMostFreq;
		this.caR = CausalAbstractionFactory.constructAlphaRobustCausalAbstraction(
				getDirectlyFollowsAbstraction(), getNoiseThresholdLeastFreq(), getNoiseThresholdMostFreq(),
				getCausalThreshold(), getRobustActivityCount());
		this.paR = ParallelAbstractionFactory.constructAlphaRobustParallelAbstraction(
				getDirectlyFollowsAbstraction(), getNoiseThresholdLeastFreq(), getNoiseThresholdMostFreq(),
				getCausalThreshold(), getRobustActivityCount());
		this.uaR = UnrelatedAbstractionFactory.constructAlphaRobustUnrelatedAbstraction(
				getDirectlyFollowsAbstraction(), getNoiseThresholdLeastFreq(), getNoiseThresholdMostFreq(),
				getRobustActivityCount());
	}
	
	
	public ActivityCountAbstraction<E> getRobustActivityCount() {
		return ac;
	}
	
	public CausalAbstraction<E> getRobustCausalAbstraction() {
		return caR;
	}
	
	public ParallelAbstraction<E> getRobustParallelAbstraction() {
		return paR;
	}
	
	public UnrelatedAbstraction<E> getRobustUnrelatedAbstraction() {
		return uaR;
	}
	
	public double getCausalThreshold() {
		return causalThreshold;
	}
	
	public double getNoiseThresholdLeastFreq() {
		return noiseThresholdLeastFreq;
	}
	
	public double getNoiseThresholdMostFreq() {
		return noiseThresholdMostFreq;
	}
}
