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

private object CartesianAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.math.coordsys",
       |    "name": "Cartesian",
       |    "fields": [
       |        { "type": "bytes", "name": "_super" }
       |    ]
       |}
        """.trimMargin()
    )
}

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
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): super(
        avroRecord.get("_super") as ByteBuffer
    ) {
        this.x = component1()
        this.y = component2()
        this.z = component3()
    }

    /**
     *  Deserialization constructor.
     */
    protected constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            CartesianAvsc.schema,
            avroData
        ).first()
    )

    override fun toVector3D() =
        Vector3D(x, y, z)

    companion object {
        /**
         *  Serializes a [Cartesian] in Apache Avro.
         *
         *  @param obj
         *      [Cartesian] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Cartesian): ByteBuffer {
            val avroRecord = GenericData.Record(
                CartesianAvsc.schema
            )

            avroRecord.put("_super", Position3D.serialize(obj))

            return AvroSimple.serializeData<GenericRecord>(
                CartesianAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [Cartesian] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Cartesian] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Cartesian].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): Cartesian =
            Cartesian(avroData)
    }
}
