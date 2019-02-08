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
 *  Node with type.
 *
 *  @param T
 *      Enum type representing node type.
 */
open class TypedNode<T : Enum<T>> : AbstractNode<Any, TypedNode<T>> {
    /**
     *  Node type as an enum constant.
     */
    var type: T

    /**
     *  @param type
     *      Node type.
     */
    constructor(type: T): super() {
        this.type = type
    }

    /**
     *  Copy constructor.
     */
    constructor(
        other: TypedNode<T>,
        deep: Boolean = false,
        includeUserData: Boolean = false
    ): super(other, deep, includeUserData)
    {
        this.type = other.type
    }

    override fun cloneNode(
        deep: Boolean,
        includeUserData: Boolean
    ): TypedNode<T>
    {
        val clonedNode = TypedNode(this, deep, includeUserData)

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
