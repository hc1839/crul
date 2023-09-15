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

package io.github.hc1839.crul.chemistry.species

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
     *  Arbitrary name of the supermolecule, or `null` if not applicable.
     */
    val name: String?

    /**
     *  Gets the island that contains `atom`.
     *
     *  If `atom` does not exist, an exception is thrown.
     */
    fun getIslandWithAtom(atom: A): Island<A>

    /**
     *  Returns a new supermolecule without the atoms in a given collection.
     *
     *  If an atom does not exist, an exception is thrown.
     */
    fun minusAtoms(atoms: Collection<A>): Supermolecule<A>

    /**
     *  Returns a new supermolecule without `atom` and the bonds that it was
     *  participating in.
     *
     *  If `atom` does not exist, an exception is thrown.
     */
    fun minusAtom(atom: A): Supermolecule<A> =
        minusAtoms(listOf(atom))

    /**
     *  Returns a new supermolecule without `bonds`.
     *
     *  Order of the atoms and any subspecies in the returned supermolecule is
     *  implementation-specific.
     *
     *  If a bond does not exist, an exception is thrown.
     */
    fun minusBonds(bonds: Collection<Bond<A>>): Supermolecule<A>

    /**
     *  Returns a new supermolecule without `bond`, adding new atom islands
     *  that result from the bond removals if necessary.
     *
     *  Order of the atoms and any subspecies in the returned supermolecule is
     *  implementation-specific.
     *
     *  If `bond` does not exist, an exception is thrown.
     */
    fun minusBond(bond: Bond<A>): Supermolecule<A> =
        minusBonds(listOf(bond))

    /**
     *  Returns a new supermolecule without `islands`.
     *
     *  Order of the atoms and any subspecies in the returned supermolecule is
     *  implementation-specific.
     *
     *  If an island does not exist, an exception is thrown.
     */
    fun minusIslands(islands: Collection<Island<A>>): Supermolecule<A>

    /**
     *  Returns a new supermolecule without `island`.
     *
     *  Order of the atoms and any subspecies in the returned supermolecule is
     *  implementation-specific.
     *
     *  If `island` does not exist, an exception is thrown.
     */
    fun minusIsland(island: Island<A>): Supermolecule<A> =
        minusIslands(listOf(island))

    /**
     *  Returns a new supermolecule with the specified `name` and with atoms
     *  from the application of `transform` to each atom in this supermolecule.
     */
    fun <R : Atom> map(
        name: String?,
        transform: (A) -> R
    ): Supermolecule<R> =
        Supermolecule(subspecies.map { it.map(transform) }, name)

    companion object {
        /**
         *  Constructs a [Supermolecule].
         *
         *  @param islands
         *      Molecules and atom islands of the supermolecule.
         *
         *  @param name
         *      Arbitrary name of the supermolecule, or `null` if not
         *      applicable.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            islands: List<Island<A>>,
            name: String? = null
        ): Supermolecule<A> =
            SupermoleculeImpl(islands, name)
    }
}

/**
 *  Constructs a new instance of [Supermolecule].
 *
 *  See [Supermolecule.newInstance].
 */
fun <A : Atom> Supermolecule(
    islands: List<Island<A>>,
    name: String? = null
): Supermolecule<A> =
    Supermolecule.newInstance(islands, name)
