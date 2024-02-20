package org.processmining.alphaminer.abstractions;

import org.processmining.logabstractions.models.CausalAbstraction;
import org.processmining.logabstractions.models.LengthTwoLoopAbstraction;
import org.processmining.logabstractions.models.MendaciousAbstraction;
import org.processmining.logabstractions.models.TwoWayLengthTwoLoopAbstraction;

public interface AlphaSharpAbstraction<E> extends AlphaClassicAbstraction<E> {

	MendaciousAbstraction<E> getMendaciousAbstraction();

	MendaciousAbstraction<E> getRedundantMendaciousDependencies();

	MendaciousAbstraction<E> getNonRedundantMendaciousDependencies();

	CausalAbstraction<E> getRepairedCausalAbstraction();

	LengthTwoLoopAbstraction<E> getLengthTwoLoopAbstraction();

	TwoWayLengthTwoLoopAbstraction<E> getTwoWayLengthTwoLoopAbstraction();

}
