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
 *  Skeletal implementation of [Molecule].
 *
 *  @param A
 *      Type of atoms.
 */
abstract class AbstractMolecule<A : Atom> :
    AbstractFragment<A>,
    Molecule<A>
{
    /**
     *  Lists of bonds associated by the identifier of the participating atom.
     */
    private val bondListsByAtomId: Map<String, List<Bond<A>>>

    /**
     *  @param bonds
     *      Non-empty set of bonds of the molecule. Exception is raised if (1)
     *      two unequal atoms have the same identifier, (2) two bonds have
     *      equal atoms but unequal orders, or (3) set of bonds represents more
     *      than one molecule.
     */
    constructor(bonds: Set<Bond<A>>): super(
        if (!bonds.isEmpty()) {
            bonds.flatMap { it.atoms() }.distinct()
        } else {
            throw IllegalArgumentException("Set of bonds is empty.")
        }
    ) {
        this.bondListsByAtomId = bondIndexing(bonds.toSet())
    }

    /**
     *  Copy constructor.
     *
     *  @param other
     *      Molecule to copy.
     *
     *  @param deep
     *      Whether atoms and bonds are copied.
     */
    @JvmOverloads
    constructor(
        other: AbstractMolecule<A>,
        deep: Boolean = false
    ): this(
        if (deep) {
            other.bonds().map { it.clone() }.toSet()
        } else {
            other.bonds().toSet()
        }
    )

    override fun bonds(): Collection<Bond<A>> =
        bondListsByAtomId.values.flatten().distinct()

    override fun getBondsByAtom(atom: A): Set<Bond<A>> {
        if (!bondListsByAtomId.contains(atom.id)) {
            "No such atom: ${atom.id}"
        }

        val bondList = bondListsByAtomId[atom.id]!!

        if (!bondList.first().atoms().contains(atom)) {
            throw IllegalArgumentException(
                "Given atom is not equal to the atom " +
                "of the same identifier: ${atom.id}"
            )
        }

        return bondList.toSet()
    }

    override fun getBond(atom1: A, atom2: A): Bond<A>? {
        if (
            !bondListsByAtomId.contains(atom1.id) ||
            !bondListsByAtomId.contains(atom2.id)
        ) {
            return null
        }

        val bond = bondListsByAtomId[atom1.id]!!.intersect(
            bondListsByAtomId[atom2.id]!!
        ).singleOrNull()

        return if (bond == null) {
            null
        } else {
            if (bond.atoms().toSet() == setOf(atom1, atom2)) {
                bond
            } else {
                null
            }
        }
    }

    companion object {
        /**
         *  Indexing of the bonds from a set of bonds.
         *
         *  @param bonds
         *      Non-empty set of bonds of the molecule. Exception is raised if
         *      (1) two unequal atoms have the same identifier, (2) two bonds
         *      have equal atoms but unequal orders, or (3) set of bonds
         *      represents more than one molecule.
         *
         *  @return
         *      Map of atom identifier to list of bonds that the atom is
         *      participating.
         */
        private fun <A : Atom> bondIndexing(
            bonds: Set<Bond<A>>
        ): Map<String, List<Bond<A>>>
        {
            if (bonds.isEmpty()) {
                throw IllegalArgumentException(
                    "Set of bonds is empty."
                )
            }

            val bondAggregates = BondAggregator.aggregate(bonds)

            if (bondAggregates.count() != 1) {
                throw IllegalArgumentException(
                    "Set of bonds represents more than one molecule."
                )
            }

            val bondAggregate = bondAggregates.single()
            val bondListsByAtomId = mutableMapOf<String, MutableList<Bond<A>>>()

            for (bond in bondAggregate) {
                for (atom in bond.atoms()) {
                    if (!bondListsByAtomId.contains(atom.id)) {
                        bondListsByAtomId[atom.id] = mutableListOf()
                    }

                    bondListsByAtomId[atom.id]!!.add(bond)
                }
            }

            return bondListsByAtomId.mapValues { (_, bondList) ->
                bondList.toList()
            }
        }
    }
}
