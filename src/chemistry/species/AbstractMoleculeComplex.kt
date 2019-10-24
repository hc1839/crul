/*
 *  Copyright Han Chen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package crul.chemistry.species

import crul.distinct.Referential

/**
 *  Skeletal implementation of [MoleculeComplex].
 *
 *  @param A
 *      Type of atoms.
 */
abstract class AbstractMoleculeComplex<A : Atom> :
    AbstractComplex<Island<A>>,
    MoleculeComplex<A>
{
    /**
     *  @param islands
     *      Molecules and atoms of the complex.
     */
    constructor(islands: Collection<Island<A>>): super(islands) {
        // Referentially distinct atoms from all islands.
        val wrappedAtomSets = islands.map { island ->
            island.atoms().map { atom ->
                Referential(atom)
            }.toSet()
        }

        // Number of distinct atoms across all islands.
        val numDistinctAtoms = wrappedAtomSets
            .flatten()
            .distinct()
            .count()

        if (
            numDistinctAtoms !=
                wrappedAtomSets.fold(0) { acc, item ->
                    acc + item.count()
                }
        ) {
            throw IllegalArgumentException(
                "At least one atom exists in more than one island."
            )
        }
    }

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractMoleculeComplex<A>): super(other)

    override fun getIslandWithAtom(atom: A): Island<A>? =
        toList()
            .filter { island -> island.containsAtom(atom) }
            .singleOrNull()
}
