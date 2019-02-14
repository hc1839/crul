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
 *  Mutable builder for [TreeWalker].
 *
 *  To construct an instance of this class, use [create].
 */
open class TreeWalkerBuilder<B : TreeWalkerBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    protected var _root: Node? = null
        private set

    /**
     *  Configures the root.
     *
     *  It must be set before [build] is called.
     */
    fun root(value: Node): B {
        _root = value
        return _this
    }

    protected var _filter: ((Node) -> FilterState)? = null
        private set

    /**
     *  Configures the filter.
     */
    fun filter(value: ((Node) -> FilterState)?): B {
        _filter = value
        return _this
    }

    /**
     *  Constructs a default implementation of [TreeWalker].
     */
    open fun build(): TreeWalker =
        TreeWalkerImpl(_root!!, _filter)

    companion object {
        private class TreeWalkerBuilderImpl() :
            TreeWalkerBuilder<TreeWalkerBuilderImpl>()

        /**
         *  Creates an instance of [TreeWalkerBuilder].
         */
        @JvmStatic
        fun create(): TreeWalkerBuilder<*> =
            TreeWalkerBuilderImpl()
    }
}
