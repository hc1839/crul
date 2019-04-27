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
 *  Interface for a fragment, which is a non-empty [Complex] of [Atom].
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
            .map { it.formalCharge }
            .reduce { acc, item -> acc + item }

    /**
     *  Whether an atom exists in this fragment.
     */
    fun containsAtom(atom: A): Boolean

    /**
     *  Gets an atom by its ID, or `null` if there is no such atom.
     *
     *  If this fragment has more than one atom with the same given ID, the
     *  first one encountered is returned.
     */
    fun getAtomById(atomId: String): A? =
        @Suppress("UNCHECKED_CAST")
        atoms().firstOrNull { it.id == atomId }

    override fun atoms(): Collection<A> =
        @Suppress("UNCHECKED_CAST") (
            super.atoms() as Collection<A>
        )

    override fun clone(): Fragment<A> =
        @Suppress("UNCHECKED_CAST")
        super.clone() as Fragment<A>

    abstract override fun clone(deep: Boolean): Fragment<A>

    companion object {
        /**
         *  Constructs a [Fragment].
         *
         *  @param atoms
         *      Atoms of the fragment.
         */
        @JvmStatic
        fun <A : Atom> newInstance(atoms: Collection<A>): Fragment<A> =
            FragmentImpl(atoms)
    }
}
