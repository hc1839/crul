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

package hierarchy.tree

import java.lang.ref.WeakReference as WeakRef

import hierarchy.tree.traversal.DefaultNodeIterator
import hierarchy.tree.traversal.DefaultTreeWalker
import hierarchy.tree.traversal.NodeAcceptance
import hierarchy.tree.traversal.NodeIterator
import hierarchy.tree.traversal.TraversalOrder
import hierarchy.tree.traversal.TreeWalker

/**
 *  Skeletal implementation of [Node].
 *
 *  Implementations only need to implement [Node.cloneNode].
 */
abstract class AbstractNode : Node {
    /**
     *  Backing property for keys associated with user data and handlers.
     */
    protected val _userData:
        MutableMap<String, Pair<Any, UserDataHandler?>> = mutableMapOf()

    /**
     *  Backing property for list of children.
     */
    private val _childNodes: MutableList<Node> = mutableListOf()

    /**
     *  Weak reference to the parent node if any.
     */
    private var parentWeakRef: WeakRef<Node>? = null

    /**
     *  Constructs a new node with no parent.
     */
    constructor()

    /**
     *  Copy constructor.
     *
     *  The copied node will have no parent. Handlers are not called.
     *
     *  @param other
     *      Node to copy.
     *
     *  @param deep
     *      Whether to clone (using [Node.cloneNode]) the descendants of this
     *      node. If `false`, the copied node will have no descendants.
     *
     *  @param includeUserData
     *      Whether to include user data (along with the handlers) in the
     *      duplicated node.
     */
    constructor(
        other: AbstractNode,
        deep: Boolean = false,
        includeUserData: Boolean = false
    ) {
        if (includeUserData) {
            _userData.putAll(other._userData)
        }

        if (deep) {
            for (otherChildNode in other.childNodes) {
                val clonedChildNode = otherChildNode.cloneNode(
                    deep,
                    includeUserData
                )

                _childNodes.add(clonedChildNode)
            }
        }
    }

    override val depth: Int
        get() = ancestorNodes(false).count()

    override val childNodes: List<Node>
        get() = _childNodes.toList()

    override fun hasChildNodes(): Boolean =
        !_childNodes.isEmpty()

    override val firstChild: Node?
        get() = _childNodes.firstOrNull()

    override val lastChild: Node?
        get() = _childNodes.lastOrNull()

    override val parentNode: Node?
        get() =
            if (parentWeakRef == null) {
                null
            } else {
                parentWeakRef!!.get()
            }

    override val nextSibling: Node?
        get() {
            val parent = parentNode

            if (parent == null) {
                return null
            }

            val siblingsWithSelf = parent.childNodes

            // Index of this node in the parent's list of children.
            val thisIndex = siblingsWithSelf.indexOfFirst { it === this }

            return if (thisIndex < siblingsWithSelf.lastIndex) {
                siblingsWithSelf[thisIndex + 1]
            } else {
                null
            }
        }

    override val previousSibling: Node?
        get() {
            val parent = parentNode

            if (parent == null) {
                return null
            }

            val siblingsWithSelf = parent.childNodes

            // Index of this node in the parent's list of children.
            val thisIndex = siblingsWithSelf.indexOfFirst { it === this }

            return if (thisIndex > 0) {
                siblingsWithSelf[thisIndex - 1]
            } else {
                null
            }
        }

    override val rootNode: Node
        get() = ancestorNodes(true).last()

    override fun ancestorNodes(includeSelf: Boolean): Sequence<Node> {
        val iterator = object: AbstractIterator<Node>() {
            private var nextNode: Node? = parentNode

            override fun computeNext() {
                if (nextNode == null) {
                    done()
                    return
                }

                setNext(nextNode!!)

                nextNode = nextNode!!.parentNode
            }
        }

        return if (includeSelf) {
            listOf<Node>(this).asSequence() + iterator.asSequence()
        } else {
            iterator.asSequence()
        }
    }

    override fun createTreeWalker(
        filter: ((Node) -> NodeAcceptance)?
    ): TreeWalker =
        DefaultTreeWalker(this, filter)

    override fun createNodeIterator(
        order: TraversalOrder,
        filter: ((Node) -> NodeAcceptance)?
    ): NodeIterator =
        when (order) {
            TraversalOrder.PREORDER_DEPTH ->
                DefaultNodeIterator(this, filter)
        }

    override fun appendChild(newChild: Node): Node {
        newChild as AbstractNode

        // Check that the node to be added is not this node or an ancestor.
        if (ancestorNodes(true).any { it === newChild }) {
            throw IllegalArgumentException(
                "Node to be added is this node or an ancestor of it."
            )
        }

        newChild.parentNode?.removeChild(newChild)
        _childNodes.add(newChild)

        newChild.parentWeakRef = WeakRef(this)

        return newChild
    }

    override fun insertBefore(newChild: Node, refChild: Node): Node {
        newChild as AbstractNode
        refChild as AbstractNode

        // Check that the node to be added is not this node or an ancestor.
        if (ancestorNodes(true).any { it === newChild }) {
            throw IllegalArgumentException(
                "Node to be added is this node or an ancestor of it."
            )
        }

        if (refChild === newChild) {
            throw IllegalArgumentException(
                "Reference child is the same node as the node to be added."
            )
        }

        if (!_childNodes.any { it === refChild }) {
            throw IllegalArgumentException(
                "No such reference node in the list of children."
            )
        }

        newChild.parentNode?.removeChild(newChild)

        // This must be done after removing the new child in the case that the
        // new child is in the same list of children.
        val refChildIndex = _childNodes.indexOfFirst {
            it === refChild
        }

        _childNodes.add(refChildIndex, newChild)

        newChild.parentWeakRef = WeakRef(this)

        return newChild
    }

    override fun removeChild(oldChild: Node): Node {
        oldChild as AbstractNode

        if (oldChild.parentNode == null) {
            return oldChild
        }

        val oldChildIndex = _childNodes.indexOfFirst {
            it === this
        }

        if (oldChildIndex == -1) {
            throw RuntimeException(
                "No such child node."
            )
        }

        _childNodes.removeAt(oldChildIndex)
        oldChild.parentWeakRef = null

        // Call the handlers.
        for (key in _userData.keys) {
            val (userData, handler) = _userData[key]!!

            if (handler != null) {
                handler.handle(
                    UserDataHandler.Operation.REMOVED,
                    key,
                    userData,
                    null,
                    null
                )
            }
        }

        return oldChild
    }

    override fun getUserData(key: String): Any? =
        _userData[key]?.first

    override fun setUserData(
        key: String,
        userData: Any?,
        handler: UserDataHandler?
    ): Any?
    {
        val prevUserData = getUserData(key)

        if (userData == null) {
            _userData.remove(key)
        } else {
            _userData[key] = Pair(userData, handler)
        }

        return prevUserData
    }
}
