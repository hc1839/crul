package io.github.hc1839.crul.chemistry.tinker.force.vdw

import kotlin.math.pow
import kotlin.math.sqrt

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure

import io.github.hc1839.crul.chemistry.tinker.AtomClass

/**
 *  HHG rule for epsilon.
 */
object HhgRule : DifferentiableComboRule {
    override fun value(point: DoubleArray): Double =
        when (point.count()) {
            1 -> point[0]

            2 -> {
                (4.0 * point[0] * point[1]) /
                    (sqrt(point[0]) + sqrt(point[1])).pow(2)
            }

            else -> throw IllegalArgumentException(
                "Not one or two coordinates are given."
            )
        }

    override fun value(a: VdwParamGroup, b: VdwParamGroup): Double {
        return if (a == b) {
            a.epsilon
        } else {
            value(
                listOf(a.epsilon, b.epsilon).toDoubleArray()
            )
        }
    }

    /**
     *  Value of the first-order derivative with respect to the first
     *  parameter.
     *
     *  `point` has one or two coordinates depending on whether the pair is
     *  like or unlike, respectively.
     */
    private fun derivative(point: DoubleArray): Double =
        when (point.count()) {
            1 -> 1.0

            2 -> 4.0 / (1.0 + sqrt(point[0] / point[1])).pow(3)

            else -> throw IllegalArgumentException(
                "Point to evaluate the derivative at " +
                "does not have one or two coordinates."
            )
        }

    override fun value(point: Array<DerivativeStructure>):
        DerivativeStructure
    {
        val coords = point.map { it.value }.toDoubleArray()

        return when (point.count()) {
            1 -> DerivativeStructure(
                1,
                1,
                value(coords),
                derivative(coords)
            )

            2 -> DerivativeStructure(
                2,
                1,
                value(coords),
                derivative(coords),
                derivative(coords.reversedArray())
            )

            else -> throw IllegalArgumentException(
                "Point to evaluate the derivative at " +
                "does not have one or two coordinates."
            )
        }
    }
}
