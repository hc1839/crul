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

package crul.hypergraph.indexer

import crul.hypergraph.base.Construct
import crul.hypergraph.compare.ConstructSetElement
import crul.hypergraph.reference.ConstructReference

/**
 *  Indexer for constructs involving in other constructs.
 *
 *  @constructor
 */
open class InvolvementIndexer<T: Construct>() {
    /**
     *  Construct references that are in this indexer.
     */
    private val _constructReferences: MutableSet<ConstructReference<T>> =
        HashSet()

    /**
     *  Construct references that are in this indexer.
     */
    val constructReferences: Set<ConstructReference<T>>
        get() = _constructReferences.toSet()

    /**
     *  Constructs that are referred by the references in this indexer.
     */
    val constructs: Set<T>
        get() =
            _constructReferences.map { constructRef ->
                ConstructSetElement<T>(constructRef.get()!!)
            }.toSet().map {
                it.construct
            }.toSet()

    /**
     *  Adds a construct reference to this indexer.
     *
     *  Adding does not automatically cause reindexing.
     */
    open fun add(constructRef: ConstructReference<T>) {
        _constructReferences.add(constructRef)
    }

    /**
     *  Removes a construct from this indexer.
     *
     *  Removal automatically causes reindexing.
     */
    open fun remove(constructRef: ConstructReference<T>) {
        reindex()
        _constructReferences.remove(constructRef.redirected)
    }

    /**
     *  Reindex the construct references.
     */
    open fun reindex() {
        val constructRefsBuf = _constructReferences.map { it.redirected }

        _constructReferences.clear()
        _constructReferences.addAll(constructRefsBuf)
    }
}
