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

package crul.hierarchy.tree

import crul.hierarchy.tree.traversal.NodeIterator
import crul.hierarchy.tree.traversal.TraversalOrder
import crul.hierarchy.tree.traversal.TreeWalker

/**
 *  Mutable builder for [Node].
 *
 *  To construct an instance of this class, use [newInstance].
 */
open class NodeBuilder<B : NodeBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    /**
     *  Constructs a default implementation of [Node].
     */
    open fun build(): Node =
        NodeImpl()

    companion object {
        private class NodeBuilderImpl() :
            NodeBuilder<NodeBuilderImpl>()

        /**
         *  Creates an instance of [NodeBuilder].
         */
        @JvmStatic
        fun newInstance(): NodeBuilder<*> =
            NodeBuilderImpl()
    }
}
