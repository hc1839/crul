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

package hypergraph.notify

import hypergraph.base.*

/**
 *  Message from a vertex indicating that a name has been added.
 */
class VertexNameAdded<out V : Vertex>(
    source: V,
    val name: String
) : Message<V>(source)

/**
 *  Message from a vertex indicating that a name has been removed.
 */
class VertexNameRemoved<out V : Vertex>(
    source: V,
    val name: String
) : Message<V>(source)

/**
 *  Message from a vertex indicating that a property has been added.
 */
class PropertyAdded<out V : Vertex, out P : Property>(
    source: V,
    val property: P
) : Message<V>(source)

/**
 *  Message from a vertex indicating that a property has been removed.
 */
class PropertyRemoved<out V : Vertex>(
    source: V,
    val propertyId: String
) : Message<V>(source)

/**
 *  Message from a vertex indicating that two properties have been merged.
 */
class PropertiesMerged<out V : Vertex>(
    source: V,
    val targetPropertyId: String,
    val otherPropertyId: String
) : Message<V>(source)
