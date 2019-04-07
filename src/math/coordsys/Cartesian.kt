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
 *  Cartesian coordinate tuple.
 */
open class Cartesian : Position3D {
    val x: Double

    val y: Double

    val z: Double

    constructor(x: Double, y: Double, z: Double): super(x, y, z) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(position: Vector3D): this(
        position.components[0],
        position.components[1],
        position.components[2]
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(msgpack: ByteArray): super(msgpack) {
        this.x = component1()
        this.y = component2()
        this.z = component3()
    }

    override fun toVector3D() =
        Vector3D(x, y, z)

    companion object {
        /**
         *  Serializes a [Cartesian] in MessagePack.
         *
         *  @param obj
         *      [Cartesian] to serialize.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Cartesian): ByteBuffer =
            Position3D.serialize(obj)

        /**
         *  Deserializes a [Cartesian] in MessagePack.
         *
         *  @param msgpack
         *      Serialized [Cartesian] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Cartesian].
         */
        @JvmStatic
        fun deserialize(msgpack: ByteBuffer): Cartesian {
            val msgpackByteArray = ByteArray(
                msgpack.limit() - msgpack.position()
            ) {
                msgpack.get()
            }

            return Cartesian(msgpackByteArray)
        }
    }
}
