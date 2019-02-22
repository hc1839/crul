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
 *  Interface for a fragment, which is a non-emtpy [Complex] of [Atom].
 *
 *  @param A
 *      Type of atoms in this fragment.
 */
interface Fragment<A : Atom> : Complex<A> {
    /**
     *  Formal charge of this fragment, which is the sum of the formal charges
     *  of the atoms.
     *
     *  If [atoms] is empty, an exception is raised.
     */
    val formalCharge: Double
        get() = atoms()
            .asSequence()
            .map { it.formalCharge }
            .reduce { acc, item -> acc + item }

    /**
     *  Whether an atom exists in this fragment.
     */
    fun containsAtom(atom: A): Boolean

    /**
     *  Gets an atom by its name, or `null` if there is no such atom.
     *
     *  If this fragment has more than one atom with the same given name, the
     *  first one encountered is returned.
     */
    fun getAtomByName(atomName: String): A? =
        @Suppress("UNCHECKED_CAST") (
            atoms()
                .asSequence()
                .firstOrNull { it.name == atomName }
        )

    override fun atoms(): Iterator<A> =
        @Suppress("UNCHECKED_CAST") (
            super.atoms() as Iterator<A>
        )

    /**
     *  Clones this fragment along with its atoms.
     */
    abstract override fun clone(): Fragment<A>
}
