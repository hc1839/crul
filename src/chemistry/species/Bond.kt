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
 *  Interface for a bond in a molecule.
 *
 *  Atoms in a bond must not be equal to each other and must not have the same
 *  name. Semantically, the order of the atoms is not important. Technically,
 *  the order of the given atoms is preserved, and the atoms are returned as
 *  such.
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

    /**
     *  Iterator over the two atoms in the given order.
     */
    override fun atoms(): Iterator<A> =
        toAtomPair().toList().iterator()

    abstract override fun clone(): Bond<A>
}
