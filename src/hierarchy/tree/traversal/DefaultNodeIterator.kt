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
 *  Default implementation of [NodeIterator] that iterates in pre-order
 *  depth-first traversal.
 */
class DefaultNodeIterator : AbstractIterator<Node>, NodeIterator {
    /**
     *  Tree walker that is used to determine the next node.
     */
    private val treeWalker: TreeWalker

    /**
     *  Whether the iteration is at the beginning.
     */
    private var isBegin: Boolean = true

    /**
     *  @param root
     *      Root of the subtree whose nodes are iterated over.
     *
     *  @param filter
     *      Filter for screening nodes.
     */
    constructor(
        root: Node,
        filter: ((Node) -> NodeAcceptance)?
    ): super()
    {
        this.treeWalker = DefaultTreeWalker(root, filter)
    }

    /**
     *  Filter decorator that returns [NodeAcceptance.ACCEPT] if there is no
     *  filter.
     */
    private fun filterDecorator(node: Node): NodeAcceptance =
        filter?.invoke(node) ?: NodeAcceptance.ACCEPT

    override val order: TraversalOrder =
        TraversalOrder.PREORDER_DEPTH

    override val root: Node
        get() = treeWalker.root

    override val filter: ((Node)-> NodeAcceptance)?
        get() = treeWalker.filter

    override fun computeNext() {
        if (isBegin) {
            // Bootstrap the iteration.
            when (filterDecorator(treeWalker.currentNode)) {
                NodeAcceptance.ACCEPT, NodeAcceptance.ACCEPT_SELF -> {
                    setNext(treeWalker.currentNode)
                }

                NodeAcceptance.REJECT -> {
                    done()
                }
            }

            isBegin = false
        } else {
            if (treeWalker.firstChild() != null) {
                setNext(treeWalker.currentNode)
            } else if (treeWalker.nextSibling() != null) {
                setNext(treeWalker.currentNode)
            } else {
                var siblingOfAncestor: Node? = null

                // Find the nearest ancestor with respect to the current node
                // in this subtree that has a visible next sibling.
                while (treeWalker.ancestorNode() != null) {
                    if (treeWalker.nextSibling() != null) {
                        siblingOfAncestor = treeWalker.currentNode
                        break
                    }
                }

                if (siblingOfAncestor == null) {
                    done()
                } else {
                    setNext(siblingOfAncestor)
                }
            }
        }
    }
}
