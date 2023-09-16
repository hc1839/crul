package io.github.hc1839.crul.chemistry.tinker.force.vdw

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableFunction

/**
 *  Combination rule with its first-order derivative.
 */
interface DifferentiableComboRule :
    ComboRule,
    MultivariateDifferentiableFunction
{
    /**
     *  Value of a vdW parameter and its first-order derivative.
     *
     *  Order of coordinates is not important.
     *
     *  @param point
     *      Only `value` of `DerivativeStructure` is used.
     *
     *  @return
     *      Value of the vdW parameter and its first-order derivative.
     */
    abstract override fun value(point: Array<DerivativeStructure>):
        DerivativeStructure
}
