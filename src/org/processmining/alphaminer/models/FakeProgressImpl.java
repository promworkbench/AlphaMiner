package org.processmining.alphaminer.models;

import org.processmining.framework.plugin.Progress;

public class FakeProgressImpl implements Progress {

	private int min = 0;
	private int max = Integer.MAX_VALUE;
	private boolean isIndeterminate = false;
	private boolean isCancelled = false;
	private int value = 0;

	public void setMinimum(int value) {
		min = value;
	}

	public void setMaximum(int value) {
		max = value;
	}

	public void setValue(int value) {
		this.value = value;

	}

	public void setCaption(String message) {
		// NOP
	}

	public String getCaption() {
		return "Fake Progress";
	}

	public int getValue() {
		return value;
	}

	public void inc() {
		value++;

	}

	public void setIndeterminate(boolean makeIndeterminate) {
		isIndeterminate = makeIndeterminate;
	}

	public boolean isIndeterminate() {
		return isIndeterminate;
	}

	public int getMinimum() {
		return min;
	}

	public int getMaximum() {
		return max;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void cancel() {
		isCancelled = true;
	}

}
