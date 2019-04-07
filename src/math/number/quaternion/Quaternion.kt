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

package crul.math.number.quaternion

import java.nio.ByteBuffer
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.math.coordsys.Vector3D
import crul.serialize.MessagePackSimple

/**
 *  Quaternion.
 *
 *  @param scalar
 *      Scalar part.
 *
 *  @param vector
 *      Vector part.
 */
open class Quaternion {
    val scalar: Double

    val vector: Vector3D

    constructor(scalar: Double, vector: Vector3D) {
        this.scalar = scalar
        this.vector = vector
    }

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
        unpackedMap: Map<String, Value>,
        vector3DDeserializer: (ByteBuffer) -> Vector3D
    ): this(
        unpackedMap["scalar"]!!.asFloatValue().toDouble(),
        vector3DDeserializer.invoke(
            ByteBuffer.wrap(
                unpackedMap["vector"]!!.asBinaryValue().asByteArray()
            )
        )
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(
        msgpack: ByteArray,
        vector3DDeserializer: (ByteBuffer) -> Vector3D
    ): this(
        msgpack,
        MessagePackSimple.getInnerMap(
            msgpack,
            Quaternion::class.qualifiedName!!
        ),
        vector3DDeserializer
    )

    /**
     *  Conjugate.
     */
    fun conjugate() =
        Quaternion(scalar, -vector)

    operator fun component1() =
        scalar

    operator fun component2() =
        vector

    open operator fun plus(other: Quaternion) =
        Quaternion(
            scalar + other.scalar,
            vector + other.vector
        )

    open operator fun plus(other: Double) =
        Quaternion(
            scalar + other,
            vector
        )

    open operator fun unaryMinus() =
        Quaternion(-scalar, -vector)

    open operator fun minus(other: Quaternion) =
        Quaternion(
            scalar - other.scalar,
            vector - other.vector
        )

    open operator fun minus(other: Double) =
        Quaternion(
            scalar - other,
            vector
        )

    open operator fun times(other: Double) =
        Quaternion(
            scalar * other,
            vector * other
        )

    open operator fun times(other: Quaternion) =
        other * scalar + vector
            .components
            .mapIndexed { idx, cmpt ->
                when (idx) {
                    0 -> I * other * cmpt
                    1 -> J * other * cmpt
                    2 -> K * other * cmpt
                    else -> throw RuntimeException(
                        "Not a three-dimensional vector."
                    )
                }
            }
            .reduce { acc, item -> acc + item }

    companion object {
        /**
         *  Serializes a [Quaternion] in MessagePack.
         *
         *  @param obj
         *      [Quaternion] to serialize.
         *
         *  @param vector3DSerializer
         *      [Vector3D] serializer.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(
            obj: Quaternion,
            vector3DSerializer: (Vector3D) -> ByteBuffer
        ): ByteBuffer {
            val packer = MessagePack.newDefaultBufferPacker()

            packer.packMapHeader(1)

            packer
                .packString(obj::class.qualifiedName)
                .packMapHeader(2)

            packer
                .packString("scalar")
                .packDouble(obj.scalar)

            val vectorAsByteBuffer = vector3DSerializer
                .invoke(obj.vector)

            val vectorAsBytes = ByteArray(
                vectorAsByteBuffer.limit() -
                vectorAsByteBuffer.position()
            ) {
                vectorAsByteBuffer.get()
            }

            packer
                .packString("vector")
                .packBinaryHeader(vectorAsBytes.count())

            packer.writePayload(vectorAsBytes)

            packer.close()

            return ByteBuffer.wrap(packer.toByteArray())
        }

        /**
         *  Serializes a [Quaternion] in MessagePack using the default
         *  [Vector3D] serializer.
         *
         *  @param obj
         *      [Quaternion] to serialize.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Quaternion): ByteBuffer =
            serialize(obj, Vector3D.Companion::serialize)

        /**
         *  Deserializes a [Quaternion] in MessagePack.
         *
         *  @param msgpack
         *      Serialized [Quaternion] as returned by [serialize].
         *
         *  @param vector3DDeserializer
         *      [Vector3D] deserializer.
         *
         *  @return
         *      Deserialized [Quaternion].
         */
        @JvmStatic
        fun deserialize(
            msgpack: ByteBuffer,
            vector3DDeserializer: (ByteBuffer) -> Vector3D
        ): Quaternion {
            val msgpackByteArray = ByteArray(
                msgpack.limit() - msgpack.position()
            ) {
                msgpack.get()
            }

            return Quaternion(msgpackByteArray, vector3DDeserializer)
        }

        /**
         *  Deserializes a [Quaternion] in MessagePack using the default
         *  [Vector3D] deserializer.
         *
         *  @param msgpack
         *      Serialized [Quaternion] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Quaternion].
         */
        @JvmStatic
        fun deserialize(msgpack: ByteBuffer): Quaternion =
            deserialize(msgpack, Vector3D.Companion::deserialize)
    }
}

/**
 *  *i* quaternion unit.
 */
private object I {
    operator fun times(other: Quaternion) = Quaternion(
        -other.vector[0],
        Vector3D(
            other.scalar,
            -other.vector[2],
            other.vector[1]
        )
    )
}

/**
 *  *j* quaternion unit.
 */
private object J {
    operator fun times(other: Quaternion) = Quaternion(
        -other.vector[1],
        Vector3D(
            other.vector[2],
            other.scalar,
            -other.vector[0]
        )
    )
}

/**
 *  *k* quaternion unit.
 */
private object K {
    operator fun times(other: Quaternion) = Quaternion(
        -other.vector[2],
        Vector3D(
            -other.vector[1],
            other.vector[0],
            other.scalar
        )
    )
}
