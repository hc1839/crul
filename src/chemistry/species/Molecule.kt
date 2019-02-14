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

package chemistry.species

/**
 *  Interface for a molecule, which is a non-empty [Fragment] with unique atom
 *  names and has every pair of atoms connected by bonds (directly or
 *  indirectly).
 *
 *  Equality operator, `==`, is used for comparing atoms. Within the same
 *  molecule, two equal atoms must have the same name and vice versa.
 *
 *  @param A
 *      Type of atoms in this molecule.
 */
interface Molecule<A : Atom> : Fragment<A> {
    /**
     *  Bonds that are in this molecule, or an empty iterator if this molecule
     *  is a singleton.
     */
    fun bonds(): Iterator<Bond<A>>

    /**
     *  Gets the bonds that an atom is participating in.
     *
     *  By default, [bonds] is used. An implementation may override this for a
     *  more efficient method.
     *
     *  If a given atom does not exist in this molecule, an exception is
     *  raised.
     *
     *  @return
     *      The given atom is the first atom in the returned set of [Bond].
     */
    fun getBondsByAtom(atom: A): Set<Bond<A>> =
        bonds()
            .asSequence()
            .filter { it.containsAtom(atom) }
            .toSet()

    /**
     *  Gets the bond between two atoms, or `null` if there is no such bond.
     *
     *  By default, [bonds] is used. An implementation may override this for a
     *  more efficient method.
     *
     *  If a given atom does not exist in this molecule, an exception is
     *  raised.
     *
     *  @param atom1
     *      First atom.
     *
     *  @param atom2
     *      Second atom.
     *
     *  @return
     *      The atoms in the returned bond are in the same order as given.
     */
    fun getBond(atom1: A, atom2: A): Bond<A>? =
        bonds()
            .asSequence()
            .filter {
                it.containsAtom(atom1) &&
                it.containsAtom(atom2)
            }
            .singleOrNull()

    abstract override fun clone(): Molecule<A>
}
