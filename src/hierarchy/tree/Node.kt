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
 *  Node in a tree.
 *
 *  Order of child nodes is significant.
 *
 *  Interface of this class is inspired by `org.w3c.dom.Node`.
 *
 *  @param T
 *      Enum type representing node type.
 */
class Node<T : Enum<T>> {
    /**
     *  Backing property for keys associated with user data and handlers.
     */
    private val _userData: MutableMap<String, Pair<Any, UserDataHandler<T>?>> =
        mutableMapOf()

    /**
     *  Backing property for list of children.
     */
    private val _childNodes: MutableList<Node<T>> = mutableListOf()

    /**
     *  Weak reference to the parent node if any.
     */
    private var parentWeakRef: WeakRef<Node<T>>? = null

    /**
     *  Node type as an enum constant.
     */
    var type: T
        set(value) {
            field = value

            // Call the handlers.
            for (key in _userData.keys) {
                val (userData, handler) = _userData[key]!!

                if (handler != null) {
                    handler.handle(
                        UserDataHandler.Operation.TYPE_CHANGED,
                        key,
                        userData,
                        this,
                        null
                    )
                }
            }
        }

    /**
     *  @param type
     *      Node type.
     */
    constructor(type: T) {
        this.type = type
    }

    /**
     *  Depth of this node with respect to the root node.
     *
     *  Root node is defined to have a depth of `0`.
     */
    val depth: Int
        get() = ancestorNodes().count()

    /**
     *  Whether `other` references the same node as this.
     *
     *  It is equilvalent to using the referential equality operator.
     */
    fun isSameNode(other: Node<T>): Boolean =
        other === this

    /**
     *  List of child nodes, or empty list if there are none.
     */
    val childNodes: List<Node<T>>
        get() = _childNodes.toList()

    /**
     *  Whether this node has any children.
     */
    fun hasChildNodes(): Boolean =
        !_childNodes.isEmpty()

    /**
     *  First child node, or `null` if there is no such node.
     */
    val firstChild: Node<T>?
        get() = _childNodes.firstOrNull()

    /**
     *  Last child node, or `null` if there is no such node.
     */
    val lastChild: Node<T>?
        get() = _childNodes.lastOrNull()

    /**
     *  Parent node, or `null` if there is no such node.
     */
    val parentNode: Node<T>?
        get() =
            if (parentWeakRef == null) {
                null
            } else {
                parentWeakRef!!.get()
            }

    /**
     *  First node at the same hierarchical level as this node.
     */
    val firstSibling: Node<T>
        get() {
            val parent = parentNode

            return if (parent != null) {
                parent.childNodes.first()
            } else {
                this
            }
        }

    /**
     *  Last node at the same hierarchical level as this node.
     */
    val lastSibling: Node<T>
        get() {
            val parent = parentNode

            return if (parent != null) {
                parent.childNodes.last()
            } else {
                this
            }
        }

    /**
     *  Node that follows this node at the same hierarchical level, or  `null`
     *  if there is no such node.
     */
    val nextSibling: Node<T>?
        get() {
            val parent = parentNode

            if (parent == null) {
                return null
            }

            val siblingsWithSelf = parent.childNodes

            // Index of this node in the parent's list of children.
            val thisIndex = siblingsWithSelf.indexOfFirst { isSameNode(it) }

            return if (thisIndex < siblingsWithSelf.lastIndex) {
                siblingsWithSelf[thisIndex + 1]
            } else {
                null
            }
        }

    /**
     *  Node that precedes this node at the same hierarchical level, or `null`
     *  if there is no such node.
     */
    val previousSibling: Node<T>?
        get() {
            val parent = parentNode

            if (parent == null) {
                return null
            }

            val siblingsWithSelf = parent.childNodes

            // Index of this node in the parent's list of children.
            val thisIndex = siblingsWithSelf.indexOfFirst { isSameNode(it) }

            return if (thisIndex > 0) {
                siblingsWithSelf[thisIndex - 1]
            } else {
                null
            }
        }

    /**
     *  Root node.
     */
    val rootNode: Node<T>
        get() = ancestorNodes(true).last()

