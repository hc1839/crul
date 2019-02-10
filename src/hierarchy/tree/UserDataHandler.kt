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

/**
 *  When associating an object to a node using [Node.setUserData], the handler
 *  to call when the node is modified according to [Operation].
 *
 *  It is inspired by `org.w3c.dom.UserDataHandler`.
 */
interface UserDataHandler {
    /**
     *  Function to call after the node is modified.
     *
     *  @param operation
     *      Type of operation applied to the node.
     *
     *  @param key
     *      Key associated with `userData`.
     *
     *  @param userData
     *      User data associated with the node.
     *
     *  @param src
     *      Node that is modified. It is `null` when the node is removed.
     *
     *  @param dst
     *      Newly created node if any, or `null`.
     */
    fun handle(
        operation: Operation,
        key: String,
        userData: Any,
        src: Node?,
        dst: Node?
    )

    /**
     *  Type of operation applied to a node.
     */
    enum class Operation {
        /**
         *  Node is cloned using [Node.cloneNode].
         */
        CLONED,

        /**
         *  Node is removed using [Node.removeChild].
         */
        REMOVED
    }
}
