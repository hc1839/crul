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
 *  @param A
 *      Type of atoms.
 *
 *  @constructor
 *
 *  @param bonds
 *      Non-empty list of referentially distinct bonds of the molecule. No two
 *      bonds can have referentially equal atoms. List of bonds must represent
 *      exactly one molecule. Ordering of the atoms is determined by the
 *      ordering of the bonds.
 */
abstract class AbstractMolecule<A : Atom>(override val bonds: List<Bond<A>>) :
    AbstractFragment<A>(
        bonds.flatMap { it.atoms }.distinctBy { Referential(it) }
    ),
    Island<A>
{
    /**
     *  Lists of bonds associated by the participating atom.
     */
    private val bondListsByAtom: Map<Referential<A>, List<Bond<A>>> =
        bondIndexing(this.bonds)

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
                .atoms
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
         *      Non-empty collection of referentially distinct bonds of the
         *      molecule. No two bonds can have referentially equal atoms.
         *      Collection of bonds must represent exactly one molecule.
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

            val bondAggregate = BondAggregator
                .aggregate(bonds)
                .singleOrNull()

            if (bondAggregate == null) {
                throw IllegalArgumentException(
                    "Collection of bonds represents more than one molecule."
                )
            }

            val bondListsByAtom =
                mutableMapOf<Referential<A>, MutableList<Bond<A>>>()

            for (bond in bondAggregate) {
                for (atom in bond.atoms) {
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
