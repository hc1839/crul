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

/**
 *  System of graphs.
 */
class GraphSystem() : hypergraph.base.GraphSystem {
    /**
     *  Graphs where each is associated with its ID.
     */
    private val graphsById = HashMap<String, Graph>()

    override val graphIds: List<String>
        get() = graphsById.keys.toList()

    override fun createGraph(id: String): Graph {
        if (graphsById.containsKey(id)) {
            throw IllegalArgumentException(
                "Graph ID exists: $id"
            )
        }

        val graph = Graph(Friendship, id, this)
        graphsById.put(id, graph)

        return graph
    }

    override fun getGraph(id: String) =
        graphsById[id]

    /**
     *  @suppress
     *
     *  Removes a graph from the system.
     */
    @Suppress("UNUSED_PARAMETER")
    fun removeGraph(friendAccess: Graph.FriendAccess, graph: Graph) {
        graphsById.remove(graph.id)
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
