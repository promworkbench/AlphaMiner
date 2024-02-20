package org.processmining.alphaminer.parameters;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class RealizablePlacesParameters extends PluginParametersImpl {
	
	private double realizabilityThreshold;
	private double unrealizableTracesThreshold;
	
	public RealizablePlacesParameters() {
	}
	
	public RealizablePlacesParameters(final double realizabilityThreshold,
			final double unrealizableTracesThreshold) {
		this.setRealizabilityThreshold(realizabilityThreshold);
		this.setUnrealizableTracesThreshold(unrealizableTracesThreshold);
	}

	public double getRealizabilityThreshold() {
		return realizabilityThreshold;
	}

	public void setRealizabilityThreshold(double realizabilityThreshold) {
		this.realizabilityThreshold = realizabilityThreshold;
	}

	public double getUnrealizableTracesThreshold() {
		return unrealizableTracesThreshold;
	}

	public void setUnrealizableTracesThreshold(double unrealizableTracesThreshold) {
		this.unrealizableTracesThreshold = unrealizableTracesThreshold;
	}
	
}
