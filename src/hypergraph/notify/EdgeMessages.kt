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

package crul.hypergraph.notify

import crul.hypergraph.base.*

/**
 *  Message from an edge indicating that a vertex has been added.
 */
class VertexAdded<out E : Edge, out V : Vertex>(
    source: E,
    val addedVertex: V
) : Message<E>(source)

/**
 *  Message from an edge indicating that a vertex has been removed.
 */
class VertexRemoved<out E : Edge, out V : Vertex>(
    source: E,
    val removedVertex: V
) : Message<E>(source)

/**
 *  Message from an edge indicating that the edge type has been modified.
 */
class EdgeTypeModified<out E : Edge, out V : Vertex>(
    source: E,
    val oldType: V,
    val newType: V
) : Message<E>(source)
