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
 *  Supermolecule as an aggregate of islands.
 *
 *  @param A
 *      Type of atoms.
 */
interface Supermolecule<A : Atom> : Aggregate<Island<A>> {
    override val atoms: List<A>
        get() = super.atoms.map {
            @Suppress("UNCHECKED_CAST")
            it as A
        }

    /**
     *  Gets the island that contains a given atom by referential equality.
     *
     *  If `atom` does not exist, an exception is thrown.
     */
    fun getIslandWithAtom(atom: A): Island<A>

    /**
     *  Returns a new supermolecule without the atoms in a given collection.
     *
     *  Comparison is referential. If an atom does not exist, an exception is
     *  thrown.
     */
    fun minusAtoms(atoms: Collection<A>): Supermolecule<A>

    /**
     *  Returns a new supermolecule without the given atom and the bonds that
     *  it was participating in.
     *
     *  Comparison is referential. If `atom` does not exist, an exception is
     *  thrown.
     */
    fun minusAtom(atom: A): Supermolecule<A> =
        minusAtoms(listOf(atom))

    /**
     *  Returns a new supermolecule without the bonds in a given collection.
     *
     *  Comparison is referential. If a bond does not exist, an exception is
     *  thrown.
     */
    fun minusBonds(bonds: Collection<Bond<A>>): Supermolecule<A>

    /**
     *  Returns a new supermolecule without the given bond, adding new atom
     *  islands that result from the bond removals if necessary.
     *
     *  Comparison is referential. If `bond` does not exist, an exception is
     *  thrown.
     */
    fun minusBond(bond: Bond<A>): Supermolecule<A> =
        minusBonds(listOf(bond))

    /**
     *  Returns a new supermolecule without the islands in a given collection.
     *
     *  Comparison is referential. If an island does not exist, an exception is
     *  thrown.
     */
    fun minusIslands(islands: Collection<Island<A>>): Supermolecule<A>

    /**
     *  Returns a new supermolecule without the given island.
     *
     *  Comparison is referential. If `island` does not exist, an exception is
     *  thrown.
     */
    fun minusIsland(island: Island<A>): Supermolecule<A> =
        minusIslands(listOf(island))

    /**
     *  Returns a new supermolecule with atoms from the application of
     *  `transform` to each atom in this supermolecule.
     */
    fun <R : Atom> map(transform: (A) -> R): Supermolecule<R> =
        Supermolecule(subspecies.map { it.map(transform) })

    companion object {
        /**
         *  Constructs a [Supermolecule].
         *
         *  @param islands
         *      Molecules and atom islands of the supermolecule.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            islands: List<Island<A>>
        ): Supermolecule<A> =
            SupermoleculeImpl(islands)
    }
}

/**
 *  Constructs a new instance of [Supermolecule].
 *
 *  See [Supermolecule.newInstance].
 */
fun <A : Atom> Supermolecule(islands: List<Island<A>>): Supermolecule<A> =
    Supermolecule.newInstance(islands)
