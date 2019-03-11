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
 *  Skeletal implementation of [Molecule].
 *
 *  Only a subclass of [MoleculeComplexBuilder] should be instantiating the
 *  corresponding subclass of this class, since it must be ensured that every
 *  pair of atoms in a molecule are connected by bonds.
 *
 *  @param A
 *      Type of atoms in this molecule.
 */
abstract class AbstractMolecule<A : Atom> :
    AbstractFragment<A>,
    Molecule<A>
{
    /**
     *  Graph system for bond information.
     */
    private val graphSystem: GraphSystem = GraphSystem()

    /**
     *  Graph that contains bond information.
     */
    private val graph: Graph
        get() = graphSystem.getGraph(graphSystem.graphIds.first())!!

    /**
     *  Vertex acting as the edge type for a bond in the graph.
     */
    private val bondEdgeType: Vertex

    /**
     *  Vertex acting as the property type for a bond order.
     */
    private val bondOrderPropertyType: Vertex

    /**
     *  Builder for constructing bonds, or `null` if this molecule is a
     *  singleton.
     */
    protected val bondBuilder: BondBuilder<*>?

    /**
     *  Populates the graph with bond information.
     *
     *  Atoms are not cloned.
     *
     *  It is an initializer that is to be called from a constructor.
     */
    private fun initialize(bonds: Collection<Bond<A>>) {
        // Add each bond as an edge.
        for (bond in bonds) {
            // Add and store each atom as a vertex.
            val atomVertices = bond
                .atoms()
                .map { atom ->
                    val existingVertex = this
                        .graph
                        .getVertexByName(atom.id)

                    // Check for another atom with the same ID.
                    if (existingVertex == null) {
                        val atomVertex = this.graph.createVertex()

                        atomVertex.addName(atom.id)
                        atomVertex.userData = atom

                        atomVertex
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val existingAtom = existingVertex.userData as A

                        if (existingAtom != atom) {
                            throw IllegalArgumentException(
                                "Another atom with the same ID exists: " +
                                atom.id
                            )
                        }

                        existingVertex
                    }
                }

            // Create a bond edge, and add atom vertices to it.
            val bondEdge = this.graph.createEdge(this.bondEdgeType)
            bondEdge.addVertex(atomVertices[0])
            bondEdge.addVertex(atomVertices[1])

            // Create a proxy for the bond edge, and create a property to store
            // the bond order.
            val bondEdgeProxy = this.graph.createVertex()
            bondEdgeProxy.createProperty(
                this.bondOrderPropertyType,
                bond.order
            )
            bondEdge.proxy = bondEdgeProxy
        }
    }

    /**
     *  Populates the graph with one atom acting as a singleton molecule.
     *
     *  Atoms are not cloned.
     *
     *  It is an initializer that is to be called from a constructor.
     */
    private fun initialize(atom: A) {
        // Add and store the atom as a vertex.
        val atomVertex = this.graph.createVertex()
        atomVertex.userData = atom
    }

    /**
     *  @param bonds
     *      Bonds of the molecule. They are not checked for connectivity, which
     *      is done by [MoleculeComplexBuilder].
     *
     *  @param bondBuilder
     *      Builder for constructing bonds.
     */
    constructor(
        bonds: Collection<Bond<A>>,
        bondBuilder: BondBuilder<*> = BondBuilder.newInstance()
    ): super(
        bonds
            .flatMap { it.atoms().distinct() }
            .toSet()
    ) {
        this.graphSystem.createGraph(crul.uuid.Generator.inNCName())
        this.bondEdgeType = this.graph.createVertex()
        this.bondOrderPropertyType = this.graph.createVertex()
        this.bondBuilder = bondBuilder

        initialize(bonds)
    }

    /**
     *  Constructs a singleton molecule.
     *
     *  @param atom
     *      Atom acting as a singleton molecule.
     */
    constructor(atom: A): super(listOf(atom)) {
        this.graphSystem.createGraph(crul.uuid.Generator.inNCName())
        this.bondEdgeType = this.graph.createVertex()
        this.bondOrderPropertyType = this.graph.createVertex()
        this.bondBuilder = null

        initialize(atom)
    }

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractMolecule<A>): super(other) {
        this.graphSystem.createGraph(crul.uuid.Generator.inNCName())
        this.bondEdgeType = this.graph.createVertex()
        this.bondOrderPropertyType = this.graph.createVertex()
        this.bondBuilder = other.bondBuilder

        if (other.bonds().firstOrNull() == null) {
            initialize(other.atoms().first())
        } else {
            initialize(other.bonds())
        }

        // Replace the other atom with the cloned version stored in the
        // superclass.
        for (atom in _subspecies) {
            val atomVertex = this.graph.getVertexByName(atom.id)!!
            atomVertex.userData = atom
        }
    }

    override fun containsAtom(atom: A): Boolean {
        val atomVertex = graph.getVertexByName(atom.id)

        return atomVertex?.userData == atom
    }

    override fun bonds(): Collection<Bond<A>> =
        graph.getEdgesByType(bondEdgeType).map { bondEdge ->
            // Atoms of the bond.
            val atoms = bondEdge
                .vertices
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it.userData as A
                }

            val bondOrder = bondEdge
                .proxy!!
                .getPropertiesByType(bondOrderPropertyType)
                .first()
                .value

            bondBuilder!!
                .atom1(atoms[0])
                .atom2(atoms[1])
                .order(bondOrder)
                .build<A>()
        }

    override fun getBondsByAtom(atom: A): Set<Bond<A>> {
        val atomVertex = graph.getVertexByName(atom.id)

        if (atomVertex?.userData != atom) {
            throw IllegalArgumentException(
                "No such atom: ${atom.id}"
            )
        }

        val involvedBondEdges = atomVertex
            .edges
            .filter { it.type == bondEdgeType }

        val bonds = involvedBondEdges.map { bondEdge ->
            // Other atom in the participating bond.
            val otherAtom = bondEdge
                .vertices
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it.userData as A
                }
                .filter { it != atom }
                .single()

            val bondOrder = bondEdge
                .proxy!!
                .getPropertiesByType(bondOrderPropertyType)
                .first()
                .value

            bondBuilder!!
                .atom1(atom)
                .atom2(otherAtom)
                .order(bondOrder)
                .build<A>()
        }

        return bonds.toSet()
    }

    override fun getBond(atom1: A, atom2: A): Bond<A>? {
        val atomVertices = listOf(atom1, atom2)
            .map { atom ->
                val atomVertex = graph.getVertexByName(atom.id)

                if (atomVertex != null) {
                    atomVertex
                } else {
                    throw IllegalArgumentException(
                        "No such atom: ${atom.id}"
                    )
                }
            }

        // Singleton or empty list of the bond edge that both atoms are
        // participating in.
        val bondEdge = atomVertices
            .first()
            .edges
            .filter { it.vertices.contains(atomVertices[1]) }

        return if (bondEdge.isEmpty()) {
            null
        } else {
            val bondOrder = bondEdge
                .single()
                .proxy!!
                .getPropertiesByType(bondOrderPropertyType)
                .first().value

            bondBuilder!!
                .atom1(atom1)
                .atom2(atom2)
                .order(bondOrder)
                .build<A>()
        }
    }
}
