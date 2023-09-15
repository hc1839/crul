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
 *  Navigator of nodes in a tree.
 *
 *  It is inspired by `org.w3c.dom.traversal.TreeWalker`.
 */
interface TreeWalker {
    /**
     *  Root of the subtree being walked.
     */
    val root: Node

    /**
     *  Filter used to screen nodes.
     */
    val filter: ((Node) -> FilterState)?

    /**
     *  Current node that this walker is positioned.
     *
     *  It can be set to any node, regardless of whether it is within the
     *  subtree specified by [root] or whether it would be accepted by the
     *  filter.
     *
     *  The traversal searches the next visible node starting from the current
     *  node by applying the filter in the specified direction without regard
     *  to whether the encountered nodes are within the subtree. If the
     *  specified direction is down the tree, the filter is applied to the
     *  current node before proceeding, since [FilterState.REJECT_SUBTREE] or
     *  [FilterState.ACCEPT_SELF_ONLY] does not have visible children. If the
     *  first visible node that is found is not within the subtree, the
     *  traversal in the specified direction is considered not possible, and
     *  the current node is not changed
     */
    var currentNode: Node

    /**
     *  Moves to and returns the closest visible ancestor node of the current
     *  node.
     *
     *  If the search attempts to step upward from [root] or fails to find a
     *  visible ancestor node, the current node is not changed, and `null` is
     *  returned.
     */
    fun ancestorNode(): Node?

    /**
     *  Moves to and returns the first visible child of the current node.
     *
     *  If the current node has no visible children, the current node is not
     *  changed, and `null` is returned.
     */
    fun firstChild(): Node?

    /**
     *  Moves to and returns the last visible child of the current node.
     *
     *  If the current node has no visible children, the current node is not
     *  changed, and `null` is returned.
     */
    fun lastChild(): Node?

    /**
     *  Moves to and returns the previous sibling of the current node.
     *
     *  If the current node has no visible previous sibling, the current node
     *  is not changed, and `null` is returned.
     */
    fun previousSibling(): Node?

    /**
     *  Moves to and returns the next sibling of the current node.
     *
     *  If the current node has no visible next sibling, the current node is
     *  not changed, and `null` is returned.
     */
    fun nextSibling(): Node?
}
