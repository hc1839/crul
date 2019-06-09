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
     *      Collection of bonds. Order is not important, and referentially
     *      equivalent bonds are removed.  Exception is raised if two bonds
     *      have referentially equal atoms but unequal orders.
     *
     *  @return
     *      List of bond lists such that different bond lists correspond to
     *      different molecules.
     */
    @JvmStatic
    fun <A : Atom> aggregate(bonds: Collection<Bond<A>>): List<List<Bond<A>>> {
        if (bonds.isEmpty()) {
            return listOf()
        }

        // Bonds that are referentially distinct.
        val distinctBonds = bonds.distinctBy {
            SpeciesSetElement(it)
        }

        if (
            distinctBonds.count() !=
            distinctBonds
                .distinctBy { bond ->
                    // Make the distinction of a bond depend only on the atoms
                    // and not on the bond order.
                    bond.atoms().map { atom ->
                        SpeciesSetElement(atom)
                    }.toSet()
                }
                .count()
        ) {
            throw IllegalArgumentException(
                "Two bonds have equal atoms but unequal bond orders."
            )
        }

        // Sets of wrapped atoms that are bonded to the key of the map.
        val bondPartnerSetsByAtom: Map<
            SpeciesSetElement<A>,
            MutableSet<SpeciesSetElement<A>>> = bonds
                .flatMap { bond -> bond.atoms() }
                .map { atom -> SpeciesSetElement(atom) }
                .associateWith { mutableSetOf<SpeciesSetElement<A>>() }
                .toMutableMap()

        // Index the bonds.
        for (bond in bonds) {
            val (atom1, atom2) = bond.toAtomPair()

            bondPartnerSetsByAtom[SpeciesSetElement(atom1)]!!.add(
                SpeciesSetElement(atom2)
            )

            bondPartnerSetsByAtom[SpeciesSetElement(atom2)]!!.add(
                SpeciesSetElement(atom1)
            )
        }

        var remainingBondGroups = bondPartnerSetsByAtom.map {
            (srcAtom, dstAtoms) ->

            dstAtoms.plusElement(srcAtom)
        }

        // Builder for the groups of atom identifiers, each of which represents
        // a molecule.
        val aggregatesBuilder = mutableListOf<Set<SpeciesSetElement<A>>>(
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

        // Convert each set of wrapped atoms, which represents a molecule, to a
        // list of bonds.
        return aggregatesBuilder.map { wrappedAtoms ->
            val atoms = wrappedAtoms.map { it.species }

            // Filter bonds such that at least one of its atoms is in the
            // group.
            distinctBonds.filter { bond ->
                bond.atoms().any { bondAtom ->
                    atoms.any { atom ->
                        atom === bondAtom
                    }
                }
            }
        }
    }
}
