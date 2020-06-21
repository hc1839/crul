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

import org.paukov.combinatorics3.Generator

import crul.apache.math.vector.*
import crul.distinct.Referential
import crul.measure.unit.UnitOfMeasure

/**
 *  Aggregator of bonds.
 */
object BondAggregator {
    /**
     *  Aggregates atoms into atom lists, each of which corresponds to an
     *  island.
     *
     *  Breadth-first search is used.
     *
     *  @param partners
     *      Map of atom to set of atoms that are bonded to it. Atoms appearing
     *      as values must also exist as a key.
     *
     *  @return
     *      Atoms aggregated into corresponding molecules.
     */
    private fun <A : Atom> aggregateAtoms(
        partners: Map<Referential<A>, Set<Referential<A>>>
    ): List<List<A>>
    {
        if (partners.none()) {
            return listOf()
        }

        val wrappedAtoms = partners
            .flatMap { (key, value) -> value.plusElement(key) }
            .toSet()

        if (wrappedAtoms.any { !partners.containsKey(it) }) {
            throw IllegalArgumentException(
                "Not all atoms appearing as values exist as keys."
            )
        }

        // Collection of aggregates.
        val aggregates = mutableListOf<List<A>>()

        // Whether an atom has been visited.
        val visited = wrappedAtoms
            .associateWith { false }
            .toMutableMap()

        while (visited.any { !it.value }) {
            // Buffer to hold the next set of unvisited atoms to visit.
            var nextLevelAtoms = setOf(
                visited
                    .entries
                    .first { (_, isVisited) -> !isVisited }
                    .key
            )

            // Builder of the current aggregate.
            val aggregateBuilder = nextLevelAtoms.toMutableSet()

            while (!nextLevelAtoms.isEmpty()) {
                aggregateBuilder.addAll(nextLevelAtoms)

                for (wrappedAtom in nextLevelAtoms) {
                    visited[wrappedAtom] = true
                }

                // Get the next level of atoms for the next iteration.
                nextLevelAtoms = nextLevelAtoms
                    .flatMap { partners[it]!! }
                    .filter { !visited[it]!! }
                    .toSet()
            }

            aggregates.add(aggregateBuilder.map { it.value })
        }

        return aggregates.toList()
    }

    /**
     *  Aggregates bonds into bond lists, each of which corresponds to an
     *  island.
     *
     *  @param bonds
     *      Collection of referentially distinct bonds. No two bonds can have
     *      referentially equal atoms.
     *
     *  @return
     *      List of bond lists such that each bond list corresponds to an
     *      island. If `bonds` is empty, an empty list is returned.
     */
    @JvmStatic
    fun <A : Atom> aggregate(
        bonds: Collection<Bond<A>>
    ): List<List<Bond<A>>>
    {
        if (bonds.isEmpty()) {
            return listOf()
        }

        val distinctBonds = bonds.distinctBy { Referential(it) }

        if (distinctBonds.count() != bonds.count()) {
            throw IllegalArgumentException(
                "Bonds are not referentially distinct."
            )
        }

        // Check that no two bonds have referentially equal atoms.
        val bondsDistinctByRefEqualAtoms = bonds.distinctBy { bond ->
            bond.atoms.map(::Referential).toSet()
        }

        if (bondsDistinctByRefEqualAtoms.count() != bonds.count()) {
            throw IllegalArgumentException(
                "At least two bonds have referentially equal atoms."
            )
        }

        val wrappedAtoms = bonds
            .flatMap { bond -> bond.atoms }
            .map { atom -> Referential(atom) }

        // Sets of atoms that are bonded to the key of the map.
        val partners = wrappedAtoms.associateWith {
            mutableSetOf<Referential<A>>()
        }

        // Sets of bonds associated with participating atom.
        val bondSetsByAtom = wrappedAtoms.associateWith {
            mutableSetOf<Referential<Bond<A>>>()
        }

        // Index the partners and bonds.
        for (bond in bonds) {
            val (atom1, atom2) = bond.toAtomPair()

            val wrappedAtom1 = Referential(atom1)
            val wrappedAtom2 = Referential(atom2)

            partners[Referential(atom1)]!!.add(wrappedAtom2)
            partners[Referential(atom2)]!!.add(wrappedAtom1)

            val wrappedBond = Referential(bond)

            for (wrappedAtom in listOf(wrappedAtom1, wrappedAtom2)) {
                bondSetsByAtom[wrappedAtom]!!.add(wrappedBond)
            }
        }

        val atomAggregates = aggregateAtoms(partners)

        // Convert each list of atoms to a list of bonds.
        return atomAggregates.map { atomAggregate ->
            atomAggregate
                .flatMap { atom -> bondSetsByAtom[Referential(atom)]!! }
                .distinct()
                .map { it.value }
        }
    }

    /**
     *  Aggregates atoms into fragments, each of which corresponds to an
     *  inferred island.
     *
     *  Connectivity between two atoms is inferred when the distance between
     *  the two atoms is less than the sum of their van der Waals radii times a
     *  scaling factor.
     *
     *  @param atoms
     *      Collection of referentially distinct atoms.
     *
     *  @param scalingFactor
     *      Scaling factor. It must be positive.
     *
     *  @param positionUnit
     *      Unit of the atom positions.
     *
     *  @return
     *      List of atom lists such that each atom list corresponds to an
     *      inferred island. If `atoms` is empty, an empty list is returned.
     */
    @JvmStatic
    fun <A : Atom> aggregate(
        atoms: Collection<A>,
        scalingFactor: Double,
        positionUnit: UnitOfMeasure
    ): List<List<A>>
    {
        if (scalingFactor <= 0.0) {
            throw IllegalArgumentException(
                "Scaling factor not positive: $scalingFactor"
            )
        }

        if (atoms.isEmpty()) {
            return listOf()
        }

        val wrappedAtoms = atoms.map { Referential(it) }

        if (wrappedAtoms.distinct().count() != atoms.count()) {
            throw IllegalArgumentException(
                "Atoms are not referentially distinct."
            )
        }

        val wrappedAtomsArray = Array(wrappedAtoms.count()) { index ->
            wrappedAtoms[index]
        }

        // Inferred bonds without regard to bond order.
        val inferredBonds = Generator
            .combination(*wrappedAtomsArray)
            .simple(2)
            .stream()
            .iterator()
            .asSequence()
            .filter {
                val atom1 = it[0].value
                val radius1 = atom1.element.radius.value(positionUnit)

                val atom2 = it[1].value
                val radius2 = atom2.element.radius.value(positionUnit)

                val distance = (atom2.position - atom1.position).getNorm()

                distance < (radius1 + radius2) * scalingFactor
            }
            .map {
                Bond(it[0].value, it[1].value, "")
            }
            .toList()

        val inferredBondedAtomLists = aggregate(inferredBonds).map { bonds ->
            bonds
                .flatMap { bond -> bond.atoms }
                .distinctBy { atom -> Referential(atom) }
        }

        val wrappedNonbondedAtoms = wrappedAtoms -
            inferredBondedAtomLists.flatten().map { Referential(it) }

        return wrappedNonbondedAtoms.map { listOf(it.value) } +
            inferredBondedAtomLists
    }
}
