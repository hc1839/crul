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

package hypergraph.indexer

import hypergraph.base.*
import hypergraph.compare.VertexSetElement

/**
 *  Generic indexer of graph constructs.
 *
 *  Indexing is done solely by the construct IDs. Hashing and equality are not
 *  done, since that can cause circular dependency with a graph.
 *
 *  @constructor
 */
open class ConstructIndexer<T : Construct>() {
    /**
     *  Constructs associated by their IDs.
     */
    private val constructsById: MutableMap<String, T> = HashMap()

    /**
     *  IDs of the constructs that are in this indexer.
     */
    val constructIds: List<String>
        get() = constructsById.keys.toList()

    /**
     *  Constructs that are in this indexer.
     */
    val constructs: List<T>
        get() = constructsById.values.toList()

    /**
     *  Puts a construct to this indexer.
     */
    open fun put(constructId: String, construct: T) {
        constructsById.put(constructId, construct)
    }

    /**
     *  Removes a construct from this indexer.
     */
    open fun remove(constructId: String) {
        constructsById.remove(constructId)
    }

    /**
     *  Removes all constructs from this indexer.
     */
    open fun clear() {
        constructsById.clear()
    }

    /**
     *  Reindexes a construct.
     *
     *  This is usually done after a merge.
     */
    fun reindex(constructId: String) {
        val construct = get(constructId)

        if (construct != null) {
            remove(constructId)
            put(constructId, construct)
        }
    }

    /**
     *  Whether a construct is in this indexer.
     */
    fun contains(constructId: String): Boolean =
        constructsById.containsKey(constructId)

    /**
     *  Gets a construct from this indexer solely by its ID.
     *
     *  `null` if no such construct with the given ID exists in this indexer.
     */
    operator fun get(constructId: String): T? =
        constructsById[constructId]
}

/**
 *  Vertex indexer.
 */
class VertexIndexer<T : Vertex>() : ConstructIndexer<T>() {
    /**
     *  Vertex IDs associated by their names.
     */
    private val vertexIdsByName: MutableMap<String, String> = HashMap()

    /**
     *  Gets a vertex by its name.
     *
     *  `null` if no such with the given name exists in this indexer.
     */
    fun getByName(name: String): T? {
        val vertexId = vertexIdsByName[name]

        return if (vertexId == null) {
            null
        } else {
            get(vertexId)
        }
    }

    /**
     *  Puts a vertex to this indexer.
     */
    override fun put(constructId: String, construct: T) {
        super.put(constructId, construct)

        for (vertexName in construct.names) {
            vertexIdsByName.put(vertexName, constructId)
        }
    }

    /**
     *  Removes a vertex from this indexer.
     */
    override fun remove(constructId: String) {
        for ((vertexName, vertexId) in vertexIdsByName.entries.toList()) {
            if (vertexId == constructId) {
                vertexIdsByName.remove(vertexName)
            }
        }

        super.remove(constructId)
    }

    /**
     *  Removes all vertices from this indexer.
     */
    override fun clear() {
        vertexIdsByName.clear()
        super.clear()
    }
}

/**
 *  Edge indexer.
 */
class EdgeIndexer<T : Edge>() : ConstructIndexer<T>()

/**
 *  Property indexer.
 */
class PropertyIndexer<T : Property>() : ConstructIndexer<T>() {
    /**
     *  Property IDs associated by their types.
     */
    private val propertyIdsByType: MutableMap<VertexSetElement, String> =
        HashMap()

    /**
     *  Gets a property by its type.
     *
     *  `null` if no such property with the given type exists in this indexer.
     */
    fun getByType(type: Vertex): T? {
        val propertyId = propertyIdsByType[VertexSetElement(type)]

        return if (propertyId == null) {
            null
        } else {
            get(propertyId)
        }
    }

    /**
     *  Puts a property to this indexer.
     */
    override fun put(constructId: String, construct: T) {
        super.put(constructId, construct)

        propertyIdsByType.put(
            VertexSetElement(construct.type),
            constructId
        )
    }

    /**
     *  Removes a property from this indexer.
     */
    override fun remove(constructId: String) {
        for ((typeAdapter, propertyId) in propertyIdsByType.entries.toList()) {
            if (propertyId == constructId) {
                propertyIdsByType.remove(typeAdapter)
            }
        }

        super.remove(constructId)
    }

    /**
     *  Removes all properties from this indexer.
     */
    override fun clear() {
        propertyIdsByType.clear()
        super.clear()
    }
}
