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

package crul.measure.unit

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure
import crul.serialize.AvroSimple

private object UnitSystemAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.measure.unit",
       |    "name": "UnitSystem",
       |    "fields": [
       |        {
       |            "type": { "type": "map", "values": "bytes" },
       |            "name": "base_units"
       |        }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Immutable system of units.
 */
class UnitSystem {
    /**
     *  Base units associated by base dimensions.
     */
    private val baseUnits: Map<BaseDimension, UnitOfMeasure>

    /**
     *  @param baseUnits
     *      Base units to use for each base dimension. Base units for all base
     *      dimensions must be specified and must be commensurable with the
     *      corresponding SI base unit.
     */
    constructor(baseUnits: Map<BaseDimension, UnitOfMeasure>) {
        for (baseDim in enumValues<BaseDimension>()) {
            val baseUnit = baseUnits[baseDim]

            if (baseUnit == null) {
                throw IllegalArgumentException(
                    "Missing base dimension: ${baseDim.name}"
                )
            }

            if (!baseUnit.isCommensurableWith(baseDim.siUnit)) {
                throw IllegalArgumentException(
                    "Base unit is not commensurable with the " +
                    "corresponding SI base unit of '${baseDim.name}'."
                )
            }
        }

        this.baseUnits = baseUnits
    }

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): this(
        @Suppress("UNCHECKED_CAST") (
            avroRecord.get("base_units") as Map<*, ByteBuffer>
        )
        .map { (baseDimSymbol, baseUnitBuf) ->
            Pair(
                BaseDimension.getBySymbol(baseDimSymbol.toString())!!,
                UnitOfMeasure.deserialize(baseUnitBuf)
            )
        }
        .toMap()
    )

    /**
     *  Deserialization constructor.
     */
    constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            UnitSystemAvsc.schema,
            avroData
        ).first()
    )

    override fun hashCode(): Int =
        baseUnits.hashCode()

    override fun equals(other: Any?): Boolean =
        other is UnitSystem &&
        this::class == other::class &&
        (
            baseUnits == other.baseUnits
        )

    /**
     *  Gets the base unit that is associated by a given base dimension.
     *
     *  @param baseDimension
     *      Base dimension for which the base unit is to be retrieved.
     *
     *  @return
     *      Base unit associated by `baseDimension`.
     */
    fun getBaseUnit(baseDimension: BaseDimension): UnitOfMeasure =
        baseUnits[baseDimension]!!

    /**
     *  Creates a unit by raising each base unit in this unit system to the
     *  power specified by `dimension`.
     *
     *  Effectively, it is the substitution of each base dimension in
     *  `dimension` with the corresponding base unit in this unit system.
     */
    fun createUnit(dimension: Dimension): UnitOfMeasure =
        dimension
            .exponents
            .map { (baseDim, exp) ->
                baseUnits[baseDim]!!.pow(exp)
            }
            .reduce { acc, item -> acc * item }

    companion object {
        /**
         *  Serializes a [UnitSystem] in Apache Avro.
         *
         *  @param obj
         *      [UnitSystem] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: UnitSystem): ByteBuffer {
            val avroRecord = GenericData.Record(
                UnitSystemAvsc.schema
            )

            avroRecord.put(
                "base_units",
                obj.baseUnits.map { (baseDim, baseUnit) ->
                    Pair(
                        baseDim.symbol,
                        UnitOfMeasure.serialize(baseUnit)
                    )
                }.toMap()
            )

            return AvroSimple.serializeData<GenericRecord>(
                UnitSystemAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [UnitSystem] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [UnitSystem] as returned by [serialize].
         *
         *  @return
         *      Deserialized [UnitSystem].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): UnitSystem =
            UnitSystem(avroData)
    }
}
