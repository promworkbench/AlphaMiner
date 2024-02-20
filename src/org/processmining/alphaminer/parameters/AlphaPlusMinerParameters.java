package org.processmining.alphaminer.parameters;

/**
 * This parameter object allows us to "ignore" length-one-loops completely. This
 * is needed because the alpha++ algorithm uses the alpha+ algorithm internally,
 * and, creates a Petri net based on the event log without any length-one-loop
 * transitions.
 * 
 * @author svzelst
 *
 */
public class AlphaPlusMinerParameters extends AlphaMinerParameters {

	boolean ignoreLengthOneLoops = false;

	public AlphaPlusMinerParameters(final AlphaVersion version) {
		super(version);
	}

	public AlphaPlusMinerParameters(final AlphaVersion version, final boolean ignoreLengthOneLoops) {
		this.ignoreLengthOneLoops = ignoreLengthOneLoops;
	}

	public boolean isIgnoreLengthOneLoops() {
		return ignoreLengthOneLoops;
	}

	public void setIgnoreLengthOneLoops(boolean ignoreLengthOneLoops) {
		this.ignoreLengthOneLoops = ignoreLengthOneLoops;
	}

}
