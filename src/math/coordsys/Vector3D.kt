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

/**
 *  Vector in three dimensions.
 */
open class Vector3D : Vector {
    constructor(
        component1: Double,
        component2: Double,
        component3: Double
    ): super(
        listOf(component1, component2, component3)
    )

    /**
     *  Convenience constructor for operator overloading that needs to return
     *  an instance of a subclass.
     */
    protected constructor(
        components: List<Double>
    ): this(
        components[0],
        components[1],
        components[2]
    ) {
        if (components.count() != 3) {
            throw IllegalArgumentException(
                "Not a three-dimensional vector."
            )
        }
    }

    /**
     *  Deserialization constructor.
     */
    protected constructor(msgpack: ByteArray): super(msgpack)

    operator fun component1() = this.components[0]
    operator fun component2() = this.components[1]
    operator fun component3() = this.components[2]

    override fun plus(other: Vector): Vector3D =
        Vector3D(super.plus(other).components)

    override fun unaryMinus(): Vector3D =
        Vector3D(super.unaryMinus().components)

    override fun minus(other: Vector): Vector3D =
        Vector3D(super.minus(other).components)

    override fun times(other: Double): Vector3D =
        Vector3D(super.times(other).components)

    override fun div(other: Double): Vector3D =
        Vector3D(super.div(other).components)

    companion object {
        /**
         *  Serializes a [Vector3D] in MessagePack.
         *
         *  @param obj
         *      [Vector3D] to serialize.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Vector3D): ByteBuffer =
            Vector.serialize(obj)

        /**
         *  Deserializes a [Vector3D] in MessagePack.
         *
         *  @param msgpack
         *      Serialized [Vector3D] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Vector3D].
         */
        @JvmStatic
        fun deserialize(msgpack: ByteBuffer): Vector3D {
            val msgpackByteArray = ByteArray(
                msgpack.limit() - msgpack.position()
            ) {
                msgpack.get()
            }

            return Vector3D(msgpackByteArray)
        }
    }
}
