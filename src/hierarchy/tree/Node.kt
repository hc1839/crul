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
     *  ID of this node.
     *
     *  It is unique in the tree.
     */
    val id: String

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
     *  Constructs a root node.
     *
     *  To construct a child node, use [createChild].
     *
     *  @param type
     *      Node type.
     *
     *  @param id
     *      Node ID. It must conform to XML NCName production.
     */
    @JvmOverloads
    constructor(type: T, id: String = uuid.Generator.inNCName()) {
        if (!xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "Node ID does not conform to XML NCName production."
            )
        }

        this.id = id
        this.type = type
    }

    /**
     *  Depth of this node with respect to the root node.
     *
     *  Root node is defined to have a depth of `0`.
     */
    val depth: Int
        get() {
            var currNode: Node<T> = this

            // Number of levels up relative to this node for the current node.
            var levelsUp: Int = 0

            while (currNode.parentNode != null) {
                currNode = currNode.parentNode!!
                ++levelsUp
            }

            return levelsUp
        }

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
                // Use private property to avoid list copying.
                parent._childNodes.first()
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
                // Use private property to avoid list copying.
                parent._childNodes.last()
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

            // Use private property to avoid list copying.
            val siblingsWithSelf = parent._childNodes

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

            // Use private property to avoid list copying.
            val siblingsWithSelf = parent._childNodes

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
        get() {
            var ancestor: Node<T> = this

            while (true) {
                val parentOfAncestor = ancestor.parentNode

                if (parentOfAncestor != null) {
                    ancestor = parentOfAncestor
                } else {
                    break
                }
            }

            // Ancestor is now the root node.
            return ancestor
        }

    /**
     *  Descendants of this node in a memory-efficient depth-first order.
     */
    fun descendantsByDepth(): Sequence<Node<T>> =
        object: AbstractIterator<Node<T>>() {
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

                if (currNode.firstChild != null) {
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
        }.asSequence()

    /**
     *  Descendants of this node in a memory-efficient breadth-first order.
     *
     *  @param bottomUp
     *      Whether to start from the bottom. If `true`, the subtree is
     *      necessarily traversed once in order to determine the first node
     *      with the greatest depth before returning the sequence.
     */
    fun descendantsByBreadth(bottomUp: Boolean = false): Sequence<Node<T>> =
        object: AbstractIterator<Node<T>>() {
            private var nextNode: Node<T>? =
                if (!bottomUp) {
                    firstChild
                } else {
                    // Temporary variable to hold the descendant with the
                    // currently greatest depth without having to recalculate
                    // its depth in later comparisons with other descendants.
                    var descendantDepthPair: Pair<Node<T>, Int> =
                        Pair(this@Node, depth)

                    // Find the descendant with the greatest depth.
                    for (descendantNode in descendantsByDepth()) {
                        if (!descendantNode.hasChildNodes()) {
                            val descendantDepth = descendantNode.depth

                            if (descendantDepth > descendantDepthPair.second) {
                                descendantDepthPair =
                                    Pair(descendantNode, descendantDepth)
                            }
                        }
                    }

                    descendantDepthPair.first
                }

            // Current depth with respect to the root node. If the initial
            // value of the next node is `null`, it does not matter what the
            // initial value of the current depth is.
            private var currDepth: Int = nextNode?.depth ?: -1

            override fun computeNext() {
                if (nextNode == null) {
                    done()
                    return
                }

                setNext(nextNode!!)

                var newNextNode: Node<T>? = null

                // What was the next node is now the current node, which is
                // used to determine the new next node.
                val currNode = nextNode!!

                if (currNode.nextSibling != null) {
                    newNextNode = currNode.nextSibling
                } else {
                    // Reference node in the subtree that the walker is on.
                    var refNode: Node<T> = currNode

                    while (newNextNode == null) {
                        // Find the nearest ancestor or self with respect to
                        // the reference node in this subtree that has a next
                        // sibling.
                        while (!refNode.isSameNode(this@Node)) {
                            if (refNode.nextSibling != null) {
                                break
                            }

                            refNode = refNode.parentNode!!
                        }

                        if (refNode.isSameNode(this@Node)) {
                            // Current depth has been exhausted. Advance to the
                            // next depth.
                            if (!bottomUp) {
                                ++currDepth
                            } else {
                                --currDepth
                            }

                            newNextNode =
                                if (bottomUp && currDepth <= depth) {
                                    null
                                } else {
                                    descendantsByDepth().firstOrNull {
                                        it.depth == currDepth
                                    }
                                }

                            break
                        } else {
                            // Advance to the next subtree.
                            refNode = refNode.nextSibling!!

                            newNextNode = refNode
                                .descendantsByDepth()
                                .firstOrNull { it.depth == currDepth }
                        }
                    }
                }

                nextNode = newNextNode
            }
        }.asSequence()

    /**
     *  Retrieves a descendant node using node ID.
     *
     *  @param id
     *      ID of the descendant node to retrieve.
     *
     *  @return
     *      Descendant node, or `null` if there is no such node.
     */
    fun getDescendantById(id: String): Node<T>? =
        descendantsByDepth().firstOrNull {
            it.id == id
        }

    /**
     *  Whether a given ID is unique in the tree.
     */
    fun isIdUnique(testId: String): Boolean {
        if (rootNode.id == testId) {
            return false
        }

        for (node in rootNode.descendantsByDepth()) {
            if (node.id == testId) {
                return false
            }
        }

        return true
    }

    /**
     *  Inserts a node as a child.
     *
     *  The node is added as a child, and its parent node is set to this node.
     *  No checks are done for the uniqueness of node IDs.
     *
     *  If an exception is raised, there would be no side effects.
     *
     *  @param newChild
     *      Node to be inserted.
     *
     *  @param refChild
     *      See [createChild].
     *
     *  @return
     *      Node that is inserted.
     */
    private fun insertBefore(
        newChild: Node<T>,
        refChild: Node<T>? = null
    ): Node<T>
    {
        if (refChild == null) {
            _childNodes.add(newChild)
        } else {
            val refChildIndex = _childNodes.indexOfFirst {
                it.isSameNode(refChild)
            }

            if (refChildIndex == -1) {
                throw IllegalArgumentException(
                    "No such reference node in the list of children."
                )
            }

            _childNodes.add(refChildIndex, newChild)
        }

        newChild.parentWeakRef = WeakRef(this)

        return newChild
    }

    /**
     *  Creates a new child node.
     *
     *  If an exception is raised, there would be no side effects.
     *
     *  @param newChildType
     *      Node type of the new child.
     *
     *  @param newChildId
     *      ID of the new child node. It must conform to XML NCName production
     *      and be unique in this tree. If `null`, a unique ID is generated.
     *
     *  @param refChild
     *      Reference node that the new child node is inserted before. If
     *      `null`, the new child node is inserted at the end of the list of
     *      children. If it is not a child of this node, an exception is
     *      raised.
     *
     *  @return
     *      New child node.
     */
    @JvmOverloads
    fun createChild(
        newChildType: T,
        newChildId: String? = null,
        refChild: Node<T>? = null
    ): Node<T>
    {
        val newChildIdToUse =
            if (newChildId == null) {
                var uuidBuf: String

                do {
                    uuidBuf = uuid.Generator.inNCName()
                } while (!isIdUnique(uuidBuf))

                uuidBuf
            } else {
                if (!isIdUnique(newChildId)) {
                    throw IllegalArgumentException(
                        "ID is not unique: $newChildId"
                    )
                }

                newChildId
            }

        val newChild = Node<T>(newChildType, newChildIdToUse)

        insertBefore(newChild, refChild)

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
            it.id == id && it.isSameNode(this)
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
    fun cloneNode(
        deep: Boolean,
        includeUserData: Boolean
    ): Node<T>
    {
        val clonedNode = Node<T>(type, id)

        if (includeUserData) {
            clonedNode._userData.putAll(_userData)
        }

        if (deep) {
            for (childNode in _childNodes) {
                val clonedChildNode = childNode.cloneNode(
                    deep,
                    includeUserData
                )

                clonedNode._childNodes.add(clonedChildNode)
                clonedChildNode.parentWeakRef = WeakRef(clonedNode)
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
     *  Imports the clone of a node.
     *
     *  IDs of `other` and, if `deep` is `true`, its descendants must not
     *  already exist in this tree. As a consequence, importing a node of this
     *  tree will fail.
     *
     *  If an exception is raised, there would be no side effects.
     *
     *  @param other
     *      Node to be cloned and added as a child to this node.
     *
     *  @param deep
     *      See [cloneNode].
     *
     *  @param includeUserData
     *      See [cloneNode].
     *
     *  @param refChild
     *      See [createChild].
     *
     *  @return
     *      Clone of `other` that has been imported.
     */
    @JvmOverloads
    fun importNode(
        other: Node<T>,
        deep: Boolean,
        includeUserData: Boolean,
        refChild: Node<T>? = null
    ): Node<T>
    {
        val newChild = other.cloneNode(deep, includeUserData)

        if (!isIdUnique(newChild.id)) {
            throw IllegalArgumentException(
                "Node ID is not unique: ${newChild.id}"
            )
        }

        for (newDescendant in descendantsByDepth()) {
            if (!isIdUnique(newDescendant.id)) {
                throw IllegalArgumentException(
                    "Node ID is not unique: ${newDescendant.id}"
                )
            }
        }

        insertBefore(newChild, refChild)

        return newChild
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
     *      Key to be associated with `userData` and `handler`. It must conform
     *      to XML NCName production.
     *
     *  @param userData
     *      Object to be associated by `key`, or `null` to remove `key`.
     *
     *  @param handler
     *      Handler to be associated by `key`, or `null` if it is not needed.
     *
     *  @return
     *      Object that was previously associated by `key`, or `null` if there
     *      was none.
     */
    fun setUserData(
        key: String,
        userData: Any?,
        handler: UserDataHandler<T>?
    ): Any?
    {
        if (!xml.Datatype.isNCName(key)) {
            throw IllegalArgumentException(
                "Key does not conform to XML NCName production."
            )
        }

        val prevUserData = getUserData(key)

        if (userData == null) {
            _userData.remove(key)
        } else {
            _userData[key] = Pair(userData, handler)
        }

        return prevUserData
    }
}
