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

import crul.hypergraph.Edge
import crul.hypergraph.Graph
import crul.hypergraph.GraphSystem
import crul.hypergraph.Vertex

/**
 *  Skeletal implementation of [MoleculeComplex].
 *
 *  @param A
 *      Type of atoms in this molecule.
 */
abstract class AbstractMoleculeComplex<A : Atom> :
    AbstractComplex<Molecule<A>>,
    MoleculeComplex<A>
{
    override val id: String

    /**
     *  @param molecules
     *      Molecules of the complex.
     *
     *  @param id
     *      Identifier for this complex. It must conform to XML NCName
     *      production.
     */
    @JvmOverloads
    constructor(
        molecules: Iterable<Molecule<A>>,
        id: String = crul.uuid.Generator.inNCName()
    ): super(molecules)
    {
        if (!crul.xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "ID does not conform to XML NCName production: $id"
            )
        }

        this.id = id
    }

    /**
     *  Copy constructor.
     *
     *  Molecules are cloned.
     *
     *  @param id
     *      Identifier to use for the copied complex. It must conform to XML
     *      NCName production.
     */
    @JvmOverloads
    constructor(
        other: AbstractMoleculeComplex<A>,
        id: String = other.id
    ): super(other)
    {
        if (!crul.xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "ID does not conform to XML NCName production: $id"
            )
        }

        this.id = id
    }

    override fun getMoleculeWithAtom(atom: A): Molecule<A>? =
        molecules()
            .asSequence()
            .filter { it.containsAtom(atom) }
            .singleOrNull()
}
