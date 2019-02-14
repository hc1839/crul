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

package chemistry.species.format

import chemistry.species.Atom
import chemistry.species.AtomBuilder
import chemistry.species.BondBuilder
import chemistry.species.MoleculeComplex
import chemistry.species.MoleculeComplexBuilder

/**
 *  Abstract deserializer as a mutable builder for a data format representing a
 *  chemical complex.
 */
abstract class AbstractDeserializer<B : AbstractDeserializer<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    /**
     *  Builder for the atoms.
     */
    protected val atomBuilder: AtomBuilder<*>

    /**
     *  Builder for the bonds.
     */
    protected val bondBuilder: BondBuilder<*>

    /**
     *  Builder for the complex.
     */
    protected val complexBuilder: MoleculeComplexBuilder<*>

    /**
     *  @param atomBuilder
     *      Builder for the atoms that are constructed.
     *
     *  @param bondBuilder
     *      Builder for the bonds that are constructed.
     *
     *  @param complexBuilder
     *      Builder for the complex that is constructed.
     */
    constructor(
        atomBuilder: AtomBuilder<*>,
        bondBuilder: BondBuilder<*>,
        complexBuilder: MoleculeComplexBuilder<*>
    ) {
        this.atomBuilder = atomBuilder
        this.bondBuilder = bondBuilder
        this.complexBuilder = complexBuilder
    }

    /**
     *  Constructs a complex by deserialization of a data format.
     *
     *  @param A
     *      Type of atoms in the complex being constructed.
     *
     *  @param text
     *      Data format as text.
     */
    abstract fun <A : Atom> deserialize(text: String): MoleculeComplex<A>
}
