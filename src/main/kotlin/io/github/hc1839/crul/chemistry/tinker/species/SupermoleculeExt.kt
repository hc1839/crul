@file:JvmName("SupermoleculeExt")
@file:JvmMultifileClass

package io.github.hc1839.crul.chemistry.tinker.species

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.Supermolecule
import io.github.hc1839.crul.chemistry.tinker.AtomClass
import io.github.hc1839.crul.chemistry.tinker.AtomClassMapper

/**
 *  Set of atom classes for a supermolecule.
 */
fun <A : Atom> Supermolecule<A>.atomClasses(
    atomClassMapper: AtomClassMapper<A>
): Set<AtomClass> =
    atoms
        .map { atom -> atomClassMapper.invoke(atom, this) }
        .toSet()
