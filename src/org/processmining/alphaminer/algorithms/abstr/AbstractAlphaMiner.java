package org.processmining.alphaminer.algorithms.abstr;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.algorithms.AlphaMiner;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.models.FakeProgressImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.logabstractions.models.EndActivityAbstraction;
import org.processmining.logabstractions.models.StartActivityAbstraction;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public abstract class AbstractAlphaMiner<E, A extends AlphaClassicAbstraction<E>, P extends AlphaMinerParameters>
		implements AlphaMiner<E, A, P> {

	private final A abstraction;
	private final Map<E, Transition> class2transition = new HashMap<E, Transition>();
	private final PluginContext context;
	private final Executor executor;
	private final P parameters;
	private final Progress progress;
	private final Map<AlphaPairImpl<Collection<E>, Collection<E>>, Place> tuple2place = new HashMap<>();

	public AbstractAlphaMiner(PluginContext context, P parameters, A abstraction) {
		this.abstraction = abstraction;
		this.context = context;
		this.parameters = parameters;
		progress = context == null ? new FakeProgressImpl() : context.getProgress();
		executor = context == null ? Executors.newCachedThreadPool() : context.getExecutor();
	}

	protected void addFinalPlace(Petrinet net, EndActivityAbstraction<E> endActivities) {
		addFinalPlace(net, endActivities, null);
	}

	protected void addFinalPlace(Petrinet net, EndActivityAbstraction<E> endActivities, Marking m) {
		Place pend = net.addPlace("End");
		for (E eventClass : endActivities.getAllGEQThreshold()) {
			net.addArc(class2transition.get(eventClass), pend);
		}
		if (m != null) {
			m.add(pend);
		}
	}

	protected void addInitialPlace(Petrinet net, StartActivityAbstraction<E> startActivities, Marking m) {
		Place pstart = net.addPlace("Start");
		for (E eventClass : startActivities.getAllGEQThreshold()) {
			net.addArc(pstart, class2transition.get(eventClass));
		}
		m.add(pstart);
	}

	protected <C extends Collection<E>> Petrinet addPlaces(Petrinet net, Iterable<AlphaPairImpl<C, C>> tuples) {
		for (AlphaPairImpl<? extends Collection<E>, ? extends Collection<E>> tuple : tuples) {
			Place p = net.addPlace(tuple.toString());
			for (E eventClass : tuple.getFirst()) {
				net.addArc(class2transition.get(eventClass), p);
			}
			for (E eventClass : tuple.getSecond()) {
				net.addArc(p, class2transition.get(eventClass));
			}
			tuple2place.put(
					new AlphaPairImpl<Collection<E>, Collection<E>>(new HashSet<>(tuple.getFirst()),
							new HashSet<>(tuple.getSecond()), tuple.getMaxIndexOfFirst(), tuple.getMaxIndexOfSecond()),
					p);
		}
		return net;
	}

	protected void addTransitions(Petrinet net) {
		addTransitions(net, new HashSet<E>());
	}

	protected void addTransitions(Petrinet net, Collection<E> ignore) {
		for (int i = 0; i < abstraction.getEventClasses().length; i++) {
			E eventClass = abstraction.getEventClass(i);
			if (!ignore.contains(eventClass)) {
				Transition transition = net.addTransition(eventClass.toString());
				class2transition.put(eventClass, transition);
			}
		}
	}

	public A getAbstraction() {
		return abstraction;
	}

	public Map<AlphaPairImpl<Collection<E>, Collection<E>>, Place> getAlphaPairToPlaceMapping() {
		return tuple2place;
	}

	public PluginContext getContext() {
		return context;
	}

	public Map<E, Transition> getEventClassToTransitionMapping() {
		return class2transition;
	}

	public Executor getExecutor() {
		return executor;
	}

	public P getParameters() {
		return parameters;
	}

	public Progress getProgress() {
		return progress;
	}
}
