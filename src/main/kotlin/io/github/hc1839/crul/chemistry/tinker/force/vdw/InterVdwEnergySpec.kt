package io.github.hc1839.crul.chemistry.tinker.force.vdw

/**
 *  Specification for [InterVdwEnergy].
 *
 *  @property formSpec
 *      Constants specifying the vdW functional form.
 *
 *  @property rminComboRule
 *      Combination rule for Rmin.
 *
 *  @property epsilonComboRule
 *      Combination rule for epsilon.
 *
 *  @constructor
 */
data class InterVdwEnergySpec<R, E>(
    val formSpec: VdwPotentialFormSpec,
    val rminComboRule: R,
    val epsilonComboRule: E
) where R : ComboRule,
        E : ComboRule
