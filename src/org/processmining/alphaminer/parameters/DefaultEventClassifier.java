package org.processmining.alphaminer.parameters;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;

public class DefaultEventClassifier {

	public static XEventClassifier get() {
		return new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
	}

}
