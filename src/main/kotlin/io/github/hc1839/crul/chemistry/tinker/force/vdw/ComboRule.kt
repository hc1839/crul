package io.github.hc1839.crul.chemistry.tinker.force.vdw

import org.apache.commons.math3.analysis.MultivariateFunction

/**
 *  Combination rule.
 */
interface ComboRule : MultivariateFunction {
    /**
     *  Value of a vdW parameter.
     *
     *  It is for compatibility with Apache Commons Math. For more robust
     *  evaluation, use the overloaded function that relies on atom classes. An
     *  implementing class may throw an exception if the evaluation cannot be
     *  done from the arguments alone.
     *
     *  Order of coordinates is not important.
     *
     *  @param point
     *      Each coordinate is a like-pair vdW parameter. If determining a
     *      like-pair vdW parameter, one coordinate is expected, and the
     *      function is an identity function. If determining an unlike-pair vdW
     *      parameter, two coordinates are expected, and the function is the
     *      combination rule.
     *
     *  @return
     *      Value of the vdW parameter.
     */
    abstract override fun value(point: DoubleArray): Double

    /**
     *  Value of a vdW parameter.
     *
     *  Order in which the vdW-parameter groups are specified is not important.
     *
     *  @param a
     *      One of the vdW-parameter groups.
     *
     *  @param b
     *      Other vdW-parameter group.
     *
     *  @return
     *      Value of the vdW parameter.
     */
    fun value(a: VdwParamGroup, b: VdwParamGroup): Double
}
