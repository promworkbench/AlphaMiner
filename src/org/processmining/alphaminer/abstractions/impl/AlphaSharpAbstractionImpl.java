package org.processmining.alphaminer.abstractions.impl;

import org.processmining.alphaminer.abstractions.AlphaPlusAbstraction;
import org.processmining.alphaminer.abstractions.AlphaSharpAbstraction;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.factories.CausalAbstractionFactory;
import org.processmining.logabstractions.factories.MendaciousAbstractionFactory;
import org.processmining.logabstractions.factories.ParallelAbstractionFactory;
import org.processmining.logabstractions.factories.UnrelatedAbstractionFactory;
import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.LengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.MendaciousAbstraction;
import org.processmining.logabstractions.models.TwoWayLengthTwoLoopAbstraction;

public class AlphaSharpAbstractionImpl<E> extends AlphaClassicAbstractionImpl<E> implements AlphaSharpAbstraction<E> {

	private final CausalAbstraction<E> caReal;
	private final LengthTwoLoopAbstraction<E> ltla;
	private final MendaciousAbstraction<E> ma;
	private final MendaciousAbstraction<E> maNonRedundant;
	private final MendaciousAbstraction<E> maRedundant;
	private final TwoWayLengthTwoLoopAbstraction<E> twltla;

	public AlphaSharpAbstractionImpl(AlphaPlusAbstraction<E> apa) {
		super(CausalAbstractionFactory.constructAlphaSharpCausalAbstraction(apa.getDirectlyFollowsAbstraction(),
				apa.getTwoWayLengthTwoLoopAbstraction()), apa.getDirectlyFollowsAbstraction(),
				apa.getEndActivityAbstraction(), apa.getLengthOneLoopAbstraction(),
				ParallelAbstractionFactory.constructAlphaSharpParallelAbstraction(apa.getDirectlyFollowsAbstraction(),
						apa.getTwoWayLengthTwoLoopAbstraction()),
				apa.getStartActivityAbstraction(), UnrelatedAbstractionFactory.constructAlphaSharpUnrelatedAbstraction(
						apa.getDirectlyFollowsAbstraction(), apa.getTwoWayLengthTwoLoopAbstraction()));
		ltla = apa.getLengthTwoLoopAbstraction();
		twltla = apa.getTwoWayLengthTwoLoopAbstraction();
		ma = MendaciousAbstractionFactory.constructAlphaSharpMendaciousAbstraction(getCausalAbstraction(),
				getDirectlyFollowsAbstraction(), getParallelAbstraction());
		Pair<MendaciousAbstraction<E>, MendaciousAbstraction<E>> both = MendaciousAbstractionFactory
				.splitByRedundancyRuleAlphaSharp(getCausalAbstraction(), getMendaciousAbstraction());
		maNonRedundant = both.getFirst();
		maRedundant = both.getSecond();
		caReal = CausalAbstractionFactory.constructAlphaSharpRealCausalAbstraction(getCausalAbstraction(),
				getMendaciousAbstraction());
	}

	public AlphaSharpAbstractionImpl(AlphaSharpAbstractionImpl<E> asa) {
		super(asa);
		ltla = asa.getLengthTwoLoopAbstraction();
		twltla = asa.getTwoWayLengthTwoLoopAbstraction();
		ma = asa.getMendaciousAbstraction();
		maNonRedundant = asa.getNonRedundantMendaciousDependencies();
		maRedundant = asa.getRedundantMendaciousDependencies();
		caReal = asa.getRepairedCausalAbstraction();
	}

	public LengthTwoLoopAbstraction<E> getLengthTwoLoopAbstraction() {
		return ltla;
	}

	public MendaciousAbstraction<E> getMendaciousAbstraction() {
		return ma;
	}

	public MendaciousAbstraction<E> getNonRedundantMendaciousDependencies() {
		return maNonRedundant;
	}

	public MendaciousAbstraction<E> getRedundantMendaciousDependencies() {
		return maRedundant;
	}

	public CausalAbstraction<E> getRepairedCausalAbstraction() {
		return caReal;
	}

	public TwoWayLengthTwoLoopAbstraction<E> getTwoWayLengthTwoLoopAbstraction() {
		return twltla;
	}
}
