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

package visaccess

/**
 *  General access interface.
 */
interface Access

/**
 *  Friend access.
 */
interface FriendAccess : Access

/**
 *  Base class for access key.
 */
abstract class AccessKey() {
    /**
     *  Key as a string.
     *
     *  This is used as part of the verification of whether two access keys are
     *  the same.
     */
    abstract val keyString: String

    abstract override fun hashCode(): Int

    abstract override fun equals(other: Any?): Boolean
}

/**
 *  Access key for friendship.
 *
 *  @param keyString
 *      String to use as part of comparison purposes.
 */
open class FriendKey(
    override val keyString: String = uuid.Generator.inNCName()
) : AccessKey() {
    init {
        if (!xml.Datatype.isNCName(keyString)) {
            throw IllegalArgumentException(
                "Key string does not conform to XML NCName production: " +
                keyString
            )
        }
    }

    override fun hashCode() =
        keyString.hashCode()

    /**
     *  Two friend keys are equal if they have the same `keyString`.
     */
    override fun equals(other: Any?) =
        other is FriendKey &&
        this::class == other::class &&
        (
            keyString == other.keyString
        )
}
