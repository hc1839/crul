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

@file:JvmName("IdentifierRenumbering")
@file:JvmMultifileClass

package io.github.hc1839.crul.chemistry.species.format.mol2

import io.github.hc1839.crul.chemistry.species.Supermolecule

/**
 *  Renumbers non-`null` [TriposAtom.substId], and then renumbers
 *  [TriposAtom.atomtId] grouped by substructure.
 *
 *  `null` [TriposAtom.substId] are not assigned a substructure identifier but
 *  are collectively treated as if it is one substructure during
 *  atom-identifier renumbering.
 *
 *  [TriposAtom.substName] is ignored.
 *
 *  @param substIdStart
 *      Index to start the renumbering of substructures.
 *
 *  @param atomIdStart
 *      Index to start the renumbering of atoms. The renumbering of atoms is
 *      such that the resulting atom identifiers are consecutive within each
 *      substructure but do not restart between substructures.
 *
 *  @param name
 *      Arbitrary name of the returned supermolecule, or `null` if not
 *      applicable.
 *
 *  @return
 *      Supermolecule with substructure and atom identifiers renumbered.
 */
fun Supermolecule<TriposAtom>.renumberedSubstructures(
    substIdStart: Int,
    atomIdStart: Int,
    name: String? = null
): Supermolecule<TriposAtom>
{
    val oldToNewSubstIds = mutableMapOf<Int, Int>()
    var newSubstId = substIdStart

    // Renumber substructures only.
    val renumberedSubstSupermol = map(null) { atom ->
        if (atom.substId != null) {
            if (!oldToNewSubstIds.containsKey(atom.substId)) {
                oldToNewSubstIds[atom.substId] = newSubstId++
            }

            atom.copy(
                substId = oldToNewSubstIds[atom.substId]!!
            )
        } else {
            atom
        }
    }

    // Placeholder for the supermolecule with substructures and atoms
    // renumbered.
    var renumberedSupermol = renumberedSubstSupermol
    var newAtomId = atomIdStart

    // Renumber atoms for non-null substructures.
    for (substId in (1 until newSubstId)) {
        renumberedSupermol = renumberedSupermol.map(null) { atom ->
            if (atom.substId == substId) {
                atom.copy(atomId = newAtomId++)
            } else {
                atom
            }
        }
    }

    // Renumber atoms for null substructures.
    renumberedSupermol = renumberedSupermol.map(name) { atom ->
        if (atom.substId == null) {
            atom.copy(atomId = newAtomId++)
        } else {
            atom
        }
    }

    return renumberedSupermol
}
