package org.processmining.alphaminer.parameters;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class AlphaMinerParameters extends PluginParametersImpl {

	private AlphaVersion version;

	public AlphaMinerParameters() {
	}

	public AlphaMinerParameters(final AlphaVersion version) {
		this.version = version;
	}

	public AlphaVersion getVersion() {
		return version;
	}

	public void setVersion(AlphaVersion version) {
		this.version = version;
	}

}
