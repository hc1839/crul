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
 *  Interface for a bond in a molecule.
 *
 *  Atoms in a bond must not be equal to each other and must not have the same
 *  name. Semantically, the order of the atoms is not important. Technically,
 *  the order of the given atoms is preserved, and the atoms are returned as
 *  such.
 *
 *  To construct an instance of this class, use [newInstance].
 *
 *  @param A
 *      Type of atoms in this bond.
 */
interface Bond<A : Atom> : Fragment<A> {
    /**
     *  Bond order as an aribtrary string.
     *
     *  Whether it is updated when the bond order is changed in the parent
     *  complex is implementation-dependent.
     */
    val order: String

    /**
     *  Atoms as a pair in the given order.
     */
    fun toAtomPair(): Pair<A, A>

    abstract override fun clone(): Bond<A>

    companion object {
        /**
         *  Constructs a [Bond].
         *
         *  If the given atoms are equal or have the same name, an exception is
         *  raised.
         *
         *  @param atom1
         *      First atom.
         *
         *  @param atom2
         *      Second atom.
         *
         *  @param order
         *      Bond order as an arbitrary string.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            atom1: A,
            atom2: A,
            order: String
        ): Bond<A> = BondImpl(
            atom1,
            atom2,
            order
        )
    }
}
