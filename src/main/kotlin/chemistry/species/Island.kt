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

import kotlin.math.roundToInt

/**
 *  Group containing atoms of a molecule or a single atom.
 *
 *  To construct an island representing a molecule, instantiate a [Molecule].
 *  For a single atom, use [Atom.island].
 *
 *  @param A
 *      Type of atoms in this island.
 */
interface Island<A : Atom> : Fragment<A> {
    abstract override fun clone(): Island<A>

    /**
     *  Whether this island contains a single atom.
     *
     *  Implementation may override this to be a constant property.
     */
    val isSingleAtom: Boolean
        get() = atoms().count() == 1

    /**
     *  Rounded sum of the charges of the atoms in this island, or `null` if
     *  any of the atomic charges are `null`.
     */
    fun charge(): Int? =
        atoms().map { it.charge }.reduce { acc, charge ->
            if (acc != null && charge != null) {
                acc + charge
            } else {
                null
            }
        }?.roundToInt()

    /**
     *  Bonds in this island, or an empty collection if the island contains a
     *  single atom.
     *
     *  Bonds are unique. Bonds are not necessarily in the same order between
     *  calls.
     */
    fun bonds(): Collection<Bond<A>>

    /**
     *  Gets the bonds that an atom is participating in.
     *
     *  By default, [bonds] is used. An implementation may override this for a
     *  more efficient method.
     *
     *  @param atom
     *      Atom whose bonds is to be retrieved.
     *
     *  @return
     *      Bonds that `atom` is participating in. If `atom` does not exist in
     *      this island, an empty list is returned.
     */
    fun getBondsByAtom(atom: A): List<Bond<A>> =
        bonds().filter { it.containsAtom(atom) }

    /**
     *  Gets the atoms that are bonded to a given atom.
     *
     *  @param atom
     *      Atom whose bonded atoms are to be retrieved.
     *
     *  @return
     *      Atoms bonded to `atom`. If `atom` does not exist in this island, an
     *      empty list is returned.
     */
    fun getAtomsBondedTo(atom: A): List<A> =
        getBondsByAtom(atom)
            .flatMap { bond ->
                bond.toAtomPair().toList()
            }
            .filter {
                it !== atom
            }

    /**
     *  Gets the bond between two atoms.
     *
     *  By default, [bonds] is used. An implementation may override this for a
     *  more efficient method.
     *
     *  @param atom1
     *      First atom.
     *
     *  @param atom2
     *      Second atom.
     *
     *  @return
     *      Bond that the given atoms are participating in, or `null` if there
     *      is no bond between the two atoms. If a given atom does not exist in
     *      this island, `null` is returned.
     */
    fun getBond(atom1: A, atom2: A): Bond<A>? =
        bonds()
            .filter {
                it.containsAtom(atom1) &&
                it.containsAtom(atom2)
            }
            .singleOrNull()
}
