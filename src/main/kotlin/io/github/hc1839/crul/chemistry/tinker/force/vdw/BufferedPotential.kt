package io.github.hc1839.crul.chemistry.tinker.force.vdw

import kotlin.math.pow
import org.apache.commons.math3.analysis.MultivariateFunction

import io.github.hc1839.crul.chemistry.species.Atom

/**
 *  VdW energy of two atoms using the functional form of the buffered potential
 *  in Halgren 1992.
 *
 *  @property formSpec
 *      Constants specifying the form.
 *
 *  @property rminComboRule
 *      Combination rule for Rmin.
 *
 *  @property epsilonComboRule
 *      Combination rule for epsilon.
 *
 *  @property vdwSitePair
 *      Pair of vdW sites.
 *
 *  @constructor
 */
open class BufferedPotential<R, E, A>(
    val formSpec: VdwPotentialFormSpec,
    val rminComboRule: R,
    val epsilonComboRule: E,
    val vdwSitePair: VdwSitePair<A>
) : MultivariateFunction
    where R : ComboRule,
          E : ComboRule,
          A : Atom
{
    private val dFactor: DFactor<R, A> =
        DFactor(
            formSpec,
            rminComboRule,
            vdwSitePair
        )

    private val gFactor: GFactor<R, A> =
        GFactor(
            formSpec,
            rminComboRule,
            vdwSitePair
        )

    /**
     *  Number of coordinates that [value] expects.
     */
    open val numCoords: Int =
        if (vdwSitePair.isLikePair) {
            3
        } else {
            6
        }

    /**
     *  It is for compatibility with Apache Commons Math. For more robust
     *  evaluation, use the overloaded function that relies on atom classes.
     *
     *  @param point
     *      If the two atoms are like-pair, the coordinates in order are Rmin,
     *      epsilon, and beta. If the two atoms are not like-pair, the
     *      coordinates in order are Rmin of first, epsilon of first, beta of
     *      first, Rmin of second, epsilon of second, and beta of second. The
     *      expected number of coordinates can be determined by [numCoords].
     *
     *  @return
     *      Value of the buffered potential.
     */
    override fun value(point: DoubleArray): Double {
        if (point.count() != numCoords) {
            throw IllegalArgumentException(
                "Number of coordinates is not $numCoords: ${point.count()}"
            )
        }

        val rmins: List<Double>
        val epsilon: Double
        val betas: List<Double>

        // Initialize the variables.
        if (vdwSitePair.isLikePair) {
            rmins = listOf(point[0])
            epsilon = point[1]
            betas = listOf(point[2])
        } else {
            rmins = point.slice(listOf(0, 3))
            epsilon = epsilonComboRule.value(point.sliceArray(listOf(1, 4)))
            betas = point.slice(listOf(2, 5))
        }

        val factorArgs = rmins
            .zip(betas)
            .flatMap { it.toList() }
            .toDoubleArray()

        return epsilon *
            dFactor.value(factorArgs) *
            gFactor.value(factorArgs)
    }

    /**
     *  Value of the buffered potential.
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
     *      Value of the buffered potential.
     */
    fun value(a: VdwParamGroup, b: VdwParamGroup): Double {
        val rmins: List<Double>
        val epsilon: Double
        val betas: List<Double>

        // Initialize the variables.
        if (vdwSitePair.isLikePair) {
            rmins = listOf(a.rmin)
            epsilon = a.epsilon
            betas = listOf(a.beta ?: 1.0)
        } else {
            rmins = listOf(a.rmin, b.rmin)
            epsilon = epsilonComboRule.value(a, b)
            betas = listOf(a.beta ?: 1.0, b.beta ?: 1.0)
        }

        val factorArgs = rmins
            .zip(betas)
            .flatMap { it.toList() }
            .toDoubleArray()

        return epsilon *
            dFactor.value(factorArgs) *
            gFactor.value(factorArgs)
    }
}
