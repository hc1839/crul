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
 *  Interface for a complex of molecules and atoms.
 *
 *  A subspecies is either a [Molecule] or an [Atom]. An atom identifier exists
 *  in exactly one subspecies.
 *
 *  @param A
 *      Type of atoms.
 */
interface MoleculeComplex<A : Atom> : Complex<Species> {
    override fun atoms(): Collection<A> =
        super.atoms().map {
            @Suppress("UNCHECKED_CAST")
            it as A
        }

    /**
     *  Identifier for this complex.
     *
     *  It must conform to XML NCName production.
     */
    val id: String

    /**
     *  Formal charge of this complex, which is the sum of the formal charges
     *  of the molecules.
     *
     *  If there are no molecules in this complex, an exception is raised.
     */
    val formalCharge: Double
        get() = molecules()
            .map { it.formalCharge }
            .reduce { acc, item -> acc + item }

    /**
     *  Molecules in this complex, or an empty collection if there are none.
     *
     *  Molecules are unique and are in the same order between iterations.
     *  Molecules in the collection are not guaranteed to be in any particular
     *  order. A subinterface or an implementation, however, is allowed to make
     *  specified guarantees.
     */
    fun molecules(): Collection<Molecule<A>> =
        iterator()
            .asSequence()
            .filter { it is Molecule<*> }
            .map {
                @Suppress("UNCHECKED_CAST")
                it as Molecule<A>
            }
            .toList()

    /**
     *  Gets the molecule that contains a given atom, or `null` if there is no
     *  such molecule.
     */
    fun getMoleculeWithAtom(atom: A): Molecule<A>?

    override fun clone(): MoleculeComplex<A> =
        @Suppress("UNCHECKED_CAST") (
            super.clone() as MoleculeComplex<A>
        )

    abstract override fun clone(deep: Boolean): MoleculeComplex<A>
}
