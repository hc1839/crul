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

package chemistry.species.graph

import hypergraph.*

/**
 *  Graph (also called indexer) for storing information about a molecule
 *  complex, [chemistry.species.MoleculeComplex].
 *
 *  @property indexerName
 *      Name of the indexer.
 *
 *  @constructor
 */
open class MoleculeGraph @JvmOverloads constructor(
    val indexerName: String = uuid.Generator.inNCName()
) {
    /**
     *  Graph system containing the graph.
     */
    protected val graphSystem = GraphSystem()

    init {
        graphSystem.createGraph(indexerName)
    }

    /**
     *  Graph serving as the indexer.
     */
    val graph: hypergraph.Graph
        get() = graphSystem.getGraph(indexerName)!!

    /**
     *  Fragment-edge type.
     */
    val fragmentEdgeType = graph.createVertex()

    /**
     *  Atom-vertex type.
     */
    val atomVertexType = graph.createVertex()

    /**
     *  Type of edge type used by an edge that represents a bond.
     */
    val bondEdgeTypeType = graph.createVertex()

    /**
     *  Property type for a bond order.
     */
    val bondOrderPropertyType = graph.createVertex()

    init {
        fragmentEdgeType.addName(FRAGMENT_EDGE_TYPE_NAME)
        atomVertexType.addName(ATOM_VERTEX_TYPE_NAME)

        bondEdgeTypeType.addName(BOND_EDGE_TYPE_TYPE_NAME)
        bondOrderPropertyType.addName(BOND_ORDER_PROPERTY_TYPE_NAME)
    }

    /**
     *  Creates a vertex that has the type, [atomVertexType], with the name,
     *  `atomName`.
     *
     *  A new fragment edge is automatically created to contain the atom
     *  vertex.
     */
    open fun createAtomVertex(atomName: String): Vertex {
        if (graph.getVertexByName(atomName) != null) {
            throw IllegalArgumentException(
                "Atom vertex with the same name exists: $atomName"
            )
        }

        val atomVertex = graph.createVertex()
        atomVertex.addName(atomName)
        atomVertex.addType(atomVertexType)

        val fragmentEdge = graph.createEdge(fragmentEdgeType)
        fragmentEdge.addVertex(atomVertex)

        return atomVertex
    }

    /**
     *  Gets an atom vertex by its name, `atomName`.
     */
    fun getAtomVertexByName(atomName: String): Vertex? {
        val atomVertex = graph.getVertexByName(atomName)

        if (atomVertex != null && !atomVertex.isInstanceOf(atomVertexType)) {
            return null
        } else {
            return atomVertex
        }
    }

    /**
     *  Gets bond edges that an atom vertex is participating in.
     */
    protected fun existingBondEdges(atomVertex: Vertex) =
        atomVertex.edges.filter { edge ->
            (edge.type as Vertex).isInstanceOf(bondEdgeTypeType)
        }

    /**
     *  Gets the fragment edge that an atom vertex is participating in.
     */
    protected fun existingFragmentEdge(atomVertex: Vertex): Edge {
        val fragmentEdges = atomVertex.getEdgesByType(fragmentEdgeType)

        return if (fragmentEdges.count() == 1) {
            fragmentEdges.first()
        } else {
            throw RuntimeException(
                "[Internal Error] " +
                "Atom is not a member of exactly one fragment."
            )
        }
    }

    /**
     *  Creates a bond edge representing a bond with `order` and containing two
     *  atoms with names `atom1Name` and `atom2Name`.
     *
     *  `atom1Name` and `atom2Name` must refer to names of existing atom
     *  vertices must not already be participating in a bond.
     *
     *  If creating a bond between the two atoms causes two molecules to become
     *  one, the corresponding fragment edges are combined.
     */
    open fun createBondEdge(
        atom1Name: String,
        atom2Name: String,
        order: String
    ) {
        val atomNames = listOf(atom1Name, atom2Name)

        val atomVertices = atomNames.map { atomName ->
            val atomVertex =
                graph.getVertexByName(atomName)

            if (atomVertex == null) {
                throw IllegalArgumentException(
                    "No such atom: $atomName"
                )
            }

            atomVertex
        }

        val atom1Vertex = atomVertices[0]!!
        val atom2Vertex = atomVertices[1]!!

        if (
            existingBondEdges(atom1Vertex).flatMap { it.vertices }.any {
                it.names.contains(atom2Name)
            }
        ) {
            throw IllegalArgumentException(
                "Atoms are already participating in a bond."
            )
        }

        val fragment1Edge = existingFragmentEdge(atom1Vertex)

        // Check whether the new bond is between two molecules and combine the
        // corresponding two fragment edges if so.
        if (!fragment1Edge.vertices.any { it.names.contains(atom2Name) }) {
            val fragment1Vertices = fragment1Edge.vertices
            val fragment2Edge = existingFragmentEdge(atom2Vertex)
            val fragment2Vertices = fragment2Edge.vertices

            // Fragment to merge in the vertices from the other fragment.
            var fragmentEdgeTarget:
                Pair<Edge, List<Vertex>>

            // Fragment edge to be deleted.
            var fragmentEdgeTbd:
                Pair<Edge, List<Vertex>>

            if (fragment1Vertices.count() > fragment2Vertices.count()) {
                fragmentEdgeTarget = fragment1Edge to fragment1Vertices
                fragmentEdgeTbd = fragment2Edge to fragment2Vertices
            } else {
                fragmentEdgeTarget = fragment2Edge to fragment2Vertices
                fragmentEdgeTbd = fragment1Edge to fragment1Vertices
            }

            fragmentEdgeTbd.first.remove()

            // Add vertices from the fragment edge that is to be deleted.
            for (vertex in fragmentEdgeTbd.second) {
                fragmentEdgeTarget.first.addVertex(vertex)
            }
        }

        val bondEdgeType = createBondEdgeType(order)

        val bondEdge = graph.createEdge(bondEdgeType)
        bondEdge.addVertex(atom1Vertex)
        bondEdge.addVertex(atom2Vertex)
    }

    /**
     *  Gets bond edges.
     */
    fun getBondEdges(): List<Edge> {
        val bondEdgeTypes =
            graph.getVerticesByType(bondEdgeTypeType)

        return bondEdgeTypes.flatMap {
            graph.getEdgesByType(it)
        }
    }

    /**
     *  Gets the type of bond edges with `order`.
     */
    fun getBondEdgeTypeByOrder(order: String): Vertex? =
        graph.getVertexByName(bondEdgeTypeName(order))

    /**
     *  Creates a name for the type of a bond edge using the bond order.
     */
    protected fun bondEdgeTypeName(order: String): String {
        if (!xml.Datatype.isNCName("_" + order)) {
            throw IllegalArgumentException(
                "Order does not conform to XML NCName production " +
                "when prepended with an underscore: _$order"
            )
        }

        return BOND_EDGE_TYPE_NAME_PREFIX + order
    }

    /**
     *  Creates a type for bond edges with the bond order, `order`.
     */
    protected fun createBondEdgeType(order: String = "other"): Vertex {
        if (!xml.Datatype.isNCName("_" + order)) {
            throw IllegalArgumentException(
                "Order does not conform to XML NCName production " +
                "when prepended with an underscore: _$order"
            )
        }

        var bondEdgeType = getBondEdgeTypeByOrder(order)

        if (bondEdgeType == null) {
            bondEdgeType = graph.createVertex()

            bondEdgeType.addName(bondEdgeTypeName(order))
            bondEdgeType.addType(bondEdgeTypeType)
            bondEdgeType.createProperty(
                bondOrderPropertyType,
                order
            )
        }

        return bondEdgeType
    }

    protected companion object {
        /**
         *  Name of the type for fragment edges.
         */
        const val FRAGMENT_EDGE_TYPE_NAME = "fragment"

        /**
         *  Name of the type for atom vertices.
         */
        const val ATOM_VERTEX_TYPE_NAME = "atom"

        /**
         *  Name of the type of bond-edge type.
         */
        const val BOND_EDGE_TYPE_TYPE_NAME = "bond"

        /**
         *  Name of the type of properties that store bond order.
         */
        const val BOND_ORDER_PROPERTY_TYPE_NAME = "bond_order"

        /**
         *  Prefix of the edge type for bond edges that is meant to be
         *  concatenated with the bond order.
         */
        const val BOND_EDGE_TYPE_NAME_PREFIX = "bond_with_order_"
    }
}
