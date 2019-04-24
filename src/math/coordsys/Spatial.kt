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

private object SpatialAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.math.coordsys",
       |    "name": "Spatial",
       |    "fields": [
       |        {
       |            "type": { "type": "array", "items": "double" },
       |            "name": "components"
       |        }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Base class for representing a coordinate tuple or a vector.
 */
open class Spatial {
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
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): this(
        @Suppress("UNCHECKED_CAST") (
            avroRecord.get("components") as List<Double>
        )
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            SpatialAvsc.schema,
            avroData
        ).first()
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

    companion object {
        /**
         *  Serializes a [Spatial] in Apache Avro.
         *
         *  @param obj
         *      [Spatial] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Spatial): ByteBuffer {
            val avroRecord = GenericData.Record(
                SpatialAvsc.schema
            )

            avroRecord.put("components", obj.components)

            return AvroSimple.serializeData<GenericRecord>(
                SpatialAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [Spatial] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Spatial] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Spatial].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): Spatial =
            Spatial(avroData)
    }
}
