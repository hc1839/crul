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

import crul.serialize.AvroSimple

/**
 *  Interface for a fragment, which is a non-empty [Complex] of [Atom]
 *  instances.
 *
 *  Atoms are compared by referentially equality.
 *
 *  @param A
 *      Type of atoms in this fragment.
 */
interface Fragment<A : Atom> : Complex<A> {
    /**
     *  Whether an atom exists in this fragment.
     */
    fun containsAtom(atom: A): Boolean =
        atoms().any { it === atom }

    override fun atoms(): Collection<A> =
        @Suppress("UNCHECKED_CAST") (
            super.atoms() as Collection<A>
        )

    override fun getAtomsByTag(tag: Int): List<A> =
        @Suppress("UNCHECKED_CAST") (
            super.getAtomsByTag(tag) as List<A>
        )

    abstract override fun clone(): Fragment<A>

    companion object {
        /**
         *  Constructs a [Fragment].
         *
         *  @param atoms
         *      Atoms of the fragment.
         */
        @JvmStatic
        fun <A : Atom> newInstance(atoms: Collection<A>): Fragment<A> =
            Fragment(atoms)
    }
}

/**
 *  Constructs a new instance of [Fragment].
 *
 *  See [Fragment.newInstance] for description.
 */
fun <A : Atom> Fragment(atoms: Collection<A>): Fragment<A> =
    FragmentImpl(atoms)
