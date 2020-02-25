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
 *  Skeletal implementation of a molecule, which is an [Island] with more than
 *  one atom that has every pair of atoms connected by bonds, directly or
 *  indirectly.
 *
 *  A molecule is taken in a more general sense to include polyatomic ions.
 *
 *  Atoms are referentially distinct.
 *
 *  @param A
 *      Type of atoms.
 *
 *  @constructor
 *
 *  @param bonds
 *      Non-empty collection of bonds of the molecule. Order is not important,
 *      and referentially equivalent bonds are removed. Exception is raised if
 *      (1) two bonds have referentially equal atoms but unequal orders or (2)
 *      collection of bonds represents more than one molecule.
 */
abstract class AbstractMolecule<A : Atom>(bonds: Collection<Bond<A>>) :
    AbstractFragment<A>(
        if (!bonds.isEmpty()) {
            bonds.flatMap { it.atoms() }.distinctBy { Referential(it) }
        } else {
            throw IllegalArgumentException(
                "Collection of bonds given to construct a molecule is empty."
            )
        }
    ),
    Island<A>
{
    /**
     *  Lists of bonds associated by the participating atom.
     */
    private val bondListsByAtom: Map<Referential<A>, List<Bond<A>>> =
        bondIndexing(bonds)


    override fun bonds(): Collection<Bond<A>> =
        bondListsByAtom.values.flatten().distinctBy {
            Referential(it)
        }

    override fun getBondsByAtom(sourceAtom: A): List<Bond<A>> {
        val wrappedSourceAtom = Referential(sourceAtom)

        if (!bondListsByAtom.containsKey(wrappedSourceAtom)) {
            throw IllegalArgumentException(
                "No such source atom."
            )
        }

        val bondList = bondListsByAtom[wrappedSourceAtom]!!

        return bondList.toList()
    }

    override fun getBond(atom1: A, atom2: A): Bond<A>? {
        val wrappedAtom1 = Referential(atom1)
        val wrappedAtom2 = Referential(atom2)

        if (
            !bondListsByAtom.contains(wrappedAtom1) ||
            !bondListsByAtom.contains(wrappedAtom2)
        ) {
            return null
        }

        val bond = bondListsByAtom[wrappedAtom1]!!
            .intersect(bondListsByAtom[wrappedAtom2]!!)
            .singleOrNull()

        return if (bond == null) {
            null
        } else {
            val wrappedBondAtoms = bond
                .atoms()
                .map { Referential(it) }
                .toSet()

            if (wrappedBondAtoms == setOf(wrappedAtom1, wrappedAtom2)) {
                bond
            } else {
                null
            }
        }
    }

    companion object {
        /**
         *  Indexing of the bonds from a collection of bonds.
         *
         *  @param bonds
         *      Non-empty collection of bonds of the molecule. Order is not
         *      important, and referentially equivalent bonds are removed.
         *      Exception is raised if (1) two bonds have referentially equal
         *      atoms but unequal orders or (2) collection of bonds represents
         *      more than one molecule.
         *
         *  @return
         *      Map of wrapped atom to list of bonds that the atom is
         *      participating.
         */
        private fun <A : Atom> bondIndexing(bonds: Collection<Bond<A>>):
            Map<Referential<A>, List<Bond<A>>>
        {
            if (bonds.isEmpty()) {
                throw IllegalArgumentException(
                    "Collection of bonds is empty."
                )
            }

            val bondAggregates = BondAggregator.aggregate(bonds)

            if (bondAggregates.count() != 1) {
                throw IllegalArgumentException(
                    "Collection of bonds represents more than one molecule."
                )
            }

            val bondAggregate = bondAggregates.single()

            val bondListsByAtom =
                mutableMapOf<Referential<A>, MutableList<Bond<A>>>()

            for (bond in bondAggregate) {
                for (atom in bond.atoms()) {
                    val wrappedAtom = Referential(atom)

                    if (!bondListsByAtom.containsKey(wrappedAtom)) {
                        bondListsByAtom[wrappedAtom] = mutableListOf()
                    }

                    bondListsByAtom[wrappedAtom]!!.add(bond)
                }
            }

            return bondListsByAtom.mapValues { (_, bondList) ->
                bondList.toList()
            }
        }
    }
}
