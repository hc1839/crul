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

package hierarchy.tree.traversal

import hierarchy.tree.Node

/**
 *  Default implementation of [TreeWalker].
 */
internal class TreeWalkerImpl : TreeWalker {
    override val root: Node

    override val filter: ((Node) -> NodeAcceptance)?

    override var currentNode: Node

    /**
     *  @param root
     *      Root of the subtree to walk.
     *
     *  @param filter
     *      Filter for screening nodes.
     */
    constructor(
        root: Node,
        filter: ((Node) -> NodeAcceptance)?
    ) {
        this.root = root
        this.filter = filter
        this.currentNode = this.root
    }

    /**
     *  Filter decorator that returns [NodeAcceptance.ACCEPT] if there is no
     *  filter.
     */
    private fun filterDecorator(node: Node): NodeAcceptance =
        filter?.invoke(node) ?: NodeAcceptance.ACCEPT

    override fun ancestorNode(): Node? {
        // Verify that the current node is below the root.
        if (!currentNode.ancestorNodes(false).any { it === root }) {
            return null
        }

        var ancestor: Node? = currentNode.parentNode

        while (ancestor != null && ancestor !== root) {
            when (filterDecorator(ancestor)) {
                NodeAcceptance.ACCEPT, NodeAcceptance.ACCEPT_SELF -> {
                    currentNode = ancestor
                    return ancestor
                }

                else -> {
                    ancestor = ancestor.parentNode
                }
            }
        }

        return null
    }

    override fun firstChild(): Node? {
        // Apply filter to current node to decide whether to proceed in the
        // specified direction.
        when (filterDecorator(currentNode)) {
            NodeAcceptance.REJECT, NodeAcceptance.ACCEPT_SELF -> {
                return null
            }

            else -> {}
        }

        var child: Node? = currentNode.firstChild

        while (child != null) {
            when (filterDecorator(child)) {
                NodeAcceptance.ACCEPT, NodeAcceptance.ACCEPT_SELF -> {
                    // Verify that the found node is within the subtree.
                    if (child.ancestorNodes(true).any { it === root }) {
                        currentNode = child
                        return child
                    } else {
                        return null
                    }
                }

                else -> {
                    child = child.nextSibling
                }
            }
        }

        return null
    }

    override fun lastChild(): Node? {
        // Apply filter to current node to decide whether to proceed in the
        // specified direction.
        when (filterDecorator(currentNode)) {
            NodeAcceptance.REJECT, NodeAcceptance.ACCEPT_SELF -> {
                return null
            }

            else -> {}
        }

        var child: Node? = currentNode.lastChild

        while (child != null) {
            when (filterDecorator(child)) {
                NodeAcceptance.ACCEPT, NodeAcceptance.ACCEPT_SELF -> {
                    // Verify that the found node is within the subtree.
                    if (child.ancestorNodes(true).any { it === root }) {
                        currentNode = child
                        return child
                    } else {
                        return null
                    }
                }

                else -> {
                    child = child.previousSibling
                }
            }
        }

        return null
    }

    override fun previousSibling(): Node? {
        // Verify that the current node is within the subtree.
        if (!currentNode.ancestorNodes(true).any { it === root }) {
            return null
        }

        var sibling: Node? = currentNode.previousSibling

        while (sibling != null) {
            when (filterDecorator(sibling)) {
                NodeAcceptance.ACCEPT, NodeAcceptance.ACCEPT_SELF -> {
                    currentNode = sibling
                    return sibling
                }

                else -> {
                    sibling = sibling.previousSibling
                }
            }
        }

        return null
    }

    override fun nextSibling(): Node? {
        // Verify that the current node is within the subtree.
        if (!currentNode.ancestorNodes(true).any { it === root }) {
            return null
        }

        var sibling: Node? = currentNode.nextSibling

        while (sibling != null) {
            when (filterDecorator(sibling)) {
                NodeAcceptance.ACCEPT, NodeAcceptance.ACCEPT_SELF -> {
                    currentNode = sibling
                    return sibling
                }

                else -> {
                    sibling = sibling.nextSibling
                }
            }
        }

        return null
    }
}
