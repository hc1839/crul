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

package chemistry.species.impl

import chemistry.species.AbstractMolecule
import chemistry.species.Atom
import chemistry.species.Bond
import chemistry.species.BondBuilder
import chemistry.species.Molecule
import hypergraph.Edge
import hypergraph.Graph
import hypergraph.GraphSystem
import hypergraph.Vertex

/**
 *  Default implementation of [Molecule].
 *
 *  @param A
 *      Type of atoms in this molecule.
 */
internal class MoleculeImpl<A : Atom> : AbstractMolecule<A> {
    /**
     *  @param bonds
     *      Bonds of the molecule.
     */
    constructor(bonds: Set<Bond<A>>): super(bonds)

    /**
     *  Constructs a singleton molecule.
     *
     *  @param atom
     *      Atom acting as a singleton molecule.
     */
    constructor(atom: A): super(atom)

    /**
     *  Copy constructor.
     */
    constructor(other: MoleculeImpl<A>): super(other)

    override fun clone(): MoleculeImpl<A> =
        MoleculeImpl(this)
}
