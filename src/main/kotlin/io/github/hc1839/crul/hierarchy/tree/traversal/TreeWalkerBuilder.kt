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
 *  Mutable builder for [TreeWalker].
 *
 *  To construct an instance of this class, use [newInstance].
 */
open class TreeWalkerBuilder<B : TreeWalkerBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    protected var root: Node? = null
        private set

    /**
     *  Configures the root.
     *
     *  It must be set before [build] is called.
     */
    fun setRoot(value: Node): B {
        root = value
        return _this
    }

    protected var filter: ((Node) -> FilterState)? = null
        private set

    /**
     *  Configures the filter.
     */
    fun setFilter(value: ((Node) -> FilterState)?): B {
        filter = value
        return _this
    }

    /**
     *  Constructs a default implementation of [TreeWalker].
     */
    open fun build(): TreeWalker =
        TreeWalkerImpl(root!!, filter)

    companion object {
        private class TreeWalkerBuilderImpl() :
            TreeWalkerBuilder<TreeWalkerBuilderImpl>()

        /**
         *  Creates an instance of [TreeWalkerBuilder].
         */
        @JvmStatic
        fun newInstance(): TreeWalkerBuilder<*> =
            TreeWalkerBuilderImpl()
    }
}
