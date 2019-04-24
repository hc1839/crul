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

private object SphericalAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.math.coordsys",
       |    "name": "Spherical",
       |    "fields": [
       |        { "type": "bytes", "name": "_super" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Spherical coordinate tuple in ISO convention.
 */
open class Spherical : Position3D {
    val radius: Double

    val polar: Double

    val azimuthal: Double

    constructor(radius: Double, polar: Double, azimuthal: Double):
        super(radius, polar, azimuthal)
    {
        this.radius = radius
        this.polar = polar
        this.azimuthal = azimuthal
    }

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): super (
        avroRecord.get("_super") as ByteBuffer
    ) {
        this.radius = component1()
        this.polar = component2()
        this.azimuthal = component3()
    }

    /**
     *  Deserialization constructor.
     */
    protected constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            SphericalAvsc.schema,
            avroData
        ).first()
    )

    /**
     *  Converts the spherical coordinates to a position vector in Cartesian
     *  coordinates.
     */
    override fun toVector3D() =
        Vector3D(
            radius *
                kotlin.math.sin(polar) *
                kotlin.math.cos(azimuthal),
            radius *
                kotlin.math.sin(polar) *
                kotlin.math.sin(azimuthal),
            radius *
                kotlin.math.cos(polar)
        )

    companion object {
        /**
         *  Serializes a [Spherical] in Apache Avro.
         *
         *  @param obj
         *      [Spherical] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Spherical): ByteBuffer {
            val avroRecord = GenericData.Record(
                SphericalAvsc.schema
            )

            avroRecord.put("_super", Position3D.serialize(obj))

            return AvroSimple.serializeData<GenericRecord>(
                SphericalAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [Spherical] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Spherical] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Spherical].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): Spherical =
            Spherical(avroData)
    }
}
