package org.processmining.alphaminer.abstractions.impl;

import org.processmining.alphaminer.abstractions.AlphaPlusAbstraction;
import org.processmining.alphaminer.abstractions.AlphaPlusPlusAbstraction;
import org.processmining.logabstractions.factories.EventuallyFollowsAbstractionFactory;
import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.CausalSuccessionAbstraction;
import org.processmining.logabstractions.models.EventuallyFollowsAbstraction;
import org.processmining.logabstractions.models.LongTermFollowsAbstraction;

public class AlphaPlusPlusAbstractionImpl<E> extends AlphaPlusAbstractionImpl<E>
		implements AlphaPlusPlusAbstraction<E> {

	private final CausalPrecedenceAbstraction<E> cpa;
	private final CausalSuccessionAbstraction<E> csa;
	private final LongTermFollowsAbstraction<E> ltfa;
	private final EventuallyFollowsAbstraction<E> efa;

	public AlphaPlusPlusAbstractionImpl(AlphaPlusPlusAbstraction<E> appa) {
		super(appa);
		this.cpa = appa.getLengthOneLoopFreeCausalPrecedenceAbstraction();
		this.csa = appa.getLengthOneLoopFreeCausalSuccessionAbstraction();
		this.efa = appa.getLengthOneLoopFreeEventuallyFollowsAbstraction();
		this.ltfa = appa.getLengthOneLoopFreeLongTermFollowsAbstraction();
	}

	public AlphaPlusPlusAbstractionImpl(AlphaPlusAbstraction<E> apa, CausalPrecedenceAbstraction<E> cpa,
			CausalSuccessionAbstraction<E> csa, LongTermFollowsAbstraction<E> ltlfa) {
		super(apa);
		this.ltfa = ltlfa;
		this.cpa = cpa;
		this.csa = csa;
		efa = EventuallyFollowsAbstractionFactory.constructAlphaPlusPlusEventuallyFollowsAbstraction(
				getLengthOneLoopFreeCausalRelationAbstraction(), getLengthOneLoopFreeLongTermFollowsAbstraction());
	}

	public CausalPrecedenceAbstraction<E> getLengthOneLoopFreeCausalPrecedenceAbstraction() {
		return cpa;
	}

	public CausalSuccessionAbstraction<E> getLengthOneLoopFreeCausalSuccessionAbstraction() {
		return csa;
	}

	public LongTermFollowsAbstraction<E> getLengthOneLoopFreeLongTermFollowsAbstraction() {
		return ltfa;
	}

	public EventuallyFollowsAbstraction<E> getLengthOneLoopFreeEventuallyFollowsAbstraction() {
		return efa;
	}
}
