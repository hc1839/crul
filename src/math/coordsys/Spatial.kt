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

import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.serialize.BinarySerializable
import crul.serialize.MessagePackSimple

/**
 *  Base class for representing a coordinate tuple or a vector.
 */
open class Spatial : BinarySerializable {
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
     *  @param unpackedMap
     *      Unpacked MessagePack map that is specific to this class.
     *
     *  @param msgpack
     *      MessagePack map for the entire inheritance tree.
     */
    private constructor(
        unpackedMap: Map<String, Value>,
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray
    ): this(
        unpackedMap["components"]!!
            .asArrayValue()
            .list()
            .map { it.asFloatValue().toDouble() }
    )

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        MessagePackSimple.getInnerMap(
            msgpack,
            Spatial::class.qualifiedName!!
        ),
        msgpack
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

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(1)

        val componentsBuf = components

        packer
            .packString("components")
            .packArrayHeader(componentsBuf.count())

        for (cmpt in componentsBuf) {
            packer.packDouble(cmpt)
        }

        packer.close()

        return packer.toByteArray()
    }
}
