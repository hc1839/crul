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

private object Position3DAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.math.coordsys",
       |    "name": "Position3D",
       |    "fields": [
       |        { "type": "bytes", "name": "_super" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Position as a coordinate tuple in three dimensions.
 */
abstract class Position3D : Spatial {
    constructor(component1: Double, component2: Double, component3: Double):
        super(component1, component2, component3)

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
            Position3DAvsc.schema,
            avroData
        ).first()
    )

    operator fun component1() = this.components[0]
    operator fun component2() = this.components[1]
    operator fun component3() = this.components[2]

    /**
     *  Converts this position to a vector.
     */
    abstract fun toVector3D(): Vector3D

    companion object {
        /**
         *  Serializes a [Position3D] in Apache Avro.
         *
         *  @param obj
         *      [Position3D] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Position3D): ByteBuffer {
            val avroRecord = GenericData.Record(
                Position3DAvsc.schema
            )

            avroRecord.put("_super", Spatial.serialize(obj))

            return AvroSimple.serializeData<GenericRecord>(
                Position3DAvsc.schema,
                listOf(avroRecord)
            )
        }
    }
}
