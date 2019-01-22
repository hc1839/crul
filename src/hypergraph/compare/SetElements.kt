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

package hypergraph.compare

import hypergraph.base.*

/**
 *  Wrapper for a construct as an element of a set such that constructs are
 *  compared by value.
 */
open class ConstructSetElement<out T : Construct>(val construct: T) {
    override fun hashCode(): Int =
        Hashing.constructIdentity(construct)

    override fun equals(other: Any?): Boolean =
        other is ConstructSetElement<*> &&
        this::class == other::class &&
        (
            Equality.constructIdentities(construct, other.construct)
        )
}

/**
 *  Wrapper for a vertex as an element of a set.
 */
class VertexSetElement(vertex: Vertex) :
    ConstructSetElement<Vertex>(vertex)
{
    override fun hashCode(): Int =
        Hashing.vertex(construct)

    override fun equals(other: Any?): Boolean =
        super.equals(other) &&
        other is VertexSetElement &&
        (
            Equality.vertices(construct, other.construct)
        )
}

/**
 *  Wrapper for an edge as an element of a set.
 */
class EdgeSetElement(edge: Edge) :
    ConstructSetElement<Edge>(edge)
{
    override fun hashCode(): Int =
        Hashing.edge(construct)

    override fun equals(other: Any?): Boolean =
        super.equals(other) &&
        other is EdgeSetElement &&
        (
            Equality.edges(construct, other.construct)
        )
}

/**
 *  Wrapper for a property as an element of a set.
 */
class PropertySetElement(property: Property) :
    ConstructSetElement<Property>(property)
{
    override fun hashCode(): Int =
        Hashing.property(construct)


    override fun equals(other: Any?): Boolean =
        super.equals(other) &&
        other is PropertySetElement &&
        (
            Equality.properties(construct, other.construct)
        )
}
