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

package io.github.hc1839.crul.serialize

/**
 *  Serializer of arbitrary types in arbitrary formats.
 *
 *  @param T
 *      Type being serialized.
 *
 *  @param F
 *      Format of serialization.
 */
interface Serializer<T, F> {
    /**
     *  Serializes an object.
     *
     *  @param obj
     *      Object to serialize.
     *
     *  @param args
     *      Additional arguments, if needed, for the serialization.
     *
     *  @return
     *      Serialized object in a data format.
     */
    fun serialize(obj: T, vararg args: Any?): F

    /**
     *  Deserializes an object.
     *
     *  @param dataFormat
     *      Data format to deserialize an object from.
     *
     *  @param args
     *      Additional arguments, if needed, for the deserialization.
     *
     *  @return
     *      Deserialized object from `dataFormat`.
     */
    fun deserialize(dataFormat: F, vararg args: Any?): T
}
