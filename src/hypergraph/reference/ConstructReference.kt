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

package hypergraph.reference

import hypergraph.base.*
import hypergraph.compare.ConstructSetElement
import hypergraph.compare.Equality

/**
 *  Dynamic referencing of a graph construct.
 *
 *  Instance of this class stores the given arguments but can retrieve the
 *  terminal construct if needed.
 *
 *  @property graphSystem
 *      Graph system of the construct that is being referred.
 *
 *  @property graphId
 *      ID of the graph of the construct that is being referred.
 *
 *  @property origConstructId
 *      ID of the construct that is being referred. The instance of this class
 *      always stores this given ID even when the construct becomes redirected.
 *
 *  @constructor
 */
class ConstructReference<out T : Construct>(
    val graphSystem: GraphSystem,
    val graphId: String,
    val origConstructId: String
) {
    /**
     *  Gets the terminal construct.
     */
    @Suppress("UNCHECKED_CAST")
    fun get(): T? =
        graphSystem.getGraph(graphId)?.getConstructById(origConstructId) as? T

    /**
     *  Constructs a new instance that directly refers to the terminal
     *  construct.
     */
    val redirected: ConstructReference<T>
        get() = ConstructReference<T>(graphSystem, graphId, get()!!.id)

    override fun hashCode(): Int =
        listOf(
            graphSystem.hashCode(),
            graphId,
            origConstructId
        )
        .hashCode()

    override fun equals(other: Any?): Boolean =
        other is ConstructReference<*> &&
        this::class == other::class &&
        (
            graphSystem == other.graphSystem &&
            graphId == other.graphId &&
            origConstructId == other.origConstructId
        )
}
