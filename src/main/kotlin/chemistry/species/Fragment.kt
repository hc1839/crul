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
 *  Interface of a fragment, which is an aggregate of atoms.
 *
 *  @param A
 *      Type of atoms.
 */
interface Fragment<A : Atom> : Aggregate<A> {
    override val atoms: List<A>
        get() = @Suppress("UNCHECKED_CAST") (
            super.atoms as List<A>
        )

    companion object {
        /**
         *  Constructs a [Fragment].
         *
         *  @param atoms
         *      Atoms of the fragment.
         */
        @JvmStatic
        fun <A : Atom> newInstance(atoms: List<A>): Fragment<A> =
            FragmentImpl(atoms)
    }
}

/**
 *  Constructs a new instance of [Fragment].
 *
 *  See [Fragment.newInstance].
 */
fun <A : Atom> Fragment(atoms: List<A>): Fragment<A> =
    Fragment.newInstance(atoms)
