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

@file:JvmName("Hashing")
@file:JvmMultifileClass

package hypergraph.compare

import hypergraph.base.*

/**
 *  Hashing of graph constructs.
 */
object Hashing {
    /**
     *  Hash of a graph.
     */
    @JvmStatic
    fun graph(graph: Graph): Int = listOf(
        graph.graphSystem.hashCode(),
        graph.id
    ).hashCode()

    /**
     *  Hash of a graph and the construct ID.
     */
    @JvmStatic
    fun constructIdentity(construct: Construct): Int = listOf(
        Hashing.graph(construct.graph),
        construct.id
    ).hashCode()

    /**
     *  Hash of a vertex.
     */
    @JvmStatic
    fun vertex(vertex: Vertex): Int = listOf(
        Hashing.graph(vertex.graph),
        if (vertex.proxied == null) {
            0
        } else {
            Hashing.constructIdentity(vertex.proxied!!)
        }
    ).hashCode()

    /**
     *  Hash of an edge.
     */
    @JvmStatic
    fun edge(edge: Edge): Int = listOf(
        Hashing.graph(edge.graph),
        Hashing.vertex(edge.type),
        edge.vertices.map { Hashing.vertex(it) }.sorted()
    ).hashCode()

    /**
     *  Hash of a property.
     */
    @JvmStatic
    fun property(property: Property): Int = listOf(
        Hashing.graph(property.graph),
        Hashing.vertex(property.type),
        Hashing.vertex(property.parent as Vertex),
        property.value.hashCode()
    ).hashCode()
}
