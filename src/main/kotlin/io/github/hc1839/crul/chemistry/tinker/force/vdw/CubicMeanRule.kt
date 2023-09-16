package io.github.hc1839.crul.chemistry.tinker.force.vdw

import kotlin.math.pow

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure

import io.github.hc1839.crul.chemistry.tinker.AtomClass

/**
 *  Cubic-mean rule for Rmin.
 */
object CubicMeanRule : DifferentiableComboRule {
    override fun value(point: DoubleArray): Double =
        when (point.count()) {
            1 -> point[0]

            2 -> {
                val coords = point.toList()
                val coordsSquared = coords.map { it.pow(2) }

                val numerator = coordsSquared[0] * coords[0] +
                    coordsSquared[1] * coords[1]

                val denominator = coordsSquared[0] + coordsSquared[1]

                numerator / denominator
            }

            else -> throw IllegalArgumentException(
                "Not one or two coordinates are given."
            )
        }

    override fun value(a: VdwParamGroup, b: VdwParamGroup): Double {
        return if (a.atomClass == b.atomClass) {
            a.rmin
        } else {
            value(
                listOf(a.rmin, b.rmin).toDoubleArray()
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

            2 -> {
                val secondDivFirst = point[1] / point[0]
                val secondDivFirstSquared = secondDivFirst.pow(2)

                val firstTerm = 3.0 / (1.0 + secondDivFirstSquared)

                val secondTerm = -2.0 *
                    (1.0 + secondDivFirstSquared * secondDivFirst) /
                    (1.0 + secondDivFirstSquared).pow(2)

                firstTerm + secondTerm
            }

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
