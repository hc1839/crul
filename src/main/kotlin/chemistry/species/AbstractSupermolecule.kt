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
 *  Skeletal implementation of [Supermolecule].
 *
 *  @param A
 *      Type of atoms.
 *
 *  @constructor
 *
 *  @param islands
 *      Molecules and atom islands of the supermolecule.
 */
abstract class AbstractSupermolecule<A : Atom>(islands: List<Island<A>>) :
    AbstractAggregate<Island<A>>(islands),
    Supermolecule<A>
{
    init {
        // Referentially distinct atoms from all islands.
        val wrappedAtomSets = islands.map { island ->
            island.atoms.map { atom ->
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

    override fun getIslandWithAtom(atom: A): Island<A> {
        if (!containsAtom(atom)) {
            throw IllegalArgumentException(
                "Complex does not contain the given atom."
            )
        }

        return subspecies
            .filter { island -> island.containsAtom(atom) }
            .single()
    }

    override fun minusAtoms(atoms: Collection<A>): Supermolecule<A> {
        val wrappedGivenAtoms = atoms.map { Referential(it) }

        val filteredBonds = subspecies
            .filter { island -> !island.isAtomic() }
            .flatMap { island -> island.bonds() }
            .filterNot { bond ->
                bond.atoms.any { atom ->
                    Referential(atom) in wrappedGivenAtoms
                }
            }

        val newMolecules = BondAggregator.aggregate(filteredBonds).map {
            Molecule(it)
        }

        val filteredAtomIslands = subspecies.filter {
            it.isAtomic() &&
            Referential(it.atoms.single()) !in wrappedGivenAtoms
        }

        return Supermolecule(newMolecules + filteredAtomIslands)
    }

    override fun minusBonds(bonds: Collection<Bond<A>>): Supermolecule<A> {
        val wrappedGivenBonds = bonds.map { Referential(it) }

        val (atomIslands, origMolecules) = subspecies.partition {
            it.isAtomic()
        }

        val filteredBonds = origMolecules
            .flatMap { molecule -> molecule.bonds() }
            .filter { bond ->
                Referential(bond) !in wrappedGivenBonds
            }

        val newMolecules = BondAggregator.aggregate(filteredBonds).map {
            Molecule(it)
        }

        val wrappedAtomsOfNewMolecules = newMolecules
            .flatMap { molecule ->
                molecule.atoms.map { atom ->
                    Referential(atom)
                }
            }
            .distinct()

        val newAtomIslands = (
            atomIslands.map { Referential(it.atoms.single()) } +
            bonds
                .flatMap { bond ->
                    bond.atoms.map { atom ->
                        Referential(atom)
                    }
                }
                .distinct()
                .filter { wrappedAtom ->
                    wrappedAtom !in wrappedAtomsOfNewMolecules
                }
        ).distinct().map { it.value.getIsland<A>() }

        return Supermolecule<A>(newMolecules + newAtomIslands)
    }

    override fun minusIslands(
        islands: Collection<Island<A>>
    ): Supermolecule<A>
    {
        val wrappedGivenIslands = islands.map { Referential(it) }
        val wrappedOrigIslands = subspecies.map { Referential(it) }

        if (wrappedGivenIslands.any { it !in wrappedOrigIslands }) {
            throw IllegalArgumentException(
                "A given island does not exist in this complex."
            )
        }

        return Supermolecule(
            (wrappedOrigIslands - wrappedGivenIslands).map { it.value }
        )
    }
}
