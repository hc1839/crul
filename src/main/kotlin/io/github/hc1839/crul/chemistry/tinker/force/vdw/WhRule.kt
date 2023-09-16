package io.github.hc1839.crul.chemistry.tinker.force.vdw

import kotlin.math.pow
import kotlin.math.sqrt

import io.github.hc1839.crul.chemistry.tinker.AtomClass

/**
 *  W-H rule for epsilon.
 */
object WhRule: ComboRule {
    override fun value(point: DoubleArray): Double =
        when (point.count()) {
            1 -> point[0]

            else -> throw IllegalArgumentException(
                "W-H combination rule can only be evaluated as an Apache " +
                "SAM for like pairs."
            )
        }

    override fun value(a: VdwParamGroup, b: VdwParamGroup): Double {
        return if (a == b) {
            a.epsilon
        } else {
            2.0 * sqrt(a.epsilon * b.epsilon) *
                a.rmin.pow(3) * b.rmin.pow(3) /
                (a.rmin.pow(6) + b.rmin.pow(6))
        }
    }
}
