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

package chemistry.species.sandbox

/**
 *  Interface for a complex of molecules.
 *
 *  @param A
 *      Type of atoms in the molecules.
 */
interface MoleculeComplex<A : Atom> : Complex<Molecule<A>> {
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
     *  Molecules in this complex.
     */
    fun molecules(): Sequence<Molecule<A>> =
        iterator().asSequence()

    /**
     *  Gets the molecule that contains a given atom, or `null` if there is no
     *  such molecule.
     *
     *  @param atomName
     *      [Atom.name] of the atom. If there is no such atom, an exception is
     *      raised.
     */
    fun getMoleculeWithAtom(atomName: String): Molecule<A>?
}
