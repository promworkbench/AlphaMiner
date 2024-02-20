package org.processmining.alphaminer.models;

import java.util.Collection;

public interface Union<T> extends Collection<T> {

	Collection<T> getLeft();
	
	Collection<T> getRight();
	
}
