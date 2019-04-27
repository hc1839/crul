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

/**
 *  Aggregator of bonds.
 */
object BondAggregator {
    /**
     *  Aggregates the bonds into bond lists, each of which represents a
     *  molecule.
     *
     *  @param bonds
     *      Set of bonds that represents a complex. If two unequal atoms have
     *      the same identifier, an exception is raised. If two bonds have
     *      equal atoms but unequal orders, an exception is raised.
     *
     *  @return
     *      List of bond lists such that different bond lists do not share
     *      equal atoms.
     */
    @JvmStatic
    fun <A : Atom> aggregate(bonds: Set<Bond<A>>): List<List<Bond<A>>> {
        if (bonds.isEmpty()) {
            return listOf()
        }

        if (
            bonds.count() !=
            bonds
                .distinctBy { bond -> bond.atoms().toSet() }
                .count()
        ) {
            throw IllegalArgumentException(
                "Two bonds have equal atoms but unequal orders."
            )
        }

        val atomsById: MutableMap<String, A> = mutableMapOf()

        // Index the atoms.
        for (bond in bonds) {
            for (atom in bond.atoms()) {
                if (!atomsById.contains(atom.id)) {
                    atomsById[atom.id] = atom
                } else if (atomsById[atom.id]!! != atom) {
                    throw IllegalArgumentException(
                        "Two unequal atoms have the same identifier: " +
                        "${atom.id}"
                    )
                }
            }
        }

        // Sets of identifiers of atoms that are bonded to the key of the map.
        val bondPartnerSetsByAtomId: MutableMap<String, Set<String>> =
            atomsById.keys.associateWith { setOf<String>() }.toMutableMap()

        // Index the bonds.
        for (bond in bonds) {
            val (atom1, atom2) = bond.toAtomPair()

            bondPartnerSetsByAtomId[atom1.id] =
                bondPartnerSetsByAtomId[atom1.id]!!.plusElement(atom2.id)

            bondPartnerSetsByAtomId[atom2.id] =
                bondPartnerSetsByAtomId[atom2.id]!!.plusElement(atom1.id)
        }

        var remainingBondGroups = bondPartnerSetsByAtomId.map {
            (srcAtomId, dstAtomIds) ->

            dstAtomIds.plusElement(srcAtomId)
        }

        // Builder for the groups of atom identifiers, each of which represents
        // a molecule.
        val aggregatesBuilder = mutableListOf<Set<String>>(
            remainingBondGroups.first()
        )

        remainingBondGroups = remainingBondGroups.drop(1)

        // Successively form the aggregates.
        while (!remainingBondGroups.isEmpty()) {
            val (partners, rest) = remainingBondGroups.partition {
                !aggregatesBuilder.last().intersect(it).isEmpty()
            }

            remainingBondGroups = rest

            if (!partners.isEmpty()) {
                aggregatesBuilder[aggregatesBuilder.lastIndex] = partners
                    .fold(aggregatesBuilder.last()) { acc, partner ->
                        acc + partner
                    }
            }

            if (!remainingBondGroups.isEmpty()) {
                aggregatesBuilder.add(remainingBondGroups.first())
                remainingBondGroups = remainingBondGroups.drop(1)
            }
        }

        // Convert each set of atom identifiers, which represents a molecule,
        // to a list of bonds.
        return aggregatesBuilder.map { atomIds ->
            // Filter bonds such that at least one of its atoms is in the
            // group.
            bonds.filter { bond ->
                bond.atoms().any { bondAtom ->
                    atomIds.contains(bondAtom.id)
                }
            }
        }
    }
}
