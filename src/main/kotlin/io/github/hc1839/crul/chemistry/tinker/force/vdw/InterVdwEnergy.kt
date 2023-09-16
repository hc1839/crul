package io.github.hc1839.crul.chemistry.tinker.force.vdw

import org.apache.commons.math3.analysis.MultivariateFunction

import io.github.hc1839.crul.apache.math.vector.*
import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.FragmentedSupermolecule
import io.github.hc1839.crul.distinct.Referential
import io.github.hc1839.crul.chemistry.species.crossAtomPairs
import io.github.hc1839.crul.chemistry.tinker.AtomClass
import io.github.hc1839.crul.chemistry.tinker.AtomClassMapper
import io.github.hc1839.crul.chemistry.tinker.AtomClassOrderedPair

/**
 *  Interfragment-vdW (inter-vdW) energy of a supermolecule.
 *
 *  Intrafragment vdW is ignored.
 *
 *  @property spec
 *      Specification for the function to instantiate.
 *
 *  @property atomClassMapper
 *      Atom-class mapper.
 *
 *  @property supermol
 *      Fragmented supermolecule whose inter-vdW energy is evaluated. It is not
 *      cloned.
 *
 *  @constructor
 */
open class InterVdwEnergy<R, E, A>(
    val spec: InterVdwEnergySpec<R, E>,
    val atomClassMapper: AtomClassMapper<A>,
    val supermol: FragmentedSupermolecule<A>
) : MultivariateFunction
    where R : ComboRule,
          E : ComboRule,
          A : Atom
{
    /**
     *  Atom classes associated by wrapped atom.
     */
    protected val atomClassesByAtom: Map<Referential<A>, AtomClass>

    /**
     *  VdW sites associated by wrapped atom.
     */
    protected val vdwSitesByAtom: Map<Referential<A>, VdwSite<A>>

    init {
        val wrappedAtoms = supermol.atoms.map { Referential(it) }

        this.atomClassesByAtom = wrappedAtoms.associateWith { wrappedAtom ->
            atomClassMapper.invoke(wrappedAtom.value, supermol)
        }

        this.vdwSitesByAtom = this.atomClassesByAtom.mapValues {
            (wrappedAtom, atomClass) ->

            val atom = wrappedAtom.value

            val bondVector = if (atomClassMapper.isUnivalent(atomClass)) {
                val bond = supermol
                    .getIslandWithAtom(atom)
                    .getBondsByAtom(atom)
                    .singleOrNull()

                if (bond == null) {
                    throw RuntimeException(
                        "Atom class $atomClass is univalent but an atom " +
                        "is not bonded to exactly one other atom."
                    )
                }

                val bondedAtom = bond
                    .toAtomPair()
                    .toList()
                    .first { it != atom }

                bondedAtom.position - atom.position
            } else {
                null
            }

            VdwSite(atom, bondVector)
        }
    }

    /**
     *  Atom classes of the atoms of the supermolecule sorted by atom-class
     *  code.
     */
    val atomClasses: List<AtomClass>
        get() = atomClassesByAtom.values.sortedBy { it.code }

    /**
     *  Grouper for the vdW-parameter values passed as a `DoubleArray` to
     *  [value].
     */
    val vdwParamGrouper: VdwParamGrouper
        get() = VdwParamGrouper() {
            atomClassMapper.isUnivalent(it)
        }

    /**
     *  Evaluates the inter-vdW energy of [supermol].
     *
     *  @param vdwParamGroups
     *      VdW-parameter groups. All atom classes of [supermol] must have a
     *      vdW-parameter group in this collection. The presence of a beta
     *      parameter must match the univalency of the corresponding atom
     *      class.
     *
     *  @return
     *      Inter-vdW energy of [supermol].
     */
    fun value(vdwParamGroups: Collection<VdwParamGroup>): Double {
        val vdwParamMapperCombo = VdwParamMapperCombo(
            spec.rminComboRule,
            spec.epsilonComboRule,
            vdwParamGroups
        )

        var interVdwEnergyBuilder = 0.0

        // Build the inter-vdW energies.
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

            val potential = BufferedPotential(
                spec.formSpec,
                spec.rminComboRule,
                spec.epsilonComboRule,
                vdwSitePair
            )

            interVdwEnergyBuilder += potential.value(
                vdwParamGroupPair.first,
                vdwParamGroupPair.second
            )
        }

        return interVdwEnergyBuilder
    }

    /**
     *  Inter-vdW energy of the supermolecule.
     *
     *  @param point
     *      VdW parameters for the atom classes in the same order as
     *      [atomClasses]. Values are grouped into [VdwParamGroup] instances by
     *      [vdwParamGrouper]. See [VdwParamGrouper.group] for the description
     *      of the expected order of the vdW-parameter values.
     *
     *  @return
     *      Inter-vdW energy of the supermolecule.
     */
    override fun value(point: DoubleArray): Double =
        value(vdwParamGrouper.group(atomClasses, point.toList()))
}
