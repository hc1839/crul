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

package io.github.hc1839.crul.math.fuzzy.number

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.float.FloatCompare.nearlyEquals
import io.github.hc1839.crul.math.descriptive.EndpointInclusion
import io.github.hc1839.crul.math.descriptive.IntervalEndpoints
import io.github.hc1839.crul.math.descriptive.IntervalEndpointsSpec
import io.github.hc1839.crul.math.fuzzy.Interval
import io.github.hc1839.crul.math.fuzzy.Membership
import io.github.hc1839.crul.serialize.AvroSimple

private object TriangularFuzzyNumberAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "io.github.hc1839.crul.math.fuzzy.number",
       |    "name": "TriangularFuzzyNumber",
       |    "fields": [
       |        { "type": "double", "name": "peak" },
       |        { "type": "double", "name": "lo" },
       |        { "type": "double", "name": "hi" },
       |        { "type": "string", "name": "endpoint_inclusion" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Triangular fuzzy number (TFN).
 *
 *  @param peak
 *      Peak (not height) of the TFN.
 *
 *  @param intervalEndpoints
 *      Endpoints of the TFN.
 */
open class TriangularFuzzyNumber(
    peak: Double,
    intervalEndpoints: IntervalEndpointsSpec<Double>
) : FuzzyNumber<Double>(
        peak,
        Interval<Double>(
            intervalEndpoints,
            Membership<Double>({ element: Double ->
                listOf(
                    if (nearlyEquals(element, peak)) {
                        1.0
                    } else if (element < peak) {
                        (element - intervalEndpoints.lo) /
                        (peak - intervalEndpoints.lo)
                    } else {
                        (intervalEndpoints.hi - element) /
                        (intervalEndpoints.hi - peak)
                    },
                    0.0
                ).max()!!
            })
        )
    )
{
    /**
     *  Constructs a TFN using an isosceles triangle.
     *
     *  @param peak
     *      Peak (not height) of the TFN.
     *
     *  @param base
     *      Base length of the isosceles triangle.
     */
    constructor(peak: Double, base: Double): this(
        peak,
        IntervalEndpoints<Double>(
            peak - base / 2.0,
            peak + base / 2.0,
            EndpointInclusion.CLOSED
        )
    )

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): this(
        avroRecord.get("peak") as Double,
        IntervalEndpoints<Double>(
            avroRecord.get("lo") as Double,
            avroRecord.get("hi") as Double,
            enumValueOf<EndpointInclusion>(
                avroRecord.get("endpoint_inclusion").toString()
            )
        )
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            TriangularFuzzyNumberAvsc.schema,
            avroData
        ).first()
    )

    companion object {
        /**
         *  Serializes a [TriangularFuzzyNumber] in Apache Avro.
         *
         *  @param obj
         *      [TriangularFuzzyNumber] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: TriangularFuzzyNumber): ByteBuffer {
            val avroRecord = GenericData.Record(
                TriangularFuzzyNumberAvsc.schema
            )

            avroRecord.put("peak", obj.peak)
            avroRecord.put("lo", obj.intervalEndpoints.lo)
            avroRecord.put("hi", obj.intervalEndpoints.hi)

            avroRecord.put(
                "endpoint_inclusion",
                obj.intervalEndpoints.type.name
            )

            return AvroSimple.serializeData<GenericRecord>(
                TriangularFuzzyNumberAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [TriangularFuzzyNumber] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [TriangularFuzzyNumber] as returned by [serialize].
         *
         *  @return
         *      Deserialized [TriangularFuzzyNumber].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): TriangularFuzzyNumber =
            TriangularFuzzyNumber(avroData)
    }
}
