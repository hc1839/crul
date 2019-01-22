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

package hypergraph.base

/**
 *  Graph construct.
 */
interface Construct {
    /**
     *  ID of this construct that is automatically generated by the
     *  implementation.
     *
     *  Must be unique within a graph.
     */
    val id: String

    /**
     *  Parent of this construct.
     *
     *  `null` if this construct is a [Graph].
     */
    val parent: Construct?

    /**
     *  [Graph] that this construct is contained in.
     *
     *  If this construct is a [Graph], itself is returned.
     */
    val graph: Graph

    /**
     *  Removes this construct from the parent construct.
     *
     *  If this construct is a [Graph], the graph is removed from the
     *  [GraphSystem] that it is contained in.
     */
    fun remove()
}

/**
 *  Non-vertex construct that can be proxied by a vertex.
 */
interface CanBeProxied : Construct {
    /**
     *  Vertex that is acting as a proxy for this construct.
     *
     *  If this construct is not being proxied, `null` is returned.
     */
    var proxy: Vertex?
}

/**
 *  System for storing and managing graphs.
 */
interface GraphSystem {
    /**
     *  Graph IDs that this system is managing.
     */
    val graphIds: List<String>

    /**
     *  Creates a new graph using `id` as its unique ID within this system.
     */
    fun createGraph(id: String): Graph

    /**
     *  Gets a graph using its `id`.
     *
     *  If this system does not contain a graph that has the given ID, `null`
     *  is returned.
     */
    fun getGraph(id: String): Graph?
}

/**
 *  Hypergraph.
 *
 *  Throughout class names and documentation, "graph" is used instead for
 *  simplicity.
 */
interface Graph : CanBeProxied {
    /**
     *  Vertices in this graph.
     */
    val vertices: List<Vertex>

    /**
     *  Edges in this graph.
     */
    val edges: List<Edge>

    /**
     *  [GraphSystem] that this graph is being managed by.
     */
    val graphSystem: GraphSystem

    /**
     *  Gets a construct in this graph using the construct ID.
     *
     *  `null` if no such construct with the given ID exists in this graph.
     */
    fun getConstructById(constructId: String): Construct?

    /**
     *  Gets a vertex using its name.
     *
     *  `null` if no such vertex with the given name exists.
     */
    fun getVertexByName(name: String): Vertex?

    /**
     *  Creates a [Vertex] in this graph with an automatically generated ID.
     */
    fun createVertex(): Vertex

    /**
     *  Creates an [Edge] in this graph with an automatically generated ID.
     */
    fun createEdge(type: Vertex): Edge
}

/**
 *  Vertex.
 */
interface Vertex : Construct {
    /**
     *  Names associated with this vertex.
     *
     *  List of names can be empty.
     */
    val names: List<String>

    /**
     *  Edges that this vertex is participating in.
     */
    val edges: List<Edge>

    /**
     *  Properties associated with this vertex.
     */
    val properties: List<Property>

    /**
     *  Types of the subject that this vertex is representing.
     *
     *  List of types can be empty.
     */
    val types: List<Vertex>

    /**
     *  Non-vertex construct that this vertex is proxying.
     *
     *  `null` if the vertex is not proxying a construct.
     */
    val proxied: CanBeProxied?

    /**
     *  Adds a name to this vertex.
     *
     *  If another vertex with the same name exists, merging will occur.
     */
    fun addName(name: String)

    /**
     *  Removes a name from this vertex.
     *
     *  Does nothing if this vertex does not have such name.
     */
    fun removeName(name: String)

    /**
     *  Adds a type of the subject that this vertex is representing.
     *
     *  Vertex must belong to the same graph system and cannot act as its own
     *  type.
     */
    fun addType(type: Vertex)

    /**
     *  Removes a type of the subject that this vertex is representing.
     */
    fun removeType(type: Vertex)

    /**
     *  Creates a [Property] that is associated with this vertex.
     *
     *  @param type
     *      Type of the association with the property.
     *
     *  @param value
     *      Value of the property.
     */
    fun createProperty(type: Vertex, value: String): Property

    /**
     *  Gets properties that have a given type of association with this vertex.
     */
    fun getPropertiesByType(type: Vertex): List<Property>

    /**
     *  Gets edges that are of a given `type` and that this vertex is
     *  participating in.
     */
    fun getEdgesByType(type: Vertex): List<Edge>
}

/**
 *  Property associated with a vertex.
 */
interface Property : CanBeProxied {
    /**
     *  Type of the association that this property has with its vertex.
     */
    var type: Vertex

    /**
     *  Value of this property.
     */
    var value: String

    /**
     *  Value of this property as a `Double`.
     */
    fun doubleValue(): Double

    /**
     *  Value of this property as an `Int`.
     */
    fun intValue(): Int

    /**
     *  Value of this property as a `Long`.
     */
    fun longValue(): Long

    /**
     *  Sets the value of this property using a `Double`.
     */
    fun setValue(newValue: Double)

    /**
     *  Sets the value of this property using an `Int`.
     */
    fun setValue(newValue: Int)

    /**
     *  Sets the value of this property using a `Long`.
     */
    fun setValue(newValue: Long)
}

/**
 *  Hyperedge.
 *
 *  Throughout class names and documentation, "edge" is used instead for
 *  simplicity.
 */
interface Edge : CanBeProxied {
    /**
     *  Vertices that are participating in this edge.
     */
    val vertices: List<Vertex>

    /**
     *  Type of this edge.
     */
    var type: Vertex

    /**
     *  Adds a vertex to this edge.
     *
     *  If adding a vertex to this edge causes it to be equal to another edge,
     *  merging will occur.
     */
    fun addVertex(vertex: Vertex)

    /**
     *  Removes a vertex from this edge.
     *
     *  If removing a vertex from this edge causes it to be equal to another
     *  edge, merging will occur.
     */
    fun removeVertex(vertex: Vertex)

    /**
     *  Gets a vertex that is participating in this edge by the vertex's name.
     *
     *  `null` if no such vertex with the given name is in this edge.
     */
    fun getVertexByName(name: String): Vertex?
}
