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

package io.github.hc1839.crul.hierarchy.tree.traversal

import io.github.hc1839.crul.hierarchy.tree.Node

/**
 *  Iterator over nodes in a tree.
 *
 *  It is inspired by `org.w3c.dom.traversal.NodeIterator`.
 */
interface NodeIterator : Iterator<Node> {
    /**
     *  Root of the subtree being iterated.
     */
    val root: Node

    /**
     *  Filter used to screen nodes.
     */
    val filter: ((Node) -> FilterState)?

    /**
     *  Traversal order.
     */
    val order: TraversalOrder
}
