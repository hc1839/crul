package io.github.hc1839.crul.chemistry.tinker.force.vdw

import io.github.hc1839.crul.chemistry.tinker.AtomClass
import io.github.hc1839.crul.chemistry.tinker.force.vdw.VdwParamGroup

/**
 *  Grouper that groups a list of vdW-parameter values into [VdwParamGroup]
 *  instances.
 *
 *  @property isUnivalent
 *      Whether an atom class is univalent.
 *
 *  @constructor
 */
class VdwParamGrouper(
    private val isUnivalent: (AtomClass) -> Boolean
) {
    /**
     *  Groups a list of values into a list of [VdwParamGroup] instances.
     *
     *  @param atomClasses
     *      Atom classes of the vdW-parameter values being grouped.
     *
     *  @param values
     *      VdW-parameter values in the same order as `atomClasses` to group.
     *      For each atom class, two or three vdW-parameter values in order are
     *      expected: Rmin, epsilon, and (if univalent) beta. In other words,
     *      the first two or three values are for the first atom class, the
     *      second two or three values are for the second atom class, and so
     *      on. If the number of values is not equal to the expected number, an
     *      exception is raised.
     *
     *  @return
     *      Grouped vdW-parameter values in the same order as `atomClasses`.
     */
    fun group(
        atomClasses: List<AtomClass>,
        values: List<Double>
    ): List<VdwParamGroup>
    {
        val numValues = atomClasses.map {
            if (isUnivalent.invoke(it)) {
                3
            } else {
                2
            }
        }.sum()

        if (values.count() != numValues) {
            throw IllegalArgumentException(
                "Number of values to group is not equal " +
                "to the expected number: " +
                "${values.count()} for $numValues"
            )
        }

        val vdwParamGroups = mutableListOf<VdwParamGroup>()
        var remainingValues = values

        for (atomClass in atomClasses) {
            val groupSize = if (isUnivalent(atomClass)) {
                3
            } else {
                2
            }

            val vdwParamValues = remainingValues.take(groupSize)
            remainingValues = remainingValues.drop(groupSize)

            val rmin = vdwParamValues[0]
            val epsilon = vdwParamValues[1]
            val beta = vdwParamValues.getOrNull(2)

            vdwParamGroups.add(
                VdwParamGroup(
                    atomClass = atomClass,
                    rmin = rmin,
                    epsilon = epsilon,
                    beta = beta
                )
            )
        }

        return vdwParamGroups
    }
}
