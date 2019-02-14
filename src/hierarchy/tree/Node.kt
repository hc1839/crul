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

import hierarchy.tree.traversal.FilterState
import hierarchy.tree.traversal.NodeIterator
import hierarchy.tree.traversal.TraversalOrder
import hierarchy.tree.traversal.TreeWalker

/**
 *  Node in a tree.
 *
 *  Order of child nodes is significant.
 *
 *  It is inspired by `org.w3c.dom.Node`.
 */
interface Node {
    /**
     *  Depth of this node with respect to the root node.
     *
     *  Root node is defined to have a depth of `0`.
     */
    val depth: Int

    /**
     *  List of child nodes, or empty list if there are none.
     */
    val childNodes: List<Node>

    /**
     *  Whether this node has any children.
     */
    fun hasChildNodes(): Boolean

    /**
     *  First child node, or `null` if there is no such node.
     */
    val firstChild: Node?

    /**
     *  Last child node, or `null` if there is no such node.
     */
    val lastChild: Node?

    /**
     *  Parent node, or `null` if there is no such node.
     */
    val parentNode: Node?

    /**
     *  Sibling of this node that is next in order, or `null` if there are
     *  none.
     */
    val nextSibling: Node?

    /**
     *  Sibling of this node that is previous in order, or `null` if there are
     *  none.
     */
    val previousSibling: Node?

    /**
     *  Root node.
     */
    val rootNode: Node

    /**
     *  Ancestors of this node in order of decreasing depth.
     *
     *  @param includeSelf
     *      Whether to include this node at the beginning of the traversal.
     */
    fun ancestorNodes(includeSelf: Boolean): Iterator<Node>

    /**
     *  Creates a [TreeWalker] over the subtree rooted at this node.
     */
    fun createTreeWalker(
        filter: ((Node) -> FilterState)?
    ): TreeWalker

    /**
     *  Creates a [NodeIterator] over the subtree rooted at this node.
     */
    fun createNodeIterator(
        order: TraversalOrder,
        filter: ((Node) -> FilterState)?
    ): NodeIterator

    /**
     *  Adds a node to the end of the list of children of this node.
     *
     *  If an exception is raised, there should be no side effects. If there
     *  would be side effects, the implementing class should document it.
     *
     *  @param newChild
     *      Node to be added as a child. If it has a parent, it is first
     *      removed from its tree. If it is this node or an ancestor of this
     *      node, an exception is raised.
     *
     *  @return
     *      Node that is added.
     */
    fun appendChild(newChild: Node): Node

    /**
     *  Inserts a node as a child before an existing child.
     *
     *  If an exception is raised, there should be no side effects. If there
     *  would be side effects, the implementing class should document it.
     *
     *  @param newChild
     *      Node to be added as a child. If it has a parent, it is first
     *      removed from its tree. If it is this node or an ancestor of this
     *      node, an exception is raised.
     *
     *  @param refChild
     *      Reference node that the `newChild` is inserted before. If it is not
     *      a child of this node or if it is the same node as `newChild`, an
     *      exception is raised.
     *
     *  @return
     *      Node that is inserted.
     */
    fun insertBefore(newChild: Node, refChild: Node): Node

    /**
     *  Removes a child node.
     *
     *  Descendants of this node will remain as descendants. If this node is
     *  the root node, nothing is done.
     *
     *  @return
     *      Node that is removed.
     */
    fun removeChild(oldChild: Node): Node

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
    fun cloneNode(deep: Boolean, includeUserData: Boolean): Node

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
    fun getUserData(key: String): Any?

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
    fun setUserData(
        key: String,
        userData: Any?,
        handler: UserDataHandler?
    ): Any?
}
