package org.processmining.alphaminer.algorithms;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;

import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * 
 * @author svzelst
 *
 * @param <E>
 *            event classification class
 * @param <A>
 *            alpha abstraction used
 * @param
 * 			<P>
 *            parameters
 */
public interface AlphaMiner<E, A extends AlphaClassicAbstraction<E>, P extends AlphaMinerParameters> {

	A getAbstraction();

	Map<AlphaPairImpl<Collection<E>, Collection<E>>, Place> getAlphaPairToPlaceMapping();

	PluginContext getContext();

	Map<E, Transition> getEventClassToTransitionMapping();

	Executor getExecutor();

	P getParameters();

	Progress getProgress();

	Pair<Petrinet, Marking> run();

}
