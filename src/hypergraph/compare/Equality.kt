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

@file:JvmName("Equality")
@file:JvmMultifileClass

package hypergraph.compare

import hypergraph.base.*

/**
 *  Equality of graph constructs.
 */
object Equality {
    /**
     *  Equality of two graphs that are compared by their graph systems and
     *  graph IDs.
     */
    @JvmStatic
    fun graphs(lhs: Graph, rhs: Graph): Boolean = (
        lhs.graphSystem == rhs.graphSystem &&
        lhs.id == rhs.id
    )

    /**
     *  Equality of two constructs that are compared by their graphs and
     *  construct IDs.
     */
    @JvmStatic
    fun constructIdentities(lhs: Construct, rhs: Construct): Boolean = (
        Equality.graphs(lhs.graph, rhs.graph) &&
        lhs.id == rhs.id
    )

    /**
     *  Equality of two vertices.
     *
     *  `true` if the graphs are equal, non-empty intersection of vertex names,
     *  and the constructs being proxied (if any) are equal.
     */
    @JvmStatic
    fun vertices(lhs: Vertex, rhs: Vertex): Boolean = (
        Equality.graphs(lhs.graph, rhs.graph) &&
        lhs.names.intersect(rhs.names).count() > 0 &&
        if (lhs.proxied != null && rhs.proxied != null) {
            constructIdentities(lhs.proxied!!, rhs.proxied!!)
        } else {
            lhs.proxied == rhs.proxied
        }
    )

    /**
     *  Equality of two edges.
     *
     *  `true` if the graphs are equal, edge types are equal, and the sets of
     *  vertices that are participating in each edge are equal.
     */
    @JvmStatic
    fun edges(lhs: Edge, rhs: Edge): Boolean = (
        Equality.graphs(lhs.graph, rhs.graph) &&
        Equality.vertices(lhs.type, rhs.type) &&
        lhs.vertices.map { VertexSetElement(it) }.toSet() ==
            rhs.vertices.map { VertexSetElement(it) }.toSet()
    )

    /**
     *  Equality of two properties.
     *
     *
     *  `true` if the graphs are equal, parent vertices of each property are
     *  equal, property type are equal, and property value are equal.
     */
    @JvmStatic
    fun properties(lhs: Property, rhs: Property): Boolean = (
        Equality.graphs(lhs.graph, rhs.graph) &&
        Equality.vertices(
            lhs.parent as Vertex,
            rhs.parent as Vertex
        ) &&
        Equality.vertices(lhs.type, rhs.type) &&
        lhs.value == rhs.value
    )
}
