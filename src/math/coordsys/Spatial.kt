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

package crul.math.coordsys

import java.nio.ByteBuffer
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.serialize.MessagePackSimple

/**
 *  Base class for representing a coordinate tuple or a vector.
 */
open class Spatial {
    /**
     *  Components of the coordinate tuple or vector as a backing property.
     */
    protected val _components: MutableList<Double>

    /**
     *  @param components
     *      Components of the coordinate tuple or vector.
     */
    constructor(components: List<Double>) {
        this._components = components.toMutableList()
    }

    constructor(vararg components: Double): this(components.toList())

    /**
     *  Initializes from a MessagePack map.
     *
     *  @param msgpack
     *      MessagePack map for the entire inheritance tree.
     *
     *  @param unpackedMap
     *      Unpacked MessagePack map that is specific to this class.
     */
    private constructor(
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray,
        unpackedMap: Map<String, Value>
    ): this(
        unpackedMap["components"]!!
            .asArrayValue()
            .list()
            .map { it.asFloatValue().toDouble() }
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(msgpack: ByteArray): this(
        msgpack,
        MessagePackSimple.getInnerMap(
            msgpack,
            Spatial::class.qualifiedName!!
        )
    )

    /**
     *  Components of the coordinate tuple or vector.
     */
    val components: List<Double>
        get() = _components.toList()

    /**
     *  Number of dimensions.
     */
    val dimensionality: Int
        get() = components.count()

    /**
     *  Gets the component at a given zero-based index.
     */
    operator fun get(index: Int) =
        components[index]

    companion object {
        /**
         *  Serializes a [Spatial] in MessagePack.
         *
         *  @param obj
         *      [Spatial] to serialize.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Spatial): ByteBuffer {
            val packer = MessagePack.newDefaultBufferPacker()

            packer.packMapHeader(1)

            packer
                .packString(obj::class.qualifiedName)
                .packMapHeader(1)

            val componentsBuf = obj.components

            packer
                .packString("components")
                .packArrayHeader(componentsBuf.count())

            for (cmpt in componentsBuf) {
                packer.packDouble(cmpt)
            }

            packer.close()

            return ByteBuffer.wrap(packer.toByteArray())
        }

        /**
         *  Deserializes a [Spatial] in MessagePack.
         *
         *  @param msgpack
         *      Serialized [Spatial] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Spatial].
         */
        @JvmStatic
        fun deserialize(msgpack: ByteBuffer): Spatial {
            val msgpackByteArray = ByteArray(
                msgpack.limit() - msgpack.position()
            ) {
                msgpack.get()
            }

            return Spatial(msgpackByteArray)
        }
    }
}
