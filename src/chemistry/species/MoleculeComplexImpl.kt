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
 *  Default implementation of [MoleculeComplex].
 *
 *  @param A
 *      Type of atoms.
 */
internal class MoleculeComplexImpl<A : Atom> : AbstractMoleculeComplex<A> {
    /**
     *  @param subspecies
     *      Molecules and atoms of the complex.
     *
     *  @param id
     *      Identifier for this complex. It must conform to XML NCName
     *      production.
     */
    @JvmOverloads
    constructor(
        subspecies: Collection<Species>,
        id: String = crul.uuid.Generator.inNCName()
    ): super(subspecies, id)

    /**
     *  Copy constructor.
     */
    @JvmOverloads
    constructor(
        other: MoleculeComplexImpl<A>,
        deep: Boolean = false,
        id: String = other.id
    ): super(other, deep, id)

    override fun clone(deep: Boolean): MoleculeComplexImpl<A> =
        MoleculeComplexImpl(this, deep)

    override fun clone(deep: Boolean, id: String): MoleculeComplexImpl<A> =
        MoleculeComplexImpl(this, deep, id)
}
