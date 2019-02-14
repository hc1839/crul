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
 *  Default implementation of [MoleculeComplex].
 *
 *  Only [MoleculeComplexBuilder] should be instantiating this class.
 *
 *  @param A
 *      Type of atoms in the molecules.
 */
internal class MoleculeComplexImpl<A : Atom> : MoleculeComplex<A> {
    /**
     *  Backing property for the list of molecules.
     */
    private val _molecules: MutableList<Molecule<A>>

    /**
     *  @param molecules
     *      Molecules of the complex.
     */
    constructor(molecules: Iterator<Molecule<A>>) {
        this._molecules = molecules.asSequence().toMutableList()
    }

    /**
     *  Copy constructor.
     *
     *  Molecules are cloned.
     */
    constructor(other: MoleculeComplexImpl<A>): this(
        other
            .molecules()
            .asSequence()
            .map { it.clone() }
            .iterator()
    )

    override fun atoms(): Iterator<A> =
        _molecules
            .asSequence()
            .flatMap { it.atoms().asSequence() }
            .iterator()

    override fun molecules(): Iterator<Molecule<A>> =
        _molecules.iterator()

    override fun getMoleculeWithAtom(atom: A): Molecule<A>? =
        _molecules
            .filter { it.containsAtom(atom) }
            .singleOrNull()

    override fun clone(): MoleculeComplex<A> =
        MoleculeComplexImpl(this)
}
