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
import hypergraph.indexer.*
import hypergraph.notify.*
import hypergraph.reference.ConstructReference

/**
 *  Edge.
 */
class Edge :
    CanBeProxied,
    hypergraph.base.Edge
{
    private val edgeRef: ConstructReference<Edge>

    private val graphRef: ConstructReference<Graph>

    override var type: hypergraph.base.Vertex
        get() {
            if (isRedirected) {
                return edgeRef.get()!!.type
            }

            return field
        }
        set(newValue) {
            if (isRedirected) {
                edgeRef.get()!!.type = newValue
                return
            }

            val oldType = field as? Vertex
            val newType = newValue as Vertex

            oldType?.removeInvolvedInType(Friendship, edgeRef)
            newType.addInvolvedInType(Friendship, edgeRef)

            field = newValue

            if (oldType != null) {
                /**
                 *  Notify the graph that the edge type has been modified.
                 */
                val response = graph.notify(
                    Friendship,
                    EdgeTypeModified(this, oldType, newType)
                )

                if (response.error != null) {
                    throw RuntimeException(response.error.what)
                }
            }
        }

    /**
     *  ID that was passed to the constructor.
     */
    private val origId: String

    /**
     *  @suppress
     *
     *  Constructs an edge with the given ID and type.
     *
     *  @param id
     *      ID of the edge to use. It must be unique within the graph and must
     *      conform to XML NCName production.
     *
     *  @param parentGraph
     *      Graph that the new edge belongs to.
     */
    constructor(
        @Suppress("UNUSED_PARAMETER")
        friendAccess: Graph.FriendAccess,
        id: String,
        type: Vertex,
        parentGraph: Graph
    ) {
        if (!xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "ID does not conform to XML NCName production: $id"
            )
        }

        edgeRef = ConstructReference<Edge>(
            parentGraph.graphSystem,
            parentGraph.id,
            id
        )

        graphRef = ConstructReference<Graph>(
            parentGraph.graphSystem,
            parentGraph.id,
            parentGraph.id
        )

        if (graphRef.get() == null) {
            throw IllegalArgumentException(
                "Graph does not exist."
            )
        }

        if (graphRef.get()!!.getConstructById(id) != null) {
            throw IllegalArgumentException(
                "ID is not unique within the graph: $id"
            )
        }

        this.origId = id
        this.type = type

        type.addInvolvedInType(Friendship, edgeRef)
    }

    /**
     *  Whether this edge no longer belongs to the graph.
     */
    private var isRemoved = false

    /**
     *  @suppress
     *
     *  Sets whether this edge is redirected to a terminal edge.
     */
    @Suppress("UNUSED_PARAMETER")
    fun setIsRedirected(
        friendAccess: Graph.FriendAccess,
        value: Boolean
    ) {
        isRedirected = value
    }

    override val graph: Graph
        get() {
            if (isRemoved) {
                throw RuntimeException(
                    "Edge no longer belongs to a graph."
                )
            }

            val referent = graphRef.get()

            if (referent == null) {
                throw RuntimeException(
                    "Graph no longer exists."
                )
            }

            return referent
        }

    override fun getTerminalProxied() =
        edgeRef.get()!!

    override val id: String
        get() {
            if (isRedirected) {
                return edgeRef.get()!!.id
            }

            return origId
        }

    /**
     *  Indexer for the vertices that are participating in this edge.
     */
    private val vertexIndexer = VertexIndexer<Vertex>()

    override val parent: Construct?
        get() {
            if (isRemoved) {
                throw RuntimeException(
                    "Edge no longer belongs to a graph."
                )
            }

            return graph
        }

    /**
     *  Overrides [hypergraph.base.Edge.vertices].
     *
     *  If the edge is newly created and no vertices have been added with
     *  [addVertex], an exception is raised.
     */
    override val vertices: List<Vertex>
        get() {
            if (isRedirected) {
                return edgeRef.get()!!.vertices
            }

            val memberVertices = vertexIndexer.constructs

            return memberVertices
        }

    /**
     *  Adds a vertex without notifying the graph.
     */
    private fun addVertexNoNotify(vertex: Vertex) {
        vertexIndexer.put(vertex.id, vertex)
        vertex.addInvolvedEdge(Friendship, edgeRef)
    }

    override fun addVertex(vertex: hypergraph.base.Vertex) {
        if (isRedirected) {
            return edgeRef.get()!!.addVertex(vertex)
        }

        vertex as Vertex

        addVertexNoNotify(vertex)

        // Notify the graph that a vertex has been added.
        val response = graph.notify(
            Friendship,
            VertexAdded(this, vertex)
        )

        if (response.error != null) {
            throw RuntimeException(response.error.what)
        }
    }

    /**
     *  Removes a vertex without notifying the graph.
     */
    private fun removeVertexNoNotify(vertex: Vertex) {
        vertexIndexer.remove(vertex.id)
        vertex.removeInvolvedEdge(Friendship, edgeRef)
    }

    override fun removeVertex(vertex: hypergraph.base.Vertex) {
        if (isRedirected) {
            return edgeRef.get()!!.removeVertex(vertex)
        }

        if (vertexIndexer.constructs.isEmpty()) {
            return
        }

        vertex as Vertex

        removeVertexNoNotify(vertex)

        // Notify the graph that a vertex has been removed.
        val response = graph.notify(
            Friendship,
            VertexRemoved(this, vertex)
        )

        if (response.error != null) {
            throw RuntimeException(response.error.what)
        }
    }

    override fun getVertexByName(name: String): Vertex? {
        if (isRedirected) {
            return edgeRef.get()!!.getVertexByName(name)
        }

        return vertexIndexer.getByName(name)
    }

    override fun remove() {
        if (isRedirected) {
            return edgeRef.get()!!.remove()
        }

        for (vertex in vertexIndexer.constructs) {
            vertex.removeInvolvedEdge(Friendship, edgeRef)
        }

        graph.removeEdge(Friendship, this)

        isRemoved = true
    }

    /**
     *  @suppress
     *
     *  Merges in `other` without removing `other`.
     *
     *  `other` must be in the same graph as this edge. It is added without
     *  notifying the graph.
     */
    @Suppress("UNUSED_PARAMETER")
    fun mergeInKeep(
        friendAccess: Graph.FriendAccess,
        other: Edge
    ) {
        if (isRedirected) {
            return edgeRef.get()!!.mergeInKeep(friendAccess, other)
        }

        if (other === this) {
            throw IllegalArgumentException(
                "Edges are the same object."
            )
        }

        if (!Equality.graphs(graph, other.graph)) {
            throw IllegalArgumentException(
                "Edges are not in the same graph."
            )
        }

        val thisProxy = proxy as Vertex?
        val otherProxy = other.proxy as Vertex?

        // Merge proxies if needed.
        if (otherProxy != null) {
            when {
                thisProxy != null &&
                !Equality.constructIdentities(thisProxy, otherProxy)
                ->
                    graph.mergeVerticesRedirect(
                        CanBeProxied.Friendship,
                        thisProxy,
                        otherProxy
                    )

                thisProxy == null -> {
                    other.proxy = null
                    proxy = otherProxy
                }
            }
        }

        // Add vertices from the other vertex without notifying the graph.
        for (otherVertex in other.vertices) {
            if (
                !vertexIndexer.constructs.any {
                    Equality.constructIdentities(it, otherVertex)
                }
            ) {
                addVertexNoNotify(otherVertex)
            }
        }
    }

    /**
     *  @suppress
     *
     *  Notifies this edge of a specific event indicated by `message` from the
     *  graph.
     *
     *  The recognized message types are
     *      - [hypergraph.notify.VertexRedirected]
     */
    @Suppress("UNUSED_PARAMETER")
    fun notify(
        friendAccess: Graph.FriendAccess,
        message: Message<Graph>
    ): Response {
        if (isRedirected) {
            return edgeRef.get()!!.notify(friendAccess, message)
        }

        return when (message) {
            is VertexRedirected -> {
                vertexIndexer.reindex(message.fromId)
                vertexIndexer.reindex(message.toId)

                Response(BasicSignal.ACKNOWLEDGED)
            }

            else ->
                Response(
                    BasicSignal.DENIED,
                    Error("Unrecognized message type.")
                )
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
