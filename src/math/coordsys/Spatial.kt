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

package math.coordsys

import org.msgpack.core.MessagePack
import org.msgpack.value.Value
import serialize.BinarySerializable

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
    constructor(components: Iterable<Double>) {
        this._components = components.toMutableList()
    }

    constructor(vararg components: Double): this(components.asIterable())

    /**
     *  Data-based constructor.
     */
    private constructor(ctorArgs: CtorArgs): this(ctorArgs.components)

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(getCtorArgs(msgpack))

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

    companion object {
        /**
         *  Constructor arguments.
         */
        private class CtorArgs(val components: List<Double>)

        /**
         *  Gets the constructor arguments from [serialize].
         */
        private fun getCtorArgs(msgpack: ByteArray): CtorArgs {
            val (unpackedMap, _) = BinarySerializable
                .getMapRestPair(
                    msgpack,
                    Spatial::class.qualifiedName!!
                )

            return CtorArgs(
                unpackedMap["components"]!!
                    .asArrayValue()
                    .list()
                    .map { it.asFloatValue().toDouble() }
            )
        }
    }
}
