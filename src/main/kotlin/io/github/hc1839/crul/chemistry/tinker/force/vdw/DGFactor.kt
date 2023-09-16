package io.github.hc1839.crul.chemistry.tinker.force.vdw

import kotlin.math.pow
import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableFunction
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.chemistry.species.Atom

/**
 *  Representation of either the `d` ([DFactor]) or `g` ([GFactor]) factor.
 *
 *  See [BufferedPotential] for description of the parameters.
 */
internal sealed class DGFactor<R, A>(
    val formSpec: VdwPotentialFormSpec,
    val rminComboRule: R,
    val vdwSitePair: VdwSitePair<A>
) : MultivariateFunction
    where R : ComboRule,
          A : Atom
{
    /**
     *  Number of coordinates that [value] expects.
     */
    val numCoords: Int =
        if (vdwSitePair.isLikePair) {
            2
        } else {
            4
        }

    /**
     *  Value of this factor given `rho`.
     */
    protected abstract fun value(rho: Double): Double

    /**
     *  @param point
     *      If the two atoms are like-pair, the coordinates in order are Rmin
     *      and beta. If the two atoms are not like-pair, the coordinates in
     *      order are Rmin of first, beta of first, Rmin of second, and beta of
     *      second. The expected number of coordinates can be determined by
     *      [numCoords].
     *
     *  @return
     *      Value of this factor.
     */
    override fun value(point: DoubleArray): Double {
        if (point.count() != numCoords) {
            throw IllegalArgumentException(
                "Number of coordinates is not $numCoords: ${point.count()}"
            )
        }

        val rmin: Double
        val betas: DoubleArray

        // Initialize the variables.
        if (vdwSitePair.isLikePair) {
            rmin = point[0]
            betas = listOf(point[1]).toDoubleArray()
        } else {
            rmin = rminComboRule.value(point.sliceArray(listOf(0, 2)))
            betas = point.sliceArray(listOf(1, 3))
        }

        val vdwSiteDistance = Vector3D(vdwSitePair.value(betas)).norm
        val rho = vdwSiteDistance / rmin

        return value(rho)
    }
}

/**
 *  Parenthetical factor that contains the `delta` constants in the buffered
 *  potential.
 *
 *  See [BufferedPotential] for description of the parameters.
 */
internal open class DFactor<R, A>(
    formSpec: VdwPotentialFormSpec,
    rminComboRule: R,
    vdwSitePair: VdwSitePair<A>
) : DGFactor<R, A>(
        formSpec,
        rminComboRule,
        vdwSitePair
    )
    where R : ComboRule,
          A : Atom
{
    override fun value(rho: Double): Double = (
        (1.0 + formSpec.bufs.delta) / (rho + formSpec.bufs.delta)
    ).pow(
        formSpec.exps.n - formSpec.exps.m
    )
}

/**
 *  `d` factor with its first-order derivative.
 */
