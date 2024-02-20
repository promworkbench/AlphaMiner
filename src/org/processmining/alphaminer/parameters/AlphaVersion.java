package org.processmining.alphaminer.parameters;

public enum AlphaVersion {
	CLASSIC("Alpha"), PLUS("Alpha+"), PLUS_PLUS("Alpha++"), SHARP("Alpha#"), ROBUST("AlphaR"), DOLLAR(
			"Alpha$");

	private final String name;

	private AlphaVersion(final String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

}
