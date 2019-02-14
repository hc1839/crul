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

package hypergraph

import hypergraph.base.Construct
import hypergraph.compare.Equality
import hypergraph.compare.Hashing
import hypergraph.indexer.*
import hypergraph.notify.*

import hypergraph.compare.VertexSetElement

/**
 *  Graph that acts a container for vertices and edges.
 */
class Graph : CanBeProxied,
    hypergraph.base.Graph
{
    override val id: String

    /**
     *  Weak reference to the parent graph system.
     */
    private val graphSystemRef: java.lang.ref.WeakReference<GraphSystem>

    /**
     *  @suppress
     *
     *  @param id
     *      ID of the new graph. It must be unique within `parentGraphSystem`
     *      and must conform to XML NCName production.
     *
     *  @param parentGraphSystem
     *      [GraphSystem] within which to create the graph.
     */
    constructor(
        @Suppress("UNUSED_PARAMETER")
        friendAccess: GraphSystem.FriendAccess,
        id: String,
        parentGraphSystem: GraphSystem
    ): super()
    {
        if (!xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "ID does not conform to XML NCName production: $id"
            )
        }

        graphSystemRef =
            java.lang.ref.WeakReference(parentGraphSystem)

        this.id = id

    }

    /**
     *  Redirector for the constructs in this graph.
     */
    private val constructRedirector = ConstructRedirector<Construct>()

    /**
     *  Indexer for the vertices in this graph.
     */
    private val vertexIndexer = VertexIndexer<Vertex>()

    /**
     *  Indexer for the edges in this graph.
     */
    private val edgeIndexer = EdgeIndexer<Edge>()

    /**
     *  Indexer for the properties in this graph.
     */
    private val propertyIndexer = PropertyIndexer<Property>()

    override val parent = null

    override val graph = this

    override fun getTerminalProxied() =
        graph

    override fun remove() {
        graphSystem.removeGraph(Friendship, this)
    }

    override val vertices: List<Vertex>
        get() = vertexIndexer.constructs

    override val edges: List<Edge>
        get() = edgeIndexer.constructs

    override val graphSystem: GraphSystem
        get() {
            val graphSystem = graphSystemRef.get()

            if (graphSystem == null) {
                throw RuntimeException(
                    "Graph system no longer exists."
                )
            }

            return graphSystem
        }

    override fun getConstructById(constructId: String) =
        if (constructId == id) {
            this
        } else {
            constructRedirector[constructId]
        }

    override fun getVertexByName(name: String) =
        vertexIndexer.getByName(name)

    /**
     *  Gets vertices where [hypergraph.base.Vertex.types] contains `type`.
     */
    fun getVerticesByType(type: Vertex): List<Vertex> =
        vertexIndexer.constructs.filter { vertex ->
            vertex.isInstanceOf(type)
        }

    /**
     *  Gets edges where [hypergraph.base.Edge.type] is `type`.
     */
    fun getEdgesByType(type: Vertex): List<Edge> =
        edgeIndexer.constructs.filter { edge ->
            Equality.constructIdentities(edge.type, type)
        }

    /**
     *  Creates a vertex and adds it to this graph.
     */
    private fun createVertex(id: String?): Vertex {
        if (id == null) {
            var newId: String

            do {
                newId = uuid.Generator.inNCName()
            } while (vertexIndexer.contains(newId))

            return createVertex(newId)
        }

        val vertex = Vertex(Friendship, id, this)

        vertexIndexer.put(vertex.id, vertex)
        constructRedirector.put(vertex.id, vertex)

        return vertex
    }

    override fun createVertex() =
        createVertex(null)

    /**
     *  @suppress
     *
     *  Removes a vertex from this graph.
     */
    @Suppress("UNUSED_PARAMETER")
    fun removeVertex(
        friendAccess: Vertex.FriendAccess,
        vertex: Vertex
    ) {
        val vertexId = vertex.id

        vertexIndexer.remove(vertexId)
        constructRedirector.remove(vertexId)
    }

    /**
     *  Creates an edge in this graph.
     */
    private fun createEdge(id: String?, type: Vertex): Edge {
        if (id == null) {
            var newId: String

            do {
                newId = uuid.Generator.inNCName()
            } while (edgeIndexer.contains(newId))

            return createEdge(newId, type)
        }

        val edge = Edge(Friendship, id, type, this)

        edgeIndexer.put(edge.id, edge)
        constructRedirector.put(edge.id, edge)

        return edge
    }

    /**
     *  @suppress
     *
     *  Removes an edge from this graph.
     */
    @Suppress("UNUSED_PARAMETER")
    fun removeEdge(
        friendAccess: Edge.FriendAccess,
        edge: Edge
    ) {
        val edgeId = edge.id

        edgeIndexer.remove(edgeId)
        constructRedirector.remove(edgeId)
    }

    override fun createEdge(type: hypergraph.base.Vertex) =
        createEdge(null, type as Vertex)

    /**
     *  Implementation of the overloaded friendship version.
     */
    private fun mergeVerticesRedirect(target: Vertex, other: Vertex) {
        val otherId = other.id
        val otherInvolvedEdges = other.edges
        val otherInvolvedInTypes =
            other.getInvolvedInTypes(Friendship)

        target.mergeInKeep(Friendship, other)

        constructRedirector.redirect(otherId, target.id)
        other.setIsRedirected(Friendship, true)

        vertexIndexer.remove(otherId)
        vertexIndexer.reindex(target.id)

        val modifiedEdges = arrayListOf<Edge>()

        // Collect and notify edges that use 'other' as the type.
        for (otherInvolvedInType in otherInvolvedInTypes) {
            when (otherInvolvedInType) {
                is Vertex -> {
                    val response = otherInvolvedInType.notify(
                        Friendship,
                        VertexRedirected<Graph>(
                            this,
                            otherId,
                            target.id
                        )
                    )

                    if (response.error != null) {
                        throw RuntimeException(response.error.what)
                    }
                }

                is Edge ->
                    modifiedEdges.add(otherInvolvedInType)
            }
        }

        // Collect notify edges that have 'other' as a participant.
        for (otherEdge in otherInvolvedEdges) {
            val response = otherEdge.notify(
                Friendship,
                VertexRedirected<Graph>(
                    this,
                    otherId,
                    target.id
                )
            )

            if (response.error != null) {
                throw RuntimeException(response.error.what)
            }

            modifiedEdges.add(otherEdge)
        }

        // Merge edges that have become equal because of the merging of the two
        // vertices.
        for (modifiedEdge in modifiedEdges) {
            val modifiedEdgeHashCode = Hashing.edge(modifiedEdge)

            for (edge in edgeIndexer.constructs) {
                if (
                    edge !== modifiedEdge &&
                    Hashing.edge(edge) == modifiedEdgeHashCode &&
                    Equality.edges(edge, modifiedEdge)
                ) {
                    mergeEdgesRedirect(edge, modifiedEdge)
                    break
                }
            }
        }
    }

    /**
     *  Merges `other` into `target` and redirects `other` to `target`.
     */
    private fun mergeEdgesRedirect(target: Edge, other: Edge) {
        val otherId = other.id

        target.mergeInKeep(Friendship, other)
        constructRedirector.redirect(otherId, target.id)
        other.setIsRedirected(Friendship, true)

        edgeIndexer.remove(otherId)
        edgeIndexer.reindex(target.id)
    }

    /**
     *  @suppress
     *
     *  Merges `other` into `target` and redirects `other` to `target`.
     */
    @Suppress("UNUSED_PARAMETER")
    fun mergeVerticesRedirect(
        friendAccess: CanBeProxied.FriendAccess,
        target: Vertex,
        other: Vertex
    ) {
        mergeVerticesRedirect(target, other)
    }

    /**
     *  @suppress
     *
     *  Notifies this graph of a specific event indicated by `message` from a
     *  vertex.
     *
     *  The recognized message types are
     *      - [hypergraph.notify.VertexNameAdded]
     *      - [hypergraph.notify.VertexNameRemoved]
     *      - [hypergraph.notify.PropertyAdded]
     *      - [hypergraph.notify.PropertyRemoved]
     *      - [hypergraph.notify.PropertiesMerged]
     */
    @Suppress("UNUSED_PARAMETER")
    fun notify(
        friendAccess: Vertex.FriendAccess,
        message: Message<Vertex>
    ): Response = when (message) {
        is VertexNameAdded -> {
            val sourceVertex = message.source
            val otherVertex = vertexIndexer.getByName(message.name)

            if (otherVertex == null) {
                vertexIndexer.reindex(sourceVertex.id)
            } else {
                mergeVerticesRedirect(sourceVertex, otherVertex)
            }

            Response(BasicSignal.ACKNOWLEDGED)
        }

        is VertexNameRemoved -> {
            vertexIndexer.reindex(message.source.id)

            Response(BasicSignal.ACKNOWLEDGED)
        }

        is PropertyAdded<*, *> -> {
            val property = message.property as Property

            propertyIndexer.put(property.id, property)
            constructRedirector.put(property.id, property)

            Response(BasicSignal.ACKNOWLEDGED)
        }

        is PropertyRemoved -> {
            val propertyId = message.propertyId

            constructRedirector.remove(propertyId)
            propertyIndexer.remove(propertyId)

            Response(BasicSignal.ACKNOWLEDGED)
        }

        is PropertiesMerged -> {
            val sourceVertex = message.source
            val targetPropertyId = message.targetPropertyId
            val otherPropertyId = message.otherPropertyId

            val sourceVertexHashCode = Hashing.vertex(sourceVertex)

            // Find and merge vertices that have become equal because of the
            // properties having merged.
            for (vertex in vertexIndexer.constructs) {
                if (
                    vertex !== sourceVertex &&
                    Hashing.vertex(vertex) == sourceVertexHashCode &&
                    Equality.vertices(vertex, sourceVertex)
                ) {
                    mergeVerticesRedirect(sourceVertex, vertex)
                }
            }

            constructRedirector.redirect(otherPropertyId, targetPropertyId)

            Response(BasicSignal.ACKNOWLEDGED)
        }

        else ->
            Response(
                BasicSignal.DENIED,
                Error("Unrecognized message type.")
            )
    }

    /**
     *  @suppress
     *
     *  Notifies this graph of a specific event indicated by `message` from an
     *  edge.
     *
     *  The recognized message types are
     *      - [hypergraph.notify.VertexAdded]
     *      - [hypergraph.notify.VertexRemoved]
     *      - [hypergraph.notify.EdgeTypeModified]
     */
    @Suppress("UNUSED_PARAMETER")
    fun notify(
        friendAccess: Edge.FriendAccess,
        message: Message<Edge>
    ): Response {
        // Finds and merges equivalent edges.
        fun sweepAndMerge(sourceEdge: Edge) {
            val sourceEdgeHashCode = Hashing.edge(sourceEdge)

            for (edge in edgeIndexer.constructs) {
                if (
                    edge !== sourceEdge &&
                    Hashing.edge(edge) == sourceEdgeHashCode &&
                    Equality.edges(edge, sourceEdge)
                ) {
                    mergeEdgesRedirect(sourceEdge, edge)
                }
            }
        }

        return when (message) {
            is VertexAdded<*, *> -> {
                sweepAndMerge(message.source)

                Response(BasicSignal.ACKNOWLEDGED)
            }

            is VertexRemoved<*, *> -> {
                sweepAndMerge(message.source)

                Response(BasicSignal.ACKNOWLEDGED)
            }

            is EdgeTypeModified<*, *> -> {
                sweepAndMerge(message.source)

                Response(BasicSignal.ACKNOWLEDGED)
            }

            else -> {
                Response(
                    BasicSignal.DENIED,
                    Error("Unrecognized message type.")
                )
            }
        }
    }

    private object FriendKey : visaccess.FriendKey()

    open class FriendAccess(key: visaccess.FriendKey) :
        visaccess.FriendAccess
    {
        init {
            if (key != FriendKey) {
                throw IllegalArgumentException(
                    "Invalid friend key."
                )
            }
        }
    }

    private object Friendship : FriendAccess(FriendKey)
}
