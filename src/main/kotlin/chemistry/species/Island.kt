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

import crul.chemistry.species.travel.Neighbor
import crul.chemistry.species.travel.Neighborhood

/**
 *  Fragment that contains atoms of a molecule or contains a single atom.
 *
 *  To construct an island representing a molecule, instantiate a [Molecule].
 *  For a single atom, use [Atom.island].
 *
 *  @param A
 *      Type of atoms.
 */
interface Island<A : Atom> : Fragment<A> {
    /**
     *  Whether the island is representing a single atom.
     */
    fun isAtomic(): Boolean =
        atoms().singleOrNull() != null

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
     *  @param sourceAtom
     *      Source atom whose bonds is to be retrieved. If it does not exist in
     *      this island, an exception is thrown.
     *
     *  @return
     *      Bonds that `sourceAtom` is participating in.
     */
    fun getBondsByAtom(sourceAtom: A): List<Bond<A>> {
        if (!containsAtom(sourceAtom)) {
            throw IllegalArgumentException(
                "Island does not contain the given source atom."
            )
        }

        return bonds().filter { it.containsAtom(sourceAtom) }
    }

    /**
     *  Gets the atoms that are bonded to a given atom.
     *
     *  @param sourceAtom
     *      Source atom whose bonded atoms are to be retrieved. If it does not
     *      exist in this island, an exception is thrown.
     *
     *  @return
     *      Atoms bonded to `sourceAtom`.
     */
    fun getAtomsBondedTo(sourceAtom: A): Collection<A> {
        if (!containsAtom(sourceAtom)) {
            throw IllegalArgumentException(
                "Island does not contain the given source atom."
            )
        }

        return getBondsByAtom(sourceAtom).map { bond ->
            bond.atoms().single { atom ->
                atom !== sourceAtom
            }
        }
    }

    /**
     *  Gets the bond between two atoms.
     *
     *  If a given atom does not exist in this island, an exception is thrown.
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
     *      is no bond between the two atoms.
     */
    fun getBond(atom1: A, atom2: A): Bond<A>? {
        if (!containsAtom(atom1) || !containsAtom(atom2)) {
            throw IllegalArgumentException(
                "Island does not contain both of the given atoms."
            )
        }

        return bonds().filter {
            it.containsAtom(atom1) &&
            it.containsAtom(atom2)
        }.singleOrNull()
    }

    /**
     *  Gets the neighborhood of `sourceAtom`.
     *
     *  If `sourceAtom` does not exist in this island, an exception is thrown.
     */
    fun getNeighborhood(sourceAtom: A): Neighborhood<A> {
        if (!containsAtom(sourceAtom)) {
            throw IllegalArgumentException(
                "Island does not contain the given source atom."
            )
        }

        val neighbors = getBondsByAtom(sourceAtom).map { bond ->
            val bondedAtom = bond.atoms().single { atom ->
                atom !== sourceAtom
            }

            Neighbor(bond.order, bondedAtom)
        }

        return Neighborhood(
            sourceAtom = sourceAtom,
            neighbors = neighbors
        )
    }

    /**
     *  Sequence of neighborhoods in this island.
     */
    fun neighborhoods(): Sequence<Neighborhood<A>> =
        atoms().asSequence().map { atom ->
            getNeighborhood(atom)
        }

    /**
     *  Returns a new island with atoms from the application of `transform` to
     *  each atom in this island.
     */
    fun <R : Atom> map(transform: (A) -> R): Island<R>
}
