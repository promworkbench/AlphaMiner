package org.processmining.alphaminer.parameters;

public class AlphaRobustMinerParameters extends AlphaMinerParameters {

	private double causalThreshold;
	private double noiseThresholdLeastFreq;
	private double noiseThresholdMostFreq;

	public AlphaRobustMinerParameters(AlphaVersion version) {
		super(version);
	}

	public AlphaRobustMinerParameters(final double causalThreshold, final double noiseThresholdLeastFreq,
			final double noiseThresholdMostFreq) {
		this.causalThreshold = causalThreshold;
		this.noiseThresholdLeastFreq = noiseThresholdLeastFreq;
		this.noiseThresholdMostFreq = noiseThresholdMostFreq;
	}

	public double getCausalThreshold() {
		return causalThreshold;
	}

	public void setCausalThreshold(double causalThreshold) {
		this.causalThreshold = causalThreshold;
	}

	public double getNoiseThresholdLeastFreq() {
		return noiseThresholdLeastFreq;
	}

	public void setNoiseThresholdLeastFreq(double noiseThresholdLeastFreq) {
		this.noiseThresholdLeastFreq = noiseThresholdLeastFreq;
	}

	public double getNoiseThresholdMostFreq() {
		return noiseThresholdMostFreq;
	}

	public void setNoiseThresholdMostFreq(double noiseThresholdMostFreq) {
		this.noiseThresholdMostFreq = noiseThresholdMostFreq;
	}

}