    /**
     *  Descendants of this node in depth-first order.
     *
     *  @param includeSelf
     *      Whether to include this node at the beginning of the traversal.
     */
    @JvmOverloads
    fun descendants(includeSelf: Boolean = false): Sequence<Node<T>> {
        val iterator = object: AbstractIterator<Node<T>>() {
            private var nextNode: Node<T>? = firstChild

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
                    var refNode: Node<T> = currNode

                    // Find the nearest ancestor or self with respect to the
                    // reference node in this subtree that has a next sibling.
                    while (!refNode.isSameNode(this@Node)) {
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
            listOf(this).asSequence() + iterator.asSequence()
        } else {
            iterator.asSequence()
        }
    }

    /**
     *  Ancestors of this node in order of decreasing depth.
     *
     *  @param includeSelf
     *      Whether to include this node at the beginning of the traversal.
     */
    @JvmOverloads
    fun ancestorNodes(includeSelf: Boolean = false): Sequence<Node<T>> {
        val iterator = object: AbstractIterator<Node<T>>() {
            private var nextNode: Node<T>? = parentNode

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
            listOf(this).asSequence() + iterator.asSequence()
        } else {
            iterator.asSequence()
        }
    }

    /**
     *  Adds a node as a child of this node.
     *
     *  If an exception is raised, there are no side effects.
     *
     *  @param newChild
     *      Node to be added as a child. If it has a parent, it is first
     *      removed. If it is this node or an ancestor of this node, an
     *      exception is raised.
     *
     *  @param refChild
     *      Reference node that the `newChild` is inserted before. If `null`,
     *      `newChild` is added at the end of the list of children. If it is
     *      not a child of this node or if it is the same node as `newChild`,
     *      an exception is raised.
     *
     *  @return
     *      Node that is added.
     */
    @JvmOverloads
    fun addChild(newChild: Node<T>, refChild: Node<T>? = null): Node<T> {
        // Check that the node to be added is not this node or an ancestor.
        if (ancestorNodes(true).any { it.isSameNode(newChild) }) {
            throw IllegalArgumentException(
                "Node to be added is this node or an ancestor of it."
            )
        }

        if (refChild == null) {
            newChild.remove()
            _childNodes.add(newChild)
        } else {
            if (refChild.isSameNode(newChild)) {
                throw IllegalArgumentException(
                    "Reference child is the same node " +
                    "as the node to be added."
                )
            }

            val refChildIndex = _childNodes.indexOfFirst {
                it.isSameNode(refChild)
            }

            if (refChildIndex == -1) {
                throw IllegalArgumentException(
                    "No such reference node in the list of children."
                )
            }

            newChild.remove()
            _childNodes.add(refChildIndex, newChild)
        }

        newChild.parentWeakRef = WeakRef(this)

        return newChild
    }

    /**
     *  Removes this node from the tree.
     *
     *  Descendants of this node will remain as descendants. If this node is
     *  the root node, nothing is done.
     */
    fun remove() {
        val parent = parentNode

        if (parent == null) {
            return
        }

        val thisIndex = parent._childNodes.indexOfFirst {
            it.isSameNode(this)
        }

        if (thisIndex == -1) {
            throw RuntimeException(
                "[Internal Error] " +
                "No such child node in the list of children of the parent."
            )
        }

        parent._childNodes.removeAt(thisIndex)
        parentWeakRef = null

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
    }

    /**
     *  Duplicates this node.
     *
     *  The duplicated node will have no parent.
     *
     *  @param deep
     *      Whether to clone the descendants of this node. If `false`, the
     *      duplicated node will have no descendants.
     *
     *  @param includeUserData
     *      Whether to include user data (along with the handlers) in the
     *      duplicated node.
     *
     *  @return
     *      Duplicated node.
     */
    fun cloneNode(deep: Boolean, includeUserData: Boolean): Node<T> {
        val clonedNode = Node<T>(type)

        if (includeUserData) {
            clonedNode._userData.putAll(_userData)
        }

        if (deep) {
            for (childNode in childNodes) {
                val clonedChildNode = childNode.cloneNode(
                    deep,
                    includeUserData
                )

                clonedNode.addChild(clonedChildNode)
            }
        }

        // Call the handlers.
        for (key in _userData.keys) {
            val (userData, handler) = _userData[key]!!

            if (handler != null) {
                handler.handle(
                    UserDataHandler.Operation.CLONED,
                    key,
                    userData,
                    this,
                    clonedNode
                )
            }
        }

        return clonedNode
    }

    /**
     *  Retrieves the object associated by a key on this node.
     *
     *  The object must have been associated on this node through
     *  [setUserData].
     *
     *  @param key
     *      Key that is associated with the object.
     *
     *  @return
     *      Object that is associated by `key`, or `null` if `key` does not
     *      exist.
     */
    fun getUserData(key: String): Any? =
        _userData[key]?.first

    /**
     *  Associates an object by a key on this node.
     *
     *  The object can be retrieved through [getUserData].
     *
     *  @param key
     *      Key to be associated with `userData` and `handler`.
     *
     *  @param userData
     *      Object to be associated by `key`, or `null` to remove `key`.
     *
     *  @param handler
     *      Handler to be associated by `key`, or `null` if it is not needed.
     *
     *  @return
     *      User data previously associated with `key`, or `null` if there was
     *      none.
     */
    @JvmOverloads
    fun setUserData(
        key: String,
        userData: Any?,
        handler: UserDataHandler<T>? = null
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
