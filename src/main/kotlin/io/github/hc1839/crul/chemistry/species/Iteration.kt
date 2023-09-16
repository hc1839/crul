@file:JvmName("Iteration")
@file:JvmMultifileClass

package io.github.hc1839.crul.chemistry.species

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.FragmentedSupermolecule
import io.github.hc1839.crul.chemistry.species.Supermolecule

/**
 *  Pairs of atoms from different islands.
 *
 *  Order of the atoms in a pair is not significant.
 */
fun <A : Atom> Supermolecule<A>.crossAtomPairs(): List<Pair<A, A>> {
    val islands = subspecies
    val atomPairsBuilder = mutableListOf<Pair<A, A>>()

    for (outerIndex in (0..islands.lastIndex - 1)) {
        for (innerIndex in (outerIndex + 1..islands.lastIndex)) {
            for (outerAtom in islands[outerIndex].atoms) {
                for (innerAtom in islands[innerIndex].atoms) {
                    atomPairsBuilder.add(
                        Pair(outerAtom, innerAtom)
                    )
                }
            }
        }
    }

    return atomPairsBuilder.toList()
}

/**
 *  Pairs of atoms from different fragments.
 *
 *  Order of the atoms in a pair is not significant.
 */
fun <A : Atom> FragmentedSupermolecule<A>.crossAtomPairs(): List<Pair<A, A>> {
    val atomPairsBuilder = mutableListOf<Pair<A, A>>()

    for (outerIndex in (0..fragments.lastIndex - 1)) {
        for (innerIndex in (outerIndex + 1..fragments.lastIndex)) {
            for (outerAtom in fragments[outerIndex].atoms) {
                for (innerAtom in fragments[innerIndex].atoms) {
                    atomPairsBuilder.add(
                        Pair(outerAtom, innerAtom)
                    )
                }
            }
        }
    }

    return atomPairsBuilder.toList()
}
