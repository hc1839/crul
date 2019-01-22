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
import hypergraph.reference.ConstructReference

/**
 *  Vertex.
 */
class Vertex : hypergraph.base.Vertex {
    /**
     *  Reference to this vertex.
     *
     *  It is for redirection purposes.
     */
    private val vertexRef: ConstructReference<Vertex>

    /**
     *  Reference to the parent graph.
     *
     *  It acts as a weak reference in order to avoid circular referencing.
     */
    private val graphRef: ConstructReference<Graph>

    /**
     *  ID that was used to construct this vertex.
     */
    private val origId: String

    /**
     *  @suppress
     *
     *  @param id
     *      ID of the new vertex. It must be unique within `parentGraph` and
     *      must conform to XML NCName production.
     *
     *  @param parentGraph
     *      [Graph] within which to create the vertex.
     */
    constructor(
        @Suppress("UNUSED_PARAMETER")
        friendAccess: Graph.FriendAccess,
        id: String,
        parentGraph: Graph
    ) {
        vertexRef = ConstructReference<Vertex>(
            parentGraph.graphSystem,
            parentGraph.id,
            id
        )

        graphRef = ConstructReference<Graph>(
            parentGraph.graphSystem,
            parentGraph.id,
            parentGraph.id
        )

        this.origId = id
    }

    /**
     *  Whether this vertex has been removed from the graph.
     */
    private var isRemoved: Boolean = false

    /**
     *  Whether this vertex is being redirected.
     */
    private var isRedirected = false

    /**
     *  @suppress
     *
     *  @param value
     *      `true` if this vertex is not a terminal vertex and is being
     *      redirected.
     */
    @Suppress("UNUSED_PARAMETER")
    fun setIsRedirected(
        friendAccess: Graph.FriendAccess,
        value: Boolean
    ) {
        isRedirected = value
    }

