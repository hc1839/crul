package io.github.hc1839.crul.chemistry.tinker.force.vdw

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.apache.math.vector.*
import io.github.hc1839.crul.chemistry.species.Atom

/**
 *  Pair of vdW sites that can be evaluated as the displacement from the first
 *  to the second.
 *
 *  @property first
 *      First vdW site.
 *
 *  @property second
 *      Second vdW site.
 *
 *  @property isLikePair
 *      Whether the pair of atoms is a like pair.
 *
 *  @constructor
 */
data class VdwSitePair<A : Atom>(
    val first: VdwSite<A>,
    val second: VdwSite<A>,
    val isLikePair: Boolean
) : MultivariateDifferentiableVectorFunction
{
    /**
     *  Number of coordinates that [value] expects.
     */
    val numCoords: Int =
        if (isLikePair) {
            1
        } else {
            2
        }

    /**
     *  Displacement from the first vdW site to the second.
     *
     *  @param point
     *      Each coordinate is a beta parameter for the respective vdW site.
     *      If for a like pair, one coordinate is expected. If for an unlike
     *      pair, two coordinates are expected. The expected number of
     *      coordinates can be determined by [numCoords].
     *
     *  @return
     *      Components of the displacement.
     */
    override fun value(point: DoubleArray): DoubleArray {
        if (point.count() != numCoords) {
            throw IllegalArgumentException(
                "Number of coordinates is not $numCoords: ${point.count()}"
            )
        }

        val betas = if (isLikePair) {
            listOf(point[0], point[0])
        } else {
            point.toList()
        }

        val vdwSites = toList().zip(betas) { vdwSite, beta ->
            Vector3D(vdwSite.value(beta))
        }

        return (vdwSites[1] - vdwSites[0]).toArray()
    }

    /**
     *  Displacement and its first-order derivative from the first vdW site to
     *  the second.
     *
     *  @param point
     *      Only `value` of `DerivativeStructure` is used.
     *
     *  @return
     *      Value and first-order derivative of the components of the
     *      displacement.
     */
    override fun value(point: Array<DerivativeStructure>):
        Array<DerivativeStructure>
    {
        if (point.count() != numCoords) {
            throw IllegalArgumentException(
                "Number of coordinates is not $numCoords: ${point.count()}"
            )
        }

        val betas = if (isLikePair) {
            listOf(point[0].value, point[0].value)
        } else {
            point.map { it.value }
        }

        val vdwSiteDerivatives = toList().zip(betas) { vdwSite, beta ->
            vdwSite.value(DerivativeStructure(0, 0, beta))
        }

        return Array(3) { index ->
            vdwSiteDerivatives[1][index].subtract(
                vdwSiteDerivatives[0][index]
            )
        }
    }

    /**
     *  Converts to a two-element list of [VdwSite].
     */
    fun toList(): List<VdwSite<A>> =
        listOf(first, second)
}