internal open class DifferentiableDFactor<R, A>(
    formSpec: VdwPotentialFormSpec,
    rminComboRule: R,
    vdwSitePair: VdwSitePair<A>
) : DFactor<R, A>(
        formSpec,
        rminComboRule,
        vdwSitePair
    ),
    MultivariateDifferentiableFunction
    where R : DifferentiableComboRule,
          A : Atom
{

    /**
     *  First-order derivatives with respect to the Rmin parameters.
     *
     *  @param rmins
     *      Rmin parameters. It has one or two coordinates depending on whether
     *      the pair is like or unlike, respectively.
     *
     *  @param betas
     *      Beta parameters for the two vdW sites, respectively.
     *
     *  @return
     *      First-order derivatives with respect to the Rmin parameters in the
     *      same order as `rmins`.
     */
    private fun derivativesRmin(
        rmins: DoubleArray,
        betas: DoubleArray
    ): List<Double>
    {
        val n = formSpec.exps.n
        val m = formSpec.exps.m
        val delta = formSpec.bufs.delta

        val rminDerivStruct = rminComboRule.value(
            Array(rmins.count()) { index ->
                DerivativeStructure(0, 0, rmins[index])
            }
        )

        val vdwSiteDispl = Vector3D(vdwSitePair.value(betas))
        val rho = vdwSiteDispl.norm / rminDerivStruct.value

        val commonFactor = (n - m).toDouble() *
            (1.0 + delta).pow(n - m) /
            (rho + delta).pow(n - m + 1) *
            rho / rminDerivStruct.value

        return rminDerivStruct.allDerivatives.drop(1).map {
            commonFactor * it
        }
    }

    /**
     *  First-order derivatives with respect to the beta parameters.
     *
     *  @param rmins
     *      Rmin parameters. It has one or two coordinates depending on whether
     *      the pair is like or unlike, respectively.
     *
     *  @param betas
     *      Beta parameters for the two vdW sites, respectively.
     *
     *  @return
     *      First-order derivatives with respect to the beta parameters in the
     *      same order as `betas`.
     */
    private fun derivativesBeta(
        rmins: DoubleArray,
        betas: DoubleArray
    ): List<Double>
    {
        val n = formSpec.exps.n
        val m = formSpec.exps.m
        val delta = formSpec.bufs.delta

        val vdwSiteDisplDerivStructs = vdwSitePair
            .value(
                Array(betas.count()) { index ->
                    DerivativeStructure(0, 0, betas[index])
                }
            )
            .toList()

        val vdwSiteDispl = Vector3D(
            vdwSiteDisplDerivStructs.map { it.value }.toDoubleArray()
        )

        val vdwSiteDisplDerivatives = listOf(
            Vector3D(
                vdwSiteDisplDerivStructs.map {
                    it.getPartialDerivative(1, 0)
                }.toDoubleArray()
            ),
            Vector3D(
                vdwSiteDisplDerivStructs.map {
                    it.getPartialDerivative(0, 1)
                }.toDoubleArray()
            )
        )

        val rmin = rminComboRule.value(rmins)
        val rho = vdwSiteDispl.norm / rmin

        val commonFactor = (n - m).toDouble() *
            (1.0 + delta).pow(n - m) /
            (rho + delta).pow(n - m + 1) *
            -1.0 / rmin

        return vdwSiteDisplDerivatives.map {
            commonFactor * it.dotProduct(vdwSiteDispl.normalize())
        }
    }

    override fun value(point: Array<DerivativeStructure>):
        DerivativeStructure
    {
        val coords = point.map { it.value }

        val rmins: DoubleArray
        val betas: DoubleArray

        if (vdwSitePair.isLikePair) {
            rmins = DoubleArray(1) { coords[0] }
            betas = DoubleArray(1) { coords[1] }
        } else {
            rmins = coords.slice(listOf(0, 2)).toDoubleArray()
            betas = coords.slice(listOf(1, 3)).toDoubleArray()
        }

        val derivatives = listOf(value(coords.toDoubleArray())) +
            derivativesRmin(rmins, betas)
                .zip(derivativesBeta(rmins, betas))
                .flatMap { it.toList() }

        return DerivativeStructure(
            numCoords,
            1,
            *derivatives.toDoubleArray()
        )
    }
}

/**
 *  Parenthetical factor that contains the `gamma` constants in the buffered
 *  potential.
 *
 *  See [BufferedPotential] for description of the parameters.
 */
internal open class GFactor<R, A>(
    formSpec: VdwPotentialFormSpec,
    rminComboRule: R,
    vdwSitePair: VdwSitePair<A>
) : DGFactor<R, A>(
        formSpec,
        rminComboRule,
        vdwSitePair
    )
    where R : ComboRule,
          A : Atom
{
    override fun value(rho: Double): Double =
        (1.0 + formSpec.bufs.gamma) /
            (rho.pow(formSpec.exps.m) + formSpec.bufs.gamma) - 2
}

/**
 *  `g` factor with its first-order derivative.
 */
