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
    /**
     *  @param molecules
     *      Molecules of the complex.
     */
    constructor(molecules: Iterable<Molecule<A>>): super(molecules)

    /**
     *  Copy constructor.
     *
     *  Molecules are cloned.
     */
    constructor(other: AbstractMoleculeComplex<A>): super(other)

    override fun getMoleculeWithAtom(atom: A): Molecule<A>? =
        molecules()
            .asSequence()
            .filter { it.containsAtom(atom) }
            .singleOrNull()
}
