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

package crul.serialize

/**
 *  Tag for a class whose instances can be serialized to a byte array.
 */
interface BinarySerializable {
    /**
     *  Serializes the object to a byte array.
     *
     *  The serialization format is specific to the class implementing this
     *  function.
     *
     *  @param args
     *      Additional arguments.
     *
     *  @return
     *      Serialization of the object as a byte array.
     */
    fun serialize(args: List<Any?>): ByteArray

    /**
     *  Serializes the object to a byte array without additional arguments.
     *
     *  Default implementation calls [serialize] with an empty list.
     *
     *  @return
     *      Serialization of the object as a byte array.
     */
    fun serialize(): ByteArray =
        serialize(listOf())
}
