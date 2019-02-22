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

package crul.hypergraph

import crul.hypergraph.base.CanBeProxied as CanBeProxiedIntf
import crul.hypergraph.base.Vertex as VertexIntf
import crul.hypergraph.compare.Equality

/**
 *  Skeletal implementation of a construct that can be proxied.
 */
abstract class CanBeProxied() : CanBeProxiedIntf {
    /**
     *  Whether the construct is being redirected to a terminal construct.
     */
    open protected var isRedirected = false

    /**
     *  Gets the terminal construct that this proxied construct is being
     *  redirected to.
     */
    abstract protected fun getTerminalProxied(): CanBeProxied

    override var proxy: VertexIntf? = null
        get() {
            if (isRedirected) {
                return getTerminalProxied().proxy
            }

            return field
        }
        set(newValue) {
            if (isRedirected) {
                getTerminalProxied().proxy = newValue
                return
            }

            val oldValue = field as Vertex?
            newValue as Vertex?

            // Check whether the given vertex is proxying another construct.
            if (newValue != null && newValue.proxied != null) {
                throw IllegalArgumentException(
                    "Vertex is already proxying a construct."
                )
            }

            // Given vertex must belong to this graph.
            if (newValue != null && newValue.graph !== graph) {
                throw IllegalArgumentException(
                    "Vertex to act as proxy does not belong to this graph."
                )
            }

            // Set the proxy vertex only if necessary.
            if (newValue == null) {
                if (oldValue != null) {
                    oldValue.setProxied(Friendship, null)
                    field = null
                }
            } else if (
                field == null ||
                !Equality.vertices(oldValue!!, newValue)
            ) {
                proxy = null
                newValue.setProxied(Friendship, this)
                field = newValue
            }
        }

    private object FriendKey : crul.visaccess.FriendKey()

    open class FriendAccess(key: crul.visaccess.FriendKey) :
        crul.visaccess.FriendAccess
    {
        init {
            if (key != FriendKey) {
                throw IllegalArgumentException(
                    "Invalid friend key."
                )
            }
        }
    }

    protected object Friendship : FriendAccess(FriendKey)
}
