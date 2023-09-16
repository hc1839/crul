package io.github.hc1839.crul.chemistry.tinker.force.vdw

import io.github.hc1839.crul.chemistry.tinker.AtomClass
import io.github.hc1839.crul.chemistry.tinker.AtomClassUnorderedPair

/**
 *  Default implementation of [VdwParamMapperCombo].
 */
internal class VdwParamMapperComboImpl<R, E> : VdwParamMapperCombo<R, E>
    where R : ComboRule,
          E : ComboRule
{
    override val rminComboRule: R

    override val epsilonComboRule: E

    /**
     *  Like-pair vdW parameters.
     */
    private val vdwParamGroups: Map<AtomClass, VdwParamGroup>

    /**
     *  See [VdwParamMapperCombo.newInstance] for the description of the
     *  parameters.
     */
    constructor(
        rminComboRule: R,
        epsilonComboRule: E,
        vdwParamGroups: Collection<VdwParamGroup>
    ) {
        if (
            vdwParamGroups.distinctBy { it.atomClass }.count()
                != vdwParamGroups.count()
        ) {
            throw IllegalArgumentException(
                "Atom classes are not unique."
            )
        }

        this.rminComboRule = rminComboRule
        this.epsilonComboRule = epsilonComboRule

        this.vdwParamGroups = vdwParamGroups.associateBy {
            it.atomClass
        }
    }

    override val atomClasses: Set<AtomClass>
        get() = vdwParamGroups.keys

    override fun rmin(atomClassPair: AtomClassUnorderedPair): Double {
        val atomClassList = atomClassPair.toList()

        for (atomClass in atomClassList) {
            if (!vdwParamGroups.containsKey(atomClass)) {
                throw IllegalArgumentException(
                    "No such atom class: ${atomClass.code}"
                )
            }
        }

        return if (atomClassList[0] == atomClassList[1]) {
            vdwParamGroups[atomClassList[0]]!!.rmin
        } else {
            rminComboRule.value(
                DoubleArray(2) { index ->
                    vdwParamGroups[atomClassList[index]]!!.rmin
                }
            )
        }
    }

    override fun epsilon(atomClassPair: AtomClassUnorderedPair): Double {
        val atomClassList = atomClassPair.toList()

        for (atomClass in atomClassList) {
            if (!vdwParamGroups.containsKey(atomClass)) {
                throw IllegalArgumentException(
                    "No such atom class: ${atomClass.code}"
                )
            }
        }

        return if (atomClassList[0] == atomClassList[1]) {
            vdwParamGroups[atomClassList[0]]!!.epsilon
        } else {
            epsilonComboRule.value(
                DoubleArray(2) { index ->
                    vdwParamGroups[atomClassList[index]]!!.epsilon
                }
            )
        }
    }

    override fun beta(atomClass: AtomClass): Double? {
        if (!vdwParamGroups.containsKey(atomClass)) {
            throw IllegalArgumentException(
                "No such atom class: ${atomClass.code}"
            )
        }

        return vdwParamGroups[atomClass]!!.beta
    }
}