    /**
     *  Parent graph of this vertex.
     */
    override val graph: Graph
        get() {
            if (isRemoved) {
                throw RuntimeException(
                    "Vertex no longer belongs to a graph."
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

    /**
     *  ID of the terminal vertex.
     *
     *  If this vertex is not a terminal vertex, the ID returned will be
     *  different from what was passed during construction.
     */
    override val id: String
        get() {
            if (isRedirected) {
                return vertexRef.get()!!.id
            }

            return origId
        }

    /**
     *  Parent graph of this vertex.
     */
    override val parent: Construct?
        get() {
            if (isRemoved) {
                throw RuntimeException(
                    "Vertex no longer belongs to a graph."
                )
            }

            return graph
        }

    /**
     *  Indexer for edges that this vertex is participating in.
     */
    private val involvedEdgeIndexer = InvolvementIndexer<Edge>()

    /**
     *  @suppress
     *
     *  @param edgeRef
     *      Add the edge that this vertex is participating in to the indexer.
     */
    @Suppress("UNUSED_PARAMETER")
    fun addInvolvedEdge(
        friendAccess: Edge.FriendAccess,
        edgeRef: ConstructReference<Edge>
    ) {
        if (isRedirected) {
            return vertexRef.get()!!.addInvolvedEdge(
                friendAccess,
                edgeRef
            )
        }

        involvedEdgeIndexer.add(edgeRef)
    }

    /**
     *  @suppress
     *
     *  @param edgeRef
     *      Remove the edge that this vertex is no longer participating in from
     *      the indexer.
     */
    @Suppress("UNUSED_PARAMETER")
    fun removeInvolvedEdge(
        friendAccess: Edge.FriendAccess,
        edgeRef: ConstructReference<Edge>
    ) {
        if (isRedirected) {
            return vertexRef.get()!!.removeInvolvedEdge(
                friendAccess,
                edgeRef
            )
        }

        involvedEdgeIndexer.remove(edgeRef)
    }

    override var proxied: hypergraph.base.CanBeProxied? = null
        get() {
            if (isRedirected) {
                return vertexRef.get()!!.proxied
            }

            return field
        }

        // `value` cannot be already proxied by another vertex and must belong
        // to this graph.
        private set(value) {
            if (isRedirected) {
                vertexRef.get()!!.proxied = value
                return
            }

            if (value != null) {
                if (value.proxy != null) {
                    throw IllegalArgumentException(
                        "Construct is already being proxied."
                    )
                }

                if (value.graph !== graph) {
                    throw IllegalArgumentException(
                        "Construct to be proxied does not belong to this graph."
                    )
                }
            }

            field = value
        }

    /**
     *  @suppress
     *
     *  @param canBeProxied
     *      Construct that is proxied by this vertex.
     */
    @Suppress("UNUSED_PARAMETER")
    fun setProxied(
        friendAccess: CanBeProxied.FriendAccess,
        canBeProxied: CanBeProxied?
    ) {
        if (isRedirected) {
            vertexRef.get()!!.proxied = canBeProxied
            return
        }

        proxied = canBeProxied
    }

    /**
     *  Arbitrary object to attach to this vertex.
     */
    var userData: Any? = null
        get() {
            if (isRedirected) {
                return vertexRef.get()!!.userData
            }

            return field
        }
        set(newValue) {
            if (isRedirected) {
                vertexRef.get()!!.userData = newValue
                return
            }

            val oldValue = field

            if (oldValue == null) {
                if (newValue != null) {
                    field = newValue
                }
            } else {
                field = null
                userData = newValue
            }
        }

    /**
     *  Backing property for vertex names.
     */
    private val _names: MutableList<String> = arrayListOf()

    override val names: List<String>
        get() {
            if (isRedirected) {
                return vertexRef.get()!!.names
            }

            return _names.toList()
        }

    /**
     *  Adds a name without causing notifications to the graph.
     *
     *  `name` must conform to XML NCName production.
     */
    private fun addNameNoNotify(name: String) {
        if (_names.contains(name)) {
            return
        }

        if (!xml.Datatype.isNCName(name)) {
            throw IllegalArgumentException(
                "Name does not conform to XML NCName production: $name"
            )
        }

        _names.add(name)
    }

    override fun addName(name: String) {
        if (isRedirected) {
            return vertexRef.get()!!.addName(name)
        }

        addNameNoNotify(name)

        // Notify the graph that a name has been added.
        val response = graph.notify(
            Friendship,
            VertexNameAdded(this, name)
        )

        if (response.error != null) {
            throw RuntimeException(response.error.what)
        }
    }

    /**
     *  Removes a name without causing notifications to the graph.
     */
    private fun removeNameNoNotify(name: String) {
        if (!_names.contains(name)) {
            return
        }

        _names.remove(name)
    }

    override fun removeName(name: String) {
        if (isRedirected) {
            return vertexRef.get()!!.removeName(name)
        }

        removeNameNoNotify(name)

        // Notify the graph that a name has been removed.
        val response = graph.notify(
            Friendship,
            VertexNameRemoved(this, name)
        )

        if (response.error != null) {
            throw RuntimeException(response.error.what)
        }
    }

    override val edges: List<Edge>
        get() {
            if (isRedirected) {
                return vertexRef.get()!!.edges
            }

            return involvedEdgeIndexer.constructs.toList()
        }

    override fun getEdgesByType(type: hypergraph.base.Vertex): List<Edge> {
        if (isRedirected) {
            return vertexRef.get()!!.getEdgesByType(type)
        }

        return edges.filter {
            Equality.constructIdentities(it.type, type)
        }
    }

    /**
     *  Indexer for the types of this vertex.
     */
    private val vertexTypeIndexer = VertexIndexer<Vertex>()

    /**
     *  Indexer for the constructs that this vertex is participating as a type.
     */
    private val involvedInTypeIndexer = InvolvementIndexer<Construct>()

    override val types: List<Vertex>
        get() {
            if (isRedirected) {
                return vertexRef.get()!!.types
            }

            return vertexTypeIndexer.constructs
        }

    /**
     *  @suppress
     *
     *  Constructs that this vertex is participating as a type.
     */
    @Suppress("UNUSED_PARAMETER")
    fun getInvolvedInTypes(
        friendAccess: Graph.FriendAccess
    ): List<Construct> {
        if (isRedirected) {
            return vertexRef.get()!!.getInvolvedInTypes(friendAccess)
        }

        return involvedInTypeIndexer.constructs.toList()
    }

    override fun addType(type: hypergraph.base.Vertex) {
        if (isRedirected) {
            return vertexRef.get()!!.addType(type)
        }

        type as Vertex

        if (type.graph.graphSystem !== graph.graphSystem) {
            throw IllegalArgumentException(
                "Vertex does not belong to the same graph system."
            )
        }

        if (
            Hashing.vertex(this) == Hashing.vertex(type) &&
            Equality.vertices(this, type)
        ) {
            throw IllegalArgumentException(
                "Cannot use the same vertex as its type."
            )
        }

        if (vertexTypeIndexer.contains(type.id)) {
            return
        }

        vertexTypeIndexer.put(type.id, type)
        type.involvedInTypeIndexer.add(vertexRef)
    }

    override fun removeType(type: hypergraph.base.Vertex) {
        if (isRedirected) {
            return vertexRef.get()!!.removeType(type)
        }

        type as Vertex

        type.involvedInTypeIndexer.remove(vertexRef)
        vertexTypeIndexer.remove(type.id)
    }

    /**
     *  Whether this vertex has `type` as one of its types.
     */
    fun isInstanceOf(type: Vertex): Boolean {
        if (isRedirected) {
            return isInstanceOf(type)
        }

        return vertexTypeIndexer.constructs.any {
            Equality.constructIdentities(it, type)
        }
    }

    /**
     *  @suppress
     *
     *  Adds a property that this vertex is participating as the type to this
     *  vertex's indexer.
     */
    @Suppress("UNUSED_PARAMETER")
    fun addInvolvedInType(
        friendAccess: Property.FriendAccess,
        propertyRef: ConstructReference<Property>
    ) {
        if (isRedirected) {
            return vertexRef.get()!!.addInvolvedInType(
                friendAccess,
                propertyRef
            )
        }

        involvedInTypeIndexer.add(propertyRef)
    }

    /**
     *  @suppress
     *
     *  Removes a property that this vertex was participating as the type from
     *  this vertex's indexer.
     */
    @Suppress("UNUSED_PARAMETER")
    fun removeInvolvedInType(
        friendAccess: Property.FriendAccess,
        propertyRef: ConstructReference<Property>
    ) {
        if (isRedirected) {
            return vertexRef.get()!!.removeInvolvedInType(
                friendAccess,
                propertyRef
            )
        }

        involvedInTypeIndexer.remove(propertyRef)
    }

    /**
     *  @suppress
     *
     *  Adds an edge that this vertex is participating as the type to this
     *  vertex's indexer.
     */
    @Suppress("UNUSED_PARAMETER")
    fun addInvolvedInType(
        friendAccess: Edge.FriendAccess,
        edgeRef: ConstructReference<Edge>
    ) {
        if (isRedirected) {
            return vertexRef.get()!!.addInvolvedInType(
                friendAccess,
                edgeRef
            )
        }

        involvedInTypeIndexer.add(edgeRef)
    }

    /**
     *  @suppress
     *
     *  Removes an edge that this vertex was participating as the type from
     *  this vertex's indexer.
     */
    @Suppress("UNUSED_PARAMETER")
    fun removeInvolvedInType(
        friendAccess: Edge.FriendAccess,
        edgeRef: ConstructReference<Edge>
    ) {
        if (isRedirected) {
            return vertexRef.get()!!.removeInvolvedInType(
                friendAccess,
                edgeRef
            )
        }

        involvedInTypeIndexer.remove(edgeRef)
    }

    /**
     *  Indexer of properties that this vertex has.
     */
    private val propertyIndexer = PropertyIndexer<Property>()

    override val properties: List<Property>
        get() {
            if (isRedirected) {
                return vertexRef.get()!!.properties
            }

            return propertyIndexer.constructs
        }

    /**
     *  Creates a property with a given type and value.
     *
     *  If `id` is `null`, a UUID that is guaranteed to be unique within the
     *  parent graph of this vertex is generated. Otherwise, the given ID is
     *  used and an exception is raised if it is not unique within the graph.
     */
    private fun createProperty(
        id: String?,
        type: Vertex,
        value: String
    ): Property {
        if (id == null) {
            var newId: String

            // Guarantee the uniqueness of the generated UUID within the graph.
            do {
                newId = uuid.Generator.inNCName()
            } while (graph.getConstructById(newId) != null)

            return createProperty(newId, type, value)
        }

        val property = Property(Friendship, id, type, value, this)

        propertyIndexer.put(property.id, property)

        // Notify the graph that a property has been added.
        val response = graph.notify(
            Friendship,
            PropertyAdded(this, property)
        )

        if (response.error != null) {
            throw RuntimeException(response.error.what)
        }

        return property
    }

    override fun createProperty(
        type: hypergraph.base.Vertex,
        value: String
    ): Property {
        if (isRedirected) {
            return vertexRef.get()!!.createProperty(type, value)
        }

        return createProperty(null, type as Vertex, value)
    }

    /**
     *  @suppress
     *
     *  Removes a property from this vertex.
     *
     *  An exception is raised if `property` does not belong to this vertex.
     */
    @Suppress("UNUSED_PARAMETER")
    fun removeProperty(
        friendAccess: Property.FriendAccess,
        property: Property
    ) {
        if (isRedirected) {
            return vertexRef.get()!!.removeProperty(
                friendAccess,
                property
            )
        }

        if (!propertyIndexer.contains(property.id)) {
            throw IllegalArgumentException(
                "Property does not belong to this vertex."
            )
        }

        val propertyId = property.id

        propertyIndexer.remove(propertyId)

        // Notify the graph that a property has been removed.
        val response = graph.notify(
            Friendship,
            PropertyRemoved(this, propertyId)
        )

        if (response.error != null) {
            throw RuntimeException(response.error.what)
        }
    }

    override fun getPropertiesByType(
        type: hypergraph.base.Vertex
    ): List<Property> {
        if (isRedirected) {
            return vertexRef.get()!!.getPropertiesByType(type)
        }

        val typeHashCode = Hashing.vertex(type)

        return propertyIndexer.constructs.filter {
            Hashing.vertex(it.type) == typeHashCode &&
            Equality.vertices(it.type, type)
        }
    }

    /**
     *  Vertex cannot be acting as a proxy, participating in at least one edge,
     *  or participating as the type for a construct.
     */
    override fun remove() {
        if (isRedirected) {
            return vertexRef.get()!!.remove()
        }

        if (proxied != null) {
            throw RuntimeException(
                "Vertex is acting as a proxy."
            )
        }

        if (involvedEdgeIndexer.constructReferences.count() != 0) {
            throw RuntimeException(
                "Vertex is involved in at least one edge."
            )
        }

        if (involvedInTypeIndexer.constructReferences.count() != 0) {
            throw RuntimeException(
                "Vertex is involved in at least one type."
            )
        }

        userData = null

        graph.removeVertex(Friendship, this)

        isRemoved = true
    }

    /**
     *  @suppress
     *
     *  Notifies this vertex of a specific event indicated by `message` from a
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
            return vertexRef.get()!!.notify(friendAccess, message)
        }

        return when (message) {
            is VertexRedirected -> {
                vertexTypeIndexer.remove(message.fromId)
                vertexTypeIndexer.reindex(message.toId)

                Response(BasicSignal.ACKNOWLEDGED)
            }

            else ->
                Response(
                    BasicSignal.DENIED,
                    Error("Unrecognized message type.")
                )
        }
    }

    /**
     *  @suppress
     *
     *  Notifies this vertex of a specific event indicated by `message` from a
     *  property.
     *
     *  The recognized message types are
     *      - [hypergraph.notify.PropertyValueModified]
     */
    @Suppress("UNUSED_PARAMETER")
    fun notify(
        friendAccess: Property.FriendAccess,
        message: Message<Property>
    ): Response {
        if (isRedirected) {
            return vertexRef.get()!!.notify(friendAccess, message)
        }

        return when (message) {
            is PropertyValueModified -> {
                val sourceProperty = message.source

                val sourcePropertyHashCode =
                    Hashing.property(sourceProperty)

                // Find and merge equivalent properties.
                for (property in propertyIndexer.constructs) {
                    if (
                        property !== sourceProperty &&
                        Hashing.property(property) == sourcePropertyHashCode &&
                        Equality.properties(property, sourceProperty)
                    ) {
                        val otherPropertyId = property.id

                        sourceProperty.mergeInKeep(Friendship, property)
                        propertyIndexer.remove(otherPropertyId)
                        propertyIndexer.reindex(sourceProperty.id)

                        // Notify the graph that two properties have merged.
                        val response = graph.notify(
                            Friendship,
                            PropertiesMerged(
                                this,
                                sourceProperty.id,
                                otherPropertyId
                            )
                        )

                        if (response.error != null) {
                            throw RuntimeException(response.error.what)
                        }
                    }
                }

                Response(BasicSignal.ACKNOWLEDGED)
            }

            else ->
                Response(
                    BasicSignal.DENIED,
                    Error("Unrecognized message type.")
                )
        }
    }

    /**
     *  @suppress
     *
     *  Merges in `other` without removing `other`.
     *
     *  `other` must be in the same graph as this vertex. If both vertices are
     *  proxying other constructs, the proxied constructs must be equivalent.
     *
     *  If both vertices have a non-`null` [userData], [userData] must be the
     *  same object.
     */
    @Suppress("UNUSED_PARAMETER")
    fun mergeInKeep(
        friendAccess: Graph.FriendAccess,
        other: Vertex
    ) {
        if (isRedirected) {
            return vertexRef.get()!!.mergeInKeep(friendAccess, other)
        }

        if (other === this) {
            throw IllegalArgumentException(
                "Vertices are the same object."
            )
        }

        if (!Equality.graphs(graph, other.graph)) {
            throw IllegalArgumentException(
                "Vertices are not in the same graph."
            )
        }

        val thisProxied = proxied
        val otherProxied = other.proxied

        // Check whether the proxied constructs, if any, are equivalent.
        if (
            thisProxied != null &&
            otherProxied != null &&
            !Equality.constructIdentities(thisProxied, otherProxied)
        ) {
            throw IllegalArgumentException(
                "Proxied constructs are different in the merging vertices."
            )
        }

        val otherUserData = other.userData

        if (otherUserData != null) {
            other.userData = null
        }

        // Merge in vertex names.
        for (otherName in other.names) {
            addNameNoNotify(otherName)
        }

        // Merge in vertex types.
        for (otherType in other.types) {
            addType(otherType)
        }

        // Add properties of the other vertex to the indexer.
        for (otherProperty in other.properties) {
            propertyIndexer.put(otherProperty.id, otherProperty)
        }

        // Add the edges that the other vertex is participating in to the
        // indexer.
        for (otherEdge in other.edges) {
            involvedEdgeIndexer.add(
                ConstructReference<Edge>(
                    graph.graphSystem,
                    otherEdge.graph.id,
                    otherEdge.id
                )
            )
        }

        // Add the types that the other vertex was participating as to the
        // indexer.
        for (
            otherInvolvedInTypeRef in
            other.involvedInTypeIndexer.constructReferences
        ) {
            involvedInTypeIndexer.add(otherInvolvedInTypeRef)
        }

        if (otherProxied != null && thisProxied == null) {
            proxied = otherProxied
        }

        if (userData == null && otherUserData != null) {
            userData = otherUserData
        } else if (userData != null && otherUserData != null) {
            if (userData !== otherUserData) {
                throw RuntimeException(
                    "Merging vertices have different objects attached to them."
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
