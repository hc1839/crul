@file:JvmName("VdwParamMapperExt")
@file:JvmMultifileClass

package io.github.hc1839.crul.chemistry.tinker.force.vdw

import io.github.hc1839.crul.chemistry.tinker.AtomClass
import io.github.hc1839.crul.chemistry.tinker.AtomClassUnorderedPair

/**
 *  Mapper that maps atom classes to vdW parameters.
 */
interface VdwParamMapper {
    /**
     *  Valid atom classes.
     */
    val atomClasses: Set<AtomClass>

    /**
     *  Rmin of two atom classes.
     *
     *  @param atomClassPair
     *      Atom-class pair to determine. If an atom class does not exist in
     *      [atomClasses], an exception is raised.
     *
     *  @return
     *      Value of the Rmin parameter for `atomClassPair`.
     */
    fun rmin(atomClassPair: AtomClassUnorderedPair): Double

    /**
     *  Potential well depth (epsilon) of two atom classes.
     *
     *  @param atomClassPair
     *      Atom-class pair to determine. If an atom class does not exist in
     *      [atomClasses], an exception is raised.
     *
     *  @return
     *      Value of the epsilon parameter for `atomClassPair`.
     */
    fun epsilon(atomClassPair: AtomClassUnorderedPair): Double

    /**
     *  Reduction factor (beta) of an atom class.
     *
     *  @param atomClass
     *      Atom class. If it does not exist in [atomClasses], an exception is
     *      raised.
     *
     *  @return
     *      Value of the beta parameter for `atomClass`, or `null` if the atom
     *      class does not have a beta parameter.
     */
    fun beta(atomClass: AtomClass): Double?

    /**
     *  Gets the group of vdW parameters for an atom-class pair.
     *
     *  @param atomClass
     *      Atom class of the vdW parameters to retrieve. If it does not exist
     *      in [atomClasses], an exception is raised.
     *
     *  @return
     *      VdW-parameter group for `atomClass`. If the atom class does not
     *      have a beta parameter, its value is `null`.
     */
    fun getParamGroup(atomClass: AtomClass): VdwParamGroup {
        val atomClassPair = AtomClassUnorderedPair(atomClass, atomClass)

        return VdwParamGroup(
            atomClass = atomClass,
            rmin = rmin(atomClassPair),
            epsilon = epsilon(atomClassPair),
            beta = beta(atomClass)
        )
    }
}
