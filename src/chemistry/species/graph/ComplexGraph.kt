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
import hypergraph.compare.Equality

/**
 *  Graph (also called indexer) for storing information about a complex.
 *
 *  It is used by [chemistry.species.AbstractBasicComplex].
 *
 *  @property indexerName
 *      Name of the indexer.
 *
 *  @constructor
 */
open class ComplexGraph @JvmOverloads constructor(
    val indexerName: String = uuid.Generator.inNCName()
) {
    /**
     *  Graph system containing the graph.
     */
    val graphSystem = GraphSystem()

    init {
        graphSystem.createGraph(indexerName)
    }

    /**
     *  Graph serving as the indexer.
     */
    val graph: Graph
        get() = graphSystem.getGraph(indexerName)!!

    /**
     *  Fragment-edge type.
     */
    val fragmentEdgeType = graph.createVertex()

    /**
     *  Atom-vertex type.
     */
    val atomVertexType = graph.createVertex()

    init {
        fragmentEdgeType.addName(FRAGMENT_EDGE_TYPE_NAME)
        atomVertexType.addName(ATOM_VERTEX_TYPE_NAME)
    }

    /**
     *  Creates an edge that has the type, [fragmentEdgeType], and a proxy
     *  vertex for it with the name, `fragmentName`.
     */
    open fun createFragmentEdge(fragmentName: String): Edge {
        if (graph.getVertexByName(fragmentName) != null) {
            throw IllegalArgumentException(
                "Species with the same name as the fragment exists: " +
                fragmentName
            )
        }

        val fragmentProxy = graph.createVertex()
        fragmentProxy.addName(fragmentName)

        val fragmentEdge = graph.createEdge(fragmentEdgeType)
        fragmentEdge.proxy = fragmentProxy

        return fragmentEdge
    }

    /**
     *  Removes a fragment edge and its proxy vertex.
     */
    open fun removeFragmentEdge(fragmentName: String) {
        val fragmentProxy = graph.getVertexByName(fragmentName)

        if (fragmentProxy == null) {
            return
        }

        val fragmentEdge = fragmentProxy.proxied as Edge
        val atomVertices = fragmentEdge.vertices

        fragmentEdge.remove()
        fragmentProxy.remove()

        for (atomVertex in atomVertices) {
            atomVertex.remove()
        }
    }

    /**
     *  Creates a vertex that has the type, [atomVertexType], with atom name,
     *  `atomName`, and adds it to the fragment with name, `fragmentName`.
     *
     *  `atomName` must be unique among the vertices (including vertices that
     *  act as proxies for fragment edges).
     */
    open fun createAtomVertex(atomName: String, fragmentName: String): Vertex {
        if (graph.getVertexByName(atomName) != null) {
            throw IllegalArgumentException(
                "Species with the same name exists: " +
                atomName
            )
        }

        val fragmentEdge = getFragmentEdgeByName(fragmentName)

        if (fragmentEdge == null) {
            throw IllegalArgumentException(
                "No such fragment edge: $fragmentName"
            )
        }

        val atomVertex = graph.createVertex()
        atomVertex.addName(atomName)
        atomVertex.addType(atomVertexType)

        fragmentEdge.addVertex(atomVertex)

        return atomVertex
    }

    /**
     *  Removes an atom vertex that has the name, `atomName`.
     *
     *  Nothing is done if there is no such atom vertex.
     */
    open fun removeAtomVertex(atomName: String) {
        val atomVertex = graph.getVertexByName(atomName)

        if (atomVertex != null && atomVertex.isInstanceOf(atomVertexType)) {
            atomVertex.remove()
        }
    }

    /**
     *  Gets the fragment edges.
     */
    fun getFragmentEdges(): List<Edge> =
        graph.getEdgesByType(fragmentEdgeType)

    /**
     *  Gets the fragment that has `fragmentName` as the name of its proxy
     *  vertex.
     */
    fun getFragmentEdgeByName(fragmentName: String): Edge? {
        val fragmentVertex = graph.getVertexByName(fragmentName)

        if (fragmentVertex == null) {
            return null
        }

        val fragmentEdge = fragmentVertex.proxied as? Edge

        if (fragmentEdge == null) {
            return null
        }

        if (
            !Equality.constructIdentities(
                fragmentEdge.type,
                fragmentEdgeType
            )
        ) {
            return null
        }

        return fragmentEdge
    }

    /**
     *  Gets the atom vertex that has `atomName` as its name.
     */
    fun getAtomVertexByName(atomName: String): Vertex? {
        val atomVertex = graph.getVertexByName(atomName)

        if (atomVertex == null) {
            return null
        }

        if (!atomVertex.isInstanceOf(atomVertexType)) {
            return null
        }

        return atomVertex
    }

    protected companion object {
        /**
         *  Name of the vertex that is acting as the edge type for fragments.
         */
        const val FRAGMENT_EDGE_TYPE_NAME = "fragment"

        /**
         *  Name of the vertex that is acting as the vertex type for atoms.
         */
        const val ATOM_VERTEX_TYPE_NAME = "atom"
    }
}
