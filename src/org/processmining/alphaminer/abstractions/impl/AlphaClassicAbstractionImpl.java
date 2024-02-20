package org.processmining.alphaminer.abstractions.impl;

import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.logabstractions.factories.CausalAbstractionFactory;
import org.processmining.logabstractions.factories.LoopAbstractionFactory;
import org.processmining.logabstractions.factories.ParallelAbstractionFactory;
import org.processmining.logabstractions.factories.UnrelatedAbstractionFactory;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.EndActivityAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.StartActivityAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;
import org.processmining.logabstractions.models.abstr.AbstractAbstraction;

public class AlphaClassicAbstractionImpl<E> extends AbstractAbstraction<E> implements AlphaClassicAbstraction<E> {

	private final CausalAbstraction<E> ca;
	private final DirectlyFollowsAbstraction<E> dfa;
	private final EndActivityAbstraction<E> eaa;
	private final LengthOneLoopAbstraction<E> lola;
	private final ParallelAbstraction<E> pa;
	private final StartActivityAbstraction<E> saa;
	private final UnrelatedAbstraction<E> ua;

	public AlphaClassicAbstractionImpl(final AlphaClassicAbstraction<E> aca) {
		super(aca.getEventClasses());
		this.ca = aca.getCausalAbstraction();
		this.dfa = aca.getDirectlyFollowsAbstraction();
		this.eaa = aca.getEndActivityAbstraction();
		this.lola = aca.getLengthOneLoopAbstraction();
		this.pa = aca.getParallelAbstraction();
		this.saa = aca.getStartActivityAbstraction();
		this.ua = aca.getUnrelatedAbstraction();
	}

	public AlphaClassicAbstractionImpl(CausalAbstraction<E> ca, DirectlyFollowsAbstraction<E> dfa,
			EndActivityAbstraction<E> ea, LengthOneLoopAbstraction<E> lola, ParallelAbstraction<E> pa,
			StartActivityAbstraction<E> sa, UnrelatedAbstraction<E> ua) {
		super(ca.getEventClasses());
		this.ca = ca;
		this.dfa = dfa;
		this.eaa = ea;
		this.lola = lola;
		this.pa = pa;
		this.saa = sa;
		this.ua = ua;
	}

	public AlphaClassicAbstractionImpl(final E[] eventClasses, DirectlyFollowsAbstraction<E> dfa,
			StartActivityAbstraction<E> sa, EndActivityAbstraction<E> ea) {
		this(eventClasses, dfa, sa, ea, LoopAbstractionFactory.constructBooleanLengthOneLoopAbstraction(dfa));
	}

	public AlphaClassicAbstractionImpl(final E[] eventClasses, DirectlyFollowsAbstraction<E> dfa,
			StartActivityAbstraction<E> sa, EndActivityAbstraction<E> ea, LengthOneLoopAbstraction<E> lola) {
		super(eventClasses);
		this.dfa = dfa;
		this.saa = sa;
		this.eaa = ea;
		this.lola = lola;
		ca = CausalAbstractionFactory.constructAlphaClassicCausalAbstraction(getDirectlyFollowsAbstraction());
		pa = ParallelAbstractionFactory.constructAlphaClassicParallelAbstraction(getDirectlyFollowsAbstraction());
		ua = UnrelatedAbstractionFactory.constructAlphaClassicUnrelatedAbstraction(getDirectlyFollowsAbstraction());
	}

	public CausalAbstraction<E> getCausalAbstraction() {
		return ca;
	}

	public DirectlyFollowsAbstraction<E> getDirectlyFollowsAbstraction() {
		return dfa;
	}

	public EndActivityAbstraction<E> getEndActivityAbstraction() {
		return eaa;
	}

	public LengthOneLoopAbstraction<E> getLengthOneLoopAbstraction() {
		return lola;
	}

	public ParallelAbstraction<E> getParallelAbstraction() {
		return pa;
	}

	public StartActivityAbstraction<E> getStartActivityAbstraction() {
		return saa;
	}

	public UnrelatedAbstraction<E> getUnrelatedAbstraction() {
		return ua;
	}

}
