package io.github.hc1839.crul.chemistry.tinker.force.vdw

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableVectorFunction
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.apache.math.vector.*
import io.github.hc1839.crul.chemistry.species.Atom

/**
 *  Three-dimensional position of a vdW site as a function of the beta
 *  parameter.
 *
 *  @property atom
 *      Atom that is evaluated for its vdW site.
 *
 *  @property bondVector
 *      Displacement from `atom` to the atom that is bonded to it if `atom` is
 *      univalent, or `null` otherwise.
 *
 *  @constructor
 */
open class VdwSite<A : Atom>(
    val atom: A,
    val bondVector: Vector3D?
) : UnivariateDifferentiableVectorFunction
{
    /**
     *  Evaluates the position of a vdW site.
     *
     *  @param point
     *      Beta parameter. If [bondVector] is not `null`, it must be between
     *      `0.0` and `1.0`, inclusive. If [bondVector] is `null`, it is
     *      ignored.
     *
     *  @return
     *      Components of the position of the vdW site.
     */
    override fun value(point: Double): DoubleArray =
        if (bondVector != null) {
            if (point < 0.0 || point > 1.0) {
                throw IllegalArgumentException(
                    "Value of the beta parameter " +
                    "is not between 0.0 and 1.0, inclusive."
                )
            }

            val vdwSite = atom.position + bondVector * (1.0 - point)

            vdwSite.toArray()
        } else {
            atom.position.toArray()
        }

    /**
     *  VdW site and its first-order derivative.
     *
     *  @param point
     *      Only `value` of `DerivativeStructure` is used.
     *
     *  @return
     *      Components of the position of the vdW site up to and including its
     *      first-order derivative.
     */
    override fun value(point: DerivativeStructure):
        Array<DerivativeStructure>
    {
        val beta = point.value

        val zerothDerivative = value(beta)

        val firstDerivative = if (bondVector != null) {
            (-bondVector).toArray()
        } else {
            DoubleArray(3) { 0.0 }
        }

        return Array(3) { index ->
            DerivativeStructure(
                1,
                1,
                zerothDerivative[index],
                firstDerivative[index]
            )
        }
    }
}
