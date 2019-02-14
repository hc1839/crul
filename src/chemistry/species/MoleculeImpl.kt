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

import hypergraph.Edge
import hypergraph.Graph
import hypergraph.GraphSystem
import hypergraph.Vertex

/**
 *  Default implementation of [Molecule].
 *
 *  Only [MoleculeComplexBuilder] should be instantiating this class, since it
 *  must be ensured that every pair of atoms in a molecule are connected by
 *  bonds.
 *
 *  @param A
 *      Type of atoms in this molecule.
 */
internal class MoleculeImpl<A : Atom> :
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
     *  Populates the graph with bond information.
     *
     *  Atoms are not cloned.
     *
     *  It is an initializer that is to be called from a constructor.
     */
    private fun initialize(bonds: Iterator<Bond<A>>) {
        // Add each bond as an edge.
        for (bond in bonds) {
            // Add and store each atom as a vertex.
            val atomVertices = bond
                .atoms()
                .asSequence()
                .map { atom ->
                    val existingVertex = this
                        .graph
                        .getVertexByName(atom.name)

                    // Check for another atom with the same name.
                    if (existingVertex == null) {
                        val atomVertex = this.graph.createVertex()

                        atomVertex.addName(atom.name)
                        atomVertex.userData = atom

                        atomVertex
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val existingAtom = existingVertex.userData as A

                        if (existingAtom != atom) {
                            throw IllegalArgumentException(
                                "Another atom with the same name exists: " +
                                atom.name
                            )
                        }

                        existingVertex
                    }
                }
                .toList()

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
     *      Bonds of the molecule as a set.
     */
    constructor(bonds: Set<Bond<A>>): super(
        bonds
            .flatMap { it.atoms().asSequence().toList() }
            .toSet()
            .iterator()
    ) {
        this.graphSystem.createGraph(uuid.Generator.inNCName())

        this.bondEdgeType = this.graph.createVertex()
        this.bondOrderPropertyType = this.graph.createVertex()

        initialize(bonds.iterator())
    }

    /**
     *  @param bonds
     *      Bonds of the molecule as an iterator.
     */
    constructor(bonds: Iterator<Bond<A>>):
        this(bonds.asSequence().toSet())

    /**
     *  Constructs a singleton molecule.
     *
     *  @param atom
     *      Atom acting as a singleton molecule.
     */
    constructor(atom: A): super(listOf(atom).iterator()) {
        this.graphSystem.createGraph(uuid.Generator.inNCName())

        this.bondEdgeType = this.graph.createVertex()
        this.bondOrderPropertyType = this.graph.createVertex()

        initialize(atom)
    }

    /**
     *  Copy constructor.
     */
    constructor(other: MoleculeImpl<A>): super(other) {
        this.graphSystem.createGraph(uuid.Generator.inNCName())

        this.bondEdgeType = this.graph.createVertex()
        this.bondOrderPropertyType = this.graph.createVertex()

        if (other.bonds().asSequence().firstOrNull() == null) {
            initialize(other.atoms().asSequence().first())
        } else {
            initialize(other.bonds())
        }

        // Replace the other atom with the cloned version stored in the
        // superclass.
        for (atom in _atoms) {
            val atomVertex = this.graph.getVertexByName(atom.name)!!

            atomVertex.userData = atom
        }
    }

    override fun containsAtom(atom: A): Boolean {
        val atomVertex = graph.getVertexByName(atom.name)

        return atomVertex?.userData == atom
    }

    override fun bonds(): Iterator<Bond<A>> =
        object : AbstractIterator<Bond<A>>() {
            private val bondEdges: Iterator<Edge> = graph
                .getEdgesByType(bondEdgeType)
                .iterator()

            override fun computeNext() {
                if (!bondEdges.hasNext()) {
                    done()
                    return
                }

                val bondEdge = bondEdges.next()

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

                setNext(
                    BondBuilder
                        .create()
                        .atom1(atoms[0])
                        .atom2(atoms[1])
                        .order(bondOrder)
                        .build<A>()
                )
            }
        }

    override fun getBondsByAtom(atom: A): Set<Bond<A>> {
        val atomVertex = graph.getVertexByName(atom.name)

        if (atomVertex?.userData != atom) {
            throw IllegalArgumentException(
                "No such atom: ${atom.name}"
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

            BondBuilder
                .create()
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
                val atomVertex = graph.getVertexByName(atom.name)

                if (atomVertex != null) {
                    atomVertex
                } else {
                    throw IllegalArgumentException(
                        "No such atom: ${atom.name}"
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

            BondBuilder
                .create()
                .atom1(atom1)
                .atom2(atom2)
                .order(bondOrder)
                .build<A>()
        }
    }

    override fun clone(): Molecule<A> =
        MoleculeImpl(this)
}
