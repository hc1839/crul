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

/**
 *  Skeletal implementation of [Node].
 *
 *  Implementations only need to implement [Node.cloneNode].
 *
 *  @param D
 *      Type of user data.
 *
 *  @param N
 *      Type of node to use in parameters and returns.
 */
abstract class AbstractNode<D, N> : Node<D, N>
    where D : Any,
          N : AbstractNode<D, N>
{
    /**
     *  Backing property for keys associated with user data and handlers.
     */
    protected val _userData:
        MutableMap<String, Pair<D, UserDataHandler<D, N>?>> = mutableMapOf()

    /**
     *  Backing property for list of children.
     */
    private val _childNodes: MutableList<N> = mutableListOf()

    /**
     *  Weak reference to the parent node if any.
     */
    private var parentWeakRef: WeakRef<N>? = null

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
        other: AbstractNode<D, N>,
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

    override val childNodes: List<N>
        get() = _childNodes.toList()

    override fun hasChildNodes(): Boolean =
        !_childNodes.isEmpty()

    override val firstChild: N?
        get() = _childNodes.firstOrNull()

    override val lastChild: N?
        get() = _childNodes.lastOrNull()

    override val parentNode: N?
        get() =
            if (parentWeakRef == null) {
                null
            } else {
                parentWeakRef!!.get()
            }

    override val firstSibling: N
        get() {
            val parent = parentNode

            return if (parent != null) {
                parent.childNodes.first()
            } else {
                @Suppress("UNCHECKED_CAST")
                this as N
            }
        }

    override val lastSibling: N
        get() {
            val parent = parentNode

            return if (parent != null) {
                parent.childNodes.last()
            } else {
                @Suppress("UNCHECKED_CAST")
                this as N
            }
        }

    override val nextSibling: N?
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

    override val previousSibling: N?
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

    override val rootNode: N
        get() = ancestorNodes(true).last()

    override fun descendants(includeSelf: Boolean): Sequence<N> {
        val iterator = object: AbstractIterator<N>() {
            private var nextNode: N? = firstChild

            override fun computeNext() {
                if (nextNode == null) {
                    done()
                    return
                }

                setNext(nextNode!!)

                // What was the next node is now the current node, which is
                // used to determine the new next node.
                val currNode = nextNode!!

                if (currNode.hasChildNodes()) {
                    nextNode = currNode.firstChild
                } else {
                    // Reference node in the subtree that changes during
                    // traversal.
                    var refNode: N = currNode

                    // Find the nearest ancestor or self with respect to the
                    // reference node in this subtree that has a next sibling.
                    while (refNode !== this@AbstractNode) {
                        if (refNode.nextSibling != null) {
                            break
                        }

                        refNode = refNode.parentNode!!
                    }

                    nextNode = refNode.nextSibling
                }
            }
        }

        return if (includeSelf) {
            @Suppress("UNCHECKED_CAST")
            listOf(this as N).asSequence() + iterator.asSequence()
        } else {
            iterator.asSequence()
        }
    }

    override fun ancestorNodes(includeSelf: Boolean): Sequence<N> {
        val iterator = object: AbstractIterator<N>() {
            private var nextNode: N? = parentNode

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
            @Suppress("UNCHECKED_CAST")
            listOf(this as N).asSequence() + iterator.asSequence()
        } else {
            iterator.asSequence()
        }
    }

    override fun appendChild(newChild: N): N {
        // Check that the node to be added is not this node or an ancestor.
        if (ancestorNodes(true).any { it === newChild }) {
            throw IllegalArgumentException(
                "Node to be added is this node or an ancestor of it."
            )
        }

        newChild.parentNode?.removeChild(newChild)
        _childNodes.add(newChild)

        newChild.parentWeakRef =
            @Suppress("UNCHECKED_CAST")
            WeakRef(this as N)

        return newChild
    }

    override fun insertBefore(newChild: N, refChild: N): N {
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

        newChild.parentWeakRef =
            @Suppress("UNCHECKED_CAST")
            WeakRef(this as N)

        return newChild
    }

    override fun removeChild(oldChild: N): N {
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

    override fun getUserData(key: String): D? =
        _userData[key]?.first

    override fun setUserData(
        key: String,
        userData: D?,
        handler: UserDataHandler<D, N>?
    ): D?
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
