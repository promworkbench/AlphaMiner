package org.processmining.alphaminer.abstractions.impl;

import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.abstractions.AlphaPlusAbstraction;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.factories.CausalAbstractionFactory;
import org.processmining.logabstractions.factories.LoopAbstractionFactory;
import org.processmining.logabstractions.factories.ParallelAbstractionFactory;
import org.processmining.logabstractions.factories.UnrelatedAbstractionFactory;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.EndActivityAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.LengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.ParallelAbstraction;
import org.processmining.logabstractions.models.StartActivityAbstraction;
import org.processmining.logabstractions.models.TwoWayLengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.UnrelatedAbstraction;

public class AlphaPlusAbstractionImpl<E> extends AlphaClassicAbstractionImpl<E> implements AlphaPlusAbstraction<E> {

	private final CausalAbstraction<E> caLf;
	private final DirectlyFollowsAbstraction<E> dfaLf;
	private final EndActivityAbstraction<E> eaaLf;
	private final Pair<E[], int[]> l1lFree;
	private final LengthTwoLoopAbstraction<E> ltla;
	private final LengthTwoLoopAbstraction<E> ltlaLf;
	private final ParallelAbstraction<E> paLf;
	private final StartActivityAbstraction<E> saaLf;
	private final TwoWayLengthTwoLoopAbstraction<E> twltlaLf;
	private final TwoWayLengthTwoLoopAbstraction<E> twltla;
	private final UnrelatedAbstraction<E> uaLf;

	public AlphaPlusAbstractionImpl(AlphaPlusAbstraction<E> apa) {
		super(apa);
		this.caLf = apa.getLengthOneLoopFreeCausalRelationAbstraction();
		this.dfaLf = apa.getLengthOneLoopFreeDirectlyFollowsAbstraction();
		this.eaaLf = apa.getLengthOneLoopFreeEndActivityAbstraction();
		this.l1lFree = apa.getLengthOneLoopFreeEventClasses();
		this.ltla = apa.getLengthTwoLoopAbstraction();
		this.ltlaLf = apa.getLengthOneLoopFreeLengthTwoLoopAbstraction();
		this.paLf = apa.getLengthOneLoopFreeParallelAbstraction();
		this.saaLf = apa.getLengthOneLoopFreeStartActivityAbstraction();
		this.twltlaLf = apa.getLengthOneLoopFreeTwoWayLengthTwoLoopAbstraction();
		this.twltla = apa.getTwoWayLengthTwoLoopAbstraction();
		this.uaLf = apa.getLengthOneLoopFreeUnrelatedAbstraction();
	}

	public AlphaPlusAbstractionImpl(AlphaClassicAbstraction<E> aca, LengthTwoLoopAbstraction<E> ltla,
			Pair<E[], int[]> l1lFree, DirectlyFollowsAbstraction<E> dfaLf, StartActivityAbstraction<E> saLf,
			EndActivityAbstraction<E> eaLf, LengthTwoLoopAbstraction<E> ltlaLf) {
		this(aca.getEventClasses(), aca.getDirectlyFollowsAbstraction(), aca.getStartActivityAbstraction(),
				aca.getEndActivityAbstraction(), aca.getLengthOneLoopAbstraction(), ltla, l1lFree, dfaLf, saLf, eaLf,
				ltlaLf);
	}

	public AlphaPlusAbstractionImpl(E[] classes, DirectlyFollowsAbstraction<E> dfa, StartActivityAbstraction<E> sa,
			EndActivityAbstraction<E> ea, LengthOneLoopAbstraction<E> lola, LengthTwoLoopAbstraction<E> ltla,
			Pair<E[], int[]> l1lFree, DirectlyFollowsAbstraction<E> dfaLf, StartActivityAbstraction<E> saLf,
			EndActivityAbstraction<E> eaLf, LengthTwoLoopAbstraction<E> ltlaLf) {
		super(classes, dfa, sa, ea, lola);
		this.ltla = ltla;
		this.l1lFree = l1lFree;
		this.dfaLf = dfaLf;
		this.saaLf = saLf;
		this.eaaLf = eaLf;
		this.ltlaLf = ltlaLf;
		this.twltla = LoopAbstractionFactory
				.constructBooleanTwoWayLengthTwoLoopAbstraction(getLengthTwoLoopAbstraction());
		this.twltlaLf = LoopAbstractionFactory
				.constructBooleanTwoWayLengthTwoLoopAbstraction(getLengthOneLoopFreeLengthTwoLoopAbstraction());
		this.caLf = CausalAbstractionFactory.constructAlphaPlusCausalAbstraction(
				getLengthOneLoopFreeDirectlyFollowsAbstraction(), getLengthOneLoopFreeTwoWayLengthTwoLoopAbstraction(),
				CausalAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN);
		this.paLf = ParallelAbstractionFactory.constructAlphaPlusParallelAbstraction(
				getLengthOneLoopFreeDirectlyFollowsAbstraction(), getTwoWayLengthTwoLoopAbstraction());
		this.uaLf = UnrelatedAbstractionFactory
				.constructAlphaPlusUnrelatedAbstraction(getLengthOneLoopFreeDirectlyFollowsAbstraction());

	}

	public CausalAbstraction<E> getLengthOneLoopFreeCausalRelationAbstraction() {
		return caLf;
	}

	public DirectlyFollowsAbstraction<E> getLengthOneLoopFreeDirectlyFollowsAbstraction() {
		return dfaLf;
	}

	public EndActivityAbstraction<E> getLengthOneLoopFreeEndActivityAbstraction() {
		return eaaLf;
	}

	public Pair<E[], int[]> getLengthOneLoopFreeEventClasses() {
		return l1lFree;
	}

	public LengthTwoLoopAbstraction<E> getLengthOneLoopFreeLengthTwoLoopAbstraction() {
		return ltlaLf;
	}

	public ParallelAbstraction<E> getLengthOneLoopFreeParallelAbstraction() {
		return paLf;
	}

	public StartActivityAbstraction<E> getLengthOneLoopFreeStartActivityAbstraction() {
		return saaLf;
	}

	public TwoWayLengthTwoLoopAbstraction<E> getLengthOneLoopFreeTwoWayLengthTwoLoopAbstraction() {
		return twltlaLf;
	}

	public UnrelatedAbstraction<E> getLengthOneLoopFreeUnrelatedAbstraction() {
		return uaLf;
	}

	public LengthTwoLoopAbstraction<E> getLengthTwoLoopAbstraction() {
		return ltla;
	}

	protected UnrelatedAbstraction<E> setupLengthOneLoopFreeUnrelatedAbstraction() {
		return uaLf;
	}

	public TwoWayLengthTwoLoopAbstraction<E> getTwoWayLengthTwoLoopAbstraction() {
		return twltla;
	}
}
