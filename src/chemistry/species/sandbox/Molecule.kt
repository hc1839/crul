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

package chemistry.species.sandbox

/**
 *  Interface for a molecule, which is a non-empty [Fragment] with unique atom
 *  names and has every pair of atoms connected by bonds (directly or
 *  indirectly).
 *
 *  @param A
 *      Type of atoms in this molecule.
 */
interface Molecule<A : Atom> : Fragment<A> {
    /**
     *  Bonds that are in this molecule.
     */
    val bonds: Iterator<Bond<A>>

    /**
     *  Gets the bonds that an atom is participating in.
     *
     *  @param atomName
     *      [Atom.name] of an atom.
     */
    fun getBondsByAtom(atomName: String): Set<Bond<A>>

    /**
     *  Gets the bond between two atoms, or `null` if there is no such bond.
     *
     *  If no such atom with the given name exists, an exception is raised.
     *
     *  @param atom1Name
     *      [Atom.name] of one of the two atoms.
     *
     *  @param atom2Name
     *      [Atom.name] of the other atom.
     */
    fun getBond(atom1Name: String, atom2Name: String): Bond<A>?

    /**
     *  Gets the atoms that are directly bonded to a given atom.
     *
     *  @param atomName
     *      [Atom.name] of the atom. If there is no such atom, an exception is
     *      raised.
     *
     *  @return
     *      For a molecule of one atom, an empty list is returned.
     */
    fun getAtomsBondedTo(atomName: String): Set<A>
}
