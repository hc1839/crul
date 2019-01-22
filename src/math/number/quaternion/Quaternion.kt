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

package math.number.quaternion

import math.coordsys.Vector3D
import org.msgpack.core.MessagePack
import org.msgpack.value.Value
import serialize.BinarySerializable

/**
 *  Quaternion.
 *
 *  @param scalar
 *      Scalar part.
 *
 *  @param vector
 *      Vector part.
 */
open class Quaternion : BinarySerializable {
    val scalar: Double

    val vector: Vector3D

    constructor(scalar: Double, vector: Vector3D) {
        this.scalar = scalar
        this.vector = vector
    }

    /**
     *  Data-based constructor.
     */
    private constructor(ctorArgs: CtorArgs):
        this(ctorArgs.scalar, ctorArgs.vector)

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(getCtorArgs(msgpack))

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

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(2)

        packer
            .packString("scalar")
            .packDouble(scalar)

        val vectorAsBytes = vector.serialize()

        packer
            .packString("vector")
            .packBinaryHeader(vectorAsBytes.count())

        packer.writePayload(vectorAsBytes)

        packer.close()

        return packer.toByteArray()
    }

    companion object {
        /**
         *  Constructor arguments.
         */
        private data class CtorArgs(
            val scalar: Double,
            val vector: Vector3D
        )

        /**
         *  Gets the constructor arguments from [serialize].
         */
        private fun getCtorArgs(msgpack: ByteArray): CtorArgs {
            val (unpackedMap, _) = BinarySerializable
                .getMapRestPair(
                    msgpack,
                    Quaternion::class.qualifiedName!!
                )

            return CtorArgs(
                unpackedMap["scalar"]!!.asFloatValue().toDouble(),
                Vector3D(unpackedMap["vector"]!!.asBinaryValue().asByteArray())
            )
        }
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
