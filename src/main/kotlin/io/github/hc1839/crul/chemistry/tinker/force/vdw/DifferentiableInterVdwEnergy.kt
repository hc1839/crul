package io.github.hc1839.crul.chemistry.tinker.force.vdw

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableFunction

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.FragmentedSupermolecule
import io.github.hc1839.crul.distinct.Referential
import io.github.hc1839.crul.chemistry.species.crossAtomPairs
import io.github.hc1839.crul.chemistry.tinker.AtomClassMapper
import io.github.hc1839.crul.chemistry.tinker.AtomClassOrderedPair

/**
 *  Intermolecular vdW (inter-vdW) energy of a supermolecule with its
 *  first-order derivative.
 *
 *  Intramolecular vdW is ignored.
 *
 *  @constructor
 *      See [InterVdwEnergy] for description of the parameters.
 */
open class DifferentiableInterVdwEnergy<R, E, A>(
    spec: InterVdwEnergySpec<R, E>,
    atomClassMapper: AtomClassMapper<A>,
    supermol: FragmentedSupermolecule<A>
) : InterVdwEnergy<R, E, A>(
        spec,
        atomClassMapper,
        supermol
    ),
    MultivariateDifferentiableFunction
    where R : DifferentiableComboRule,
          E : DifferentiableComboRule,
          A : Atom
{
    /**
     *  Inter-vdW energy and its first-order derivatives of the supermolecule.
     *
     *  @param vdwParamGroups
     *      VdW-parameter groups. All atom classes of [supermol] must have a
     *      vdW-parameter group in this collection. The presence of a beta
     *      parameter must match the univalency of the corresponding atom
     *      class.
     *
     *  @return
     *      Inter-vdW energy and its first-order derivatives of the
     *      supermolecule.
     */
    fun derivativeStructure(vdwParamGroups: Collection<VdwParamGroup>):
        DerivativeStructure
    {
        val vdwParamMapperCombo = VdwParamMapperCombo(
            spec.rminComboRule,
            spec.epsilonComboRule,
            vdwParamGroups
        )

        var interVdwEnergyBuilder = 0.0

        // Builder of first-order derivatives with respect to Rmin, epsilon,
        // and beta, respectively, for each atom class.
        val firstDerivativesBuilder = atomClasses
            .associateWith { listOf(0.0, 0.0, 0.0) }
            .toMutableMap()

        // Build the zeroth- and first-order derivatives.
        for (atomPair in supermol.crossAtomPairs()) {
            val wrappedAtomPair = Pair(
                Referential(atomPair.first),
                Referential(atomPair.second)
            )

            val atomClassPair = AtomClassOrderedPair(
                atomClassesByAtom[wrappedAtomPair.first]!!,
                atomClassesByAtom[wrappedAtomPair.second]!!
            )

            val vdwParamGroupPair = Pair(
                vdwParamMapperCombo.getParamGroup(atomClassPair.first),
                vdwParamMapperCombo.getParamGroup(atomClassPair.second)
            )

            val vdwSitePair = VdwSitePair(
                vdwSitesByAtom[wrappedAtomPair.first]!!,
                vdwSitesByAtom[wrappedAtomPair.second]!!,
                atomClassPair.first == atomClassPair.second
            )

            // Validate the presence or absence of each beta parameter.
            for (
                (vdwParamGroup, vdwSite) in
                vdwParamGroupPair.toList().zip(vdwSitePair.toList())
            ) {
                if (
                    (vdwParamGroup.beta == null) xor
                    (vdwSite.bondVector == null)
                ) {
                    throw RuntimeException(
                        "Presence of beta parameter does not match " +
                        "the univalency of a vdW site."
                    )
                }
            }

            val potential = DifferentiableBufferedPotential(
                spec.formSpec,
                spec.rminComboRule,
                spec.epsilonComboRule,
                vdwSitePair
            )

            // Coordinates to evaluate the potential at for the atom pair.
            val potentialCoords = if (potential.vdwSitePair.isLikePair) {
                listOf(
                    vdwParamGroupPair.first.rmin,
                    vdwParamGroupPair.first.epsilon,
                    vdwParamGroupPair.first.beta ?: 1.0
                )
            } else {
                vdwParamGroupPair
                    .toList()
                    .flatMap { vdwParamGroup ->
                        listOf(
                            vdwParamGroup.rmin,
                            vdwParamGroup.epsilon,
                            vdwParamGroup.beta ?: 1.0
                        )
                    }
            }

            // Zeroth- and first-order derivatives for the atom pair.
            val derivativeStructure = potential.value(
                Array(potentialCoords.count()) { index ->
                    DerivativeStructure(0, 0, potentialCoords[index])
                }
            )

            // Add the zeroth-order derivative.
            interVdwEnergyBuilder += derivativeStructure.value

            // Add the first-order derivatives.
            for (
                (derivatives, atomClass) in
                derivativeStructure
                    .allDerivatives
                    .drop(1)
                    .chunked(3)
                    .zip(atomClassPair.toList())
            ) {
                firstDerivativesBuilder[atomClass] =
                    firstDerivativesBuilder[atomClass]!!.zip(derivatives) {
                        a, b -> a + b
                    }
            }
        }

        val firstDerivatives = atomClasses.flatMap {
            firstDerivativesBuilder[it]!!
        }

        val numValues = vdwParamGroups.map {
            if (it.beta == null) {
                2
            } else {
                3
            }
        }.sum()

        return DerivativeStructure(
            numValues,
            1,
            interVdwEnergyBuilder,
            *DoubleArray(firstDerivatives.count()) { index ->
                firstDerivatives[index]
            }
        )
    }

    /**
     *  Inter-vdW energy and its first-order derivatives of the supermolecule.
     *
     *  @param point
     *      Only `value` of `DerivativeStructure` is used. Components are in
     *      the same order as [InterVdwEnergy.value].
     *
     *  @return
     *      Inter-vdW energy and its first-order derivatives of the
     *      supermolecule.
     */
    override fun value(point: Array<DerivativeStructure>):
        DerivativeStructure
    {
        val coords = point.map { it.value }

        return derivativeStructure(
            vdwParamGrouper.group(atomClasses, coords)
        )
    }
}
