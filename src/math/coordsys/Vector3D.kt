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
import org.apache.avro.Schema
import org.apache.avro.generic.*

import crul.serialize.AvroSimple

private object Vector3DAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.math.coordsys",
       |    "name": "Vector3D",
       |    "fields": [
       |        { "type": "bytes", "name": "_super" }
       |    ]
       |}
        """.trimMargin()
    )
}

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
     *  @param components
     *      List of exactly three components.
     */
    constructor(
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
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): super(
        avroRecord.get("_super") as ByteBuffer
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            Vector3DAvsc.schema,
            avroData
        ).first()
    )

    operator fun component1() = this.components[0]

    operator fun component2() = this.components[1]

    operator fun component3() = this.components[2]

    override fun unit(): Vector3D {
        val superValue = super.unit()

        return Vector3D(superValue.components)
    }

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
         *  Serializes a [Vector3D] in Apache Avro.
         *
         *  @param obj
         *      [Vector3D] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Vector3D): ByteBuffer {
            val avroRecord = GenericData.Record(
                Vector3DAvsc.schema
            )

            avroRecord.put("_super", Vector.serialize(obj))

            return AvroSimple.serializeData<GenericRecord>(
                Vector3DAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [Vector3D] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Vector3D] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Vector3D].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): Vector3D =
            Vector3D(avroData)
    }
}
