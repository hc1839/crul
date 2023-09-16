package io.github.hc1839.crul.chemistry.tinker.force.vdw

import kotlin.math.pow
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableFunction

import io.github.hc1839.crul.chemistry.species.Atom

/**
 *  VdW energy of two atoms with its first-order derivative.
 *
 *  @constructor
 *      See [BufferedPotential] for description of the parameters.
 */
open class DifferentiableBufferedPotential<R, E, A>(
    formSpec: VdwPotentialFormSpec,
    rminComboRule: R,
    epsilonComboRule: E,
    vdwSitePair: VdwSitePair<A>
) : BufferedPotential<R, E, A>(
        formSpec,
        rminComboRule,
        epsilonComboRule,
        vdwSitePair
    ),
    MultivariateDifferentiableFunction
    where R : DifferentiableComboRule,
          E : DifferentiableComboRule,
          A : Atom
{
    private val dFactor: DifferentiableDFactor<R, A> =
        DifferentiableDFactor(
            formSpec,
            rminComboRule,
            vdwSitePair
        )

    private val gFactor: DifferentiableGFactor<R, A> =
        DifferentiableGFactor(
            formSpec,
            rminComboRule,
            vdwSitePair
        )

    /**
     *  @param point
     *      Only `value` of `DerivativeStructure` is used.
     *
     *  @return
     *      Value and first-order derivative.
     */
    override fun value(point: Array<DerivativeStructure>):
        DerivativeStructure
    {
        if (point.count() != numCoords) {
            throw IllegalArgumentException(
                "Number of coordinates is not $numCoords: ${point.count()}"
            )
        }

        val coords = point.map { it.value }

        val rmins: List<Double>
        val epsilons: List<Double>
        val betas: List<Double>

        // Initialize the variables.
        if (vdwSitePair.isLikePair) {
            rmins = listOf(coords[0])
            epsilons = listOf(coords[1])
            betas = listOf(coords[2])
        } else {
            rmins = coords.slice(listOf(0, 3))
            epsilons = coords.slice(listOf(1, 4))
            betas = coords.slice(listOf(2, 5))
        }

        val factorCoords = rmins.zip(betas).flatMap { it.toList() }

        val factorArgs = Array(factorCoords.count()) { index ->
            DerivativeStructure(0, 0, factorCoords[index])
        }

        val dDerivStruct = dFactor.value(factorArgs)
        val gDerivStruct = gFactor.value(factorArgs)

        val firstDerivativesWrtRmin = listOf(dDerivStruct, gDerivStruct).map {
            derivStruct ->

            derivStruct.allDerivatives.drop(1).slice(
                0 until derivStruct.freeParameters step 2
            )
        }

        val dFirstDerivativesWrtRmin = firstDerivativesWrtRmin[0]
        val gFirstDerivativesWrtRmin = firstDerivativesWrtRmin[1]

        val firstDerivativesWrtBeta = listOf(dDerivStruct, gDerivStruct).map {
            derivStruct ->

            derivStruct.allDerivatives.drop(1).slice(
                1 until derivStruct.freeParameters step 2
            )
        }

        val dFirstDerivativesWrtBeta = firstDerivativesWrtBeta[0]
        val gFirstDerivativesWrtBeta = firstDerivativesWrtBeta[1]

        val epsilonDerivStruct = epsilonComboRule.value(
            Array(epsilons.count()) { index ->
                DerivativeStructure(0, 0, epsilons[index])
            }
        )

        val epsilon = epsilonDerivStruct.value

        val epsilonFirstDerivatives = epsilonDerivStruct
            .allDerivatives
            .drop(1)

        // Builder of the derivatives initialized with the zeroth-order
        // derivative.
        val derivatives = mutableListOf<Double>(
            value(coords.toDoubleArray())
        )

        for (coordIndex in coords.indices) {
            val firstDerivativeOffset = coordIndex / 3

            when (coordIndex % 3) {
                // Rmin.
                0 -> derivatives.add(
                    epsilon * (
                        gDerivStruct.value *
                            dFirstDerivativesWrtRmin[firstDerivativeOffset] +
                        dDerivStruct.value *
                            gFirstDerivativesWrtRmin[firstDerivativeOffset]
                    )
                )

                // Epsilon.
                1 -> derivatives.add(
                    dDerivStruct.value *
                        gDerivStruct.value *
                        epsilonFirstDerivatives[firstDerivativeOffset]
                )

                // Beta.
                2 -> derivatives.add(
                    epsilon * (
                        gDerivStruct.value *
                            dFirstDerivativesWrtBeta[firstDerivativeOffset] +
                        dDerivStruct.value *
                            gFirstDerivativesWrtBeta[firstDerivativeOffset]
                    )
                )

                else -> { }
            }
        }

        return DerivativeStructure(
            coords.count(),
            1,
            *derivatives.toDoubleArray()
        )
    }
}
