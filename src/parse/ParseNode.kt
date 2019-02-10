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

package parse

import hierarchy.tree.TypedNode
import hierarchy.tree.UserDataHandler

/**
 *  Node in a parse tree.
 *
 *  @param T
 *      Enum type representing node type.
 */
class ParseNode<T : Enum<T>> : TypedNode<T> {
    /**
     *  @param type
     *      Node type.
     */
    constructor(type: T): super(type)

    /**
     *  Copy constructor.
     */
    constructor(
        other: ParseNode<T>,
        deep: Boolean = false,
        includeUserData: Boolean = false
    ): super(other, deep, includeUserData)

    override fun cloneNode(
        deep: Boolean,
        includeUserData: Boolean
    ): ParseNode<T>
    {
        val clonedNode = ParseNode(this, deep, includeUserData)

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
}