internal open class DifferentiableGFactor<R, A>(
    formSpec: VdwPotentialFormSpec,
    rminComboRule: R,
    vdwSitePair: VdwSitePair<A>
) : GFactor<R, A>(
        formSpec,
        rminComboRule,
        vdwSitePair
    ),
    MultivariateDifferentiableFunction
    where R : DifferentiableComboRule,
          A : Atom
{

    /**
     *  First-order derivatives with respect to the Rmin parameters.
     *
     *  @param rmins
     *      Rmin parameters. It has one or two coordinates depending on whether
     *      the pair is like or unlike, respectively.
     *
     *  @param betas
     *      Beta parameters for the two vdW sites, respectively.
     *
     *  @return
     *      First-order derivatives with respect to the Rmin parameters in the
     *      same order as `rmins`.
     */
    private fun derivativesRmin(
        rmins: DoubleArray,
        betas: DoubleArray
    ): List<Double>
    {
        val m = formSpec.exps.m
        val gamma = formSpec.bufs.gamma

        val rminDerivStruct = rminComboRule.value(
            Array(rmins.count()) { index ->
                DerivativeStructure(0, 0, rmins[index])
            }
        )

        val vdwSiteDispl = Vector3D(vdwSitePair.value(betas))
        val rho = vdwSiteDispl.norm / rminDerivStruct.value

        val commonFactor = m.toDouble() *
            (1.0 + gamma) /
            (rho.pow(m) + gamma).pow(2) *
            rho.pow(m) / rminDerivStruct.value

        return rminDerivStruct.allDerivatives.drop(1).map {
            commonFactor * it
        }
    }

    /**
     *  First-order derivatives with respect to the beta parameters.
     *
     *  @param rmins
     *      Rmin parameters. It has one or two coordinates depending on whether
     *      the pair is like or unlike, respectively.
     *
     *  @param betas
     *      Beta parameters for the two vdW sites, respectively.
     *
     *  @return
     *      First-order derivatives with respect to the beta parameters in the
     *      same order as `betas`.
     */
    private fun derivativesBeta(
        rmins: DoubleArray,
        betas: DoubleArray
    ): List<Double>
    {
        val m = formSpec.exps.m
        val gamma = formSpec.bufs.gamma

        val vdwSiteDisplDerivStructs = vdwSitePair
            .value(
                Array(betas.count()) { index ->
                    DerivativeStructure(0, 0, betas[index])
                }
            )
            .toList()

        val vdwSiteDispl = Vector3D(
            vdwSiteDisplDerivStructs.map { it.value }.toDoubleArray()
        )

        val vdwSiteDisplDerivatives = listOf(
            Vector3D(
                vdwSiteDisplDerivStructs.map {
                    it.getPartialDerivative(1, 0)
                }.toDoubleArray()
            ),
            Vector3D(
                vdwSiteDisplDerivStructs.map {
                    it.getPartialDerivative(0, 1)
                }.toDoubleArray()
            )
        )

        val rmin = rminComboRule.value(rmins)
        val rho = vdwSiteDispl.norm / rmin

        val commonFactor = m.toDouble() *
            (1.0 + gamma) /
            (rho.pow(m) + gamma).pow(2) *
            -rho.pow(m - 1) / rmin

        return vdwSiteDisplDerivatives.map {
            commonFactor * it.dotProduct(vdwSiteDispl.normalize())
        }
    }

    override fun value(point: Array<DerivativeStructure>):
        DerivativeStructure
    {
        val coords = point.map { it.value }

        val rmins: DoubleArray
        val betas: DoubleArray

        if (vdwSitePair.isLikePair) {
            rmins = DoubleArray(1) { coords[0] }
            betas = DoubleArray(1) { coords[1] }
        } else {
            rmins = coords.slice(listOf(0, 2)).toDoubleArray()
            betas = coords.slice(listOf(1, 3)).toDoubleArray()
        }

        val derivatives = listOf(value(coords.toDoubleArray())) +
            derivativesRmin(rmins, betas)
                .zip(derivativesBeta(rmins, betas))
                .flatMap { it.toList() }

        return DerivativeStructure(
            numCoords,
            1,
            *derivatives.toDoubleArray()
        )
    }
}
