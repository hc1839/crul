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

package crul.hypergraph

import crul.hypergraph.compare.Equality
import crul.hypergraph.notify.*
import crul.hypergraph.reference.ConstructReference

/**
 *  Property.
 */
class Property :
    CanBeProxied,
    crul.hypergraph.base.Property
{
    private val propertyRef: ConstructReference<Property>

    private val parentRef: ConstructReference<Vertex>

    private val graphRef: ConstructReference<Graph>

    override var type: crul.hypergraph.base.Vertex
        get() {
            if (isRedirected) {
                return propertyRef.get()!!.type
            }

            return field
        }
        set(value) {
            if (isRedirected) {
                propertyRef.get()!!.type = value
                return
            }

            val oldType = field as? Vertex
            val newType = value as Vertex

            oldType?.removeInvolvedInType(Friendship, propertyRef)
            newType.addInvolvedInType(Friendship, propertyRef)

            field = newType
        }

    override var value: String
        get() {
            if (isRedirected) {
                return propertyRef.get()!!.value
            }

            return field
        }
        set(newValue) {
            if (isRedirected) {
                propertyRef.get()!!.value = newValue
                return
            }

            val oldValue = field as? String

            field = newValue

            if (oldValue != null) {
                // Notify the graph that the value has been modified.
                val response = parent.notify(
                    Friendship,
                    PropertyValueModified(this, oldValue, newValue)
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
     *  Constructs a property of a given type and value.
     *
     *  @param id
     *      ID of the property to use. It must be unique within the graph and
     *      must conform to XML NCName production.
     *
     *  @param parentVertex
     *      Vertex that the new property belongs to.
     */
    constructor(
        @Suppress("UNUSED_PARAMETER")
        friendAccess: Vertex.FriendAccess,
        id: String,
        type: Vertex,
        value: String,
        parentVertex: Vertex
    ) {
        if (!crul.xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "ID does not conform to XML NCName production: $id"
            )
        }

        propertyRef = ConstructReference<Property>(
            parentVertex.graph.graphSystem,
            parentVertex.graph.id,
            id
        )

        parentRef = ConstructReference<Vertex>(
            parentVertex.graph.graphSystem,
            parentVertex.graph.id,
            parentVertex.id
        )

        graphRef = ConstructReference<Graph>(
            parentVertex.graph.graphSystem,
            parentVertex.graph.id,
            parentVertex.graph.id
        )

        this.origId = id
        this.type = type
        this.value = value

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

        type.addInvolvedInType(Friendship, propertyRef)
    }

    /**
     *  Whether this property no longer belongs to a vertex.
     */
    private var isRemoved = false

    /**
     *  @suppress
     *
     *  Sets whether this property is redirected to a terminal property.
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
                    "Property no longer belongs to a graph."
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
        propertyRef.get()!!

    override val id: String
        get() {
            if (isRedirected) {
                return propertyRef.get()!!.id
            }

            return origId
        }

    override val parent: Vertex
        get() {
            if (isRedirected) {
                return propertyRef.get()!!.parent
            }

            if (isRemoved) {
                throw RuntimeException(
                    "Property no longer belongs to a graph."
                )
            }

            val referent = parentRef.get()

            if (referent == null) {
                throw RuntimeException(
                    "Vertex no longer exists."
                )
            }

            return referent
        }

    override fun remove() {
        if (isRedirected) {
            return propertyRef.get()!!.remove()
        }

        (type as Vertex).removeInvolvedInType(Friendship, propertyRef)
        parent.removeProperty(Friendship, this)

        if (proxy != null) {
            proxy = null
        }

        isRemoved = true
    }

    override fun doubleValue() =
        java.lang.Double.parseDouble(value)

    override fun intValue() =
        java.lang.Integer.parseInt(value)

    override fun longValue() =
        java.lang.Long.parseLong(value)

    override fun setValue(newValue: Double) {
        value = newValue.toString()
    }

    override fun setValue(newValue: Int) {
        value = newValue.toString()
    }

    override fun setValue(newValue: Long) {
        value = newValue.toString()
    }

    /**
     *  @suppress
     *
     *  Merges in `other` without removing `other`.
     *
     *  `other` must be in the same graph as this property.
     */
    @Suppress("UNUSED_PARAMETER")
    fun mergeInKeep(
        friendAccess: Vertex.FriendAccess,
        other: Property
    ) {
        if (isRedirected) {
            return propertyRef.get()!!.mergeInKeep(friendAccess, other)
        }

        if (other === this) {
            throw IllegalArgumentException(
                "Properties are the same object."
            )
        }

        // Properties must be in the same graph.
        if (!Equality.graphs(graph, other.graph)) {
            throw IllegalArgumentException(
                "Properties are not in the same graph."
            )
        }

        val thisProxy = proxy as Vertex?
        val otherProxy = other.proxy as Vertex?

        // Determine whether the proxies, if any, need to be merged.
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

                thisProxy == null ->
                    proxy = otherProxy
            }
        }
    }

    private object FriendKey : crul.visaccess.FriendKey()

    open class FriendAccess(key: crul.visaccess.FriendKey) :
        crul.visaccess.FriendAccess
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
