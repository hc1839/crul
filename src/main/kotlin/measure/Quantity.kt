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

package crul.measure

import java.nio.ByteBuffer
import kotlin.math.pow
import org.apache.avro.Schema
import org.apache.avro.generic.*

import crul.float.FloatCompare.nearlyEquals
import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure
import crul.measure.unit.UnitSystem
import crul.serialize.AvroSimple

private object QuantityAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.measure",
       |    "name": "Quantity",
       |    "fields": [
       |        { "type": "double", "name": "value" },
       |        { "type": "bytes", "name": "unit" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Quantity containing a numerical value and a unit.
 */
class Quantity : Comparable<Quantity> {
    /**
     *  Unit of measure.
     */
    val value: Double

    /**
     *  Numerical value.
     */
    val unit: UnitOfMeasure

    constructor(value: Double, unit: UnitOfMeasure) {
        this.value = value
        this.unit = unit
    }

    /**
     *  Uses a value having a given dimension using a specific unit system.
     *
     *  The base dimensions in `dimension` are substituted by the base units in
     *  `unitSystem` to create a unit for `value`.
     */
    constructor(
        value: Double,
        dimension: Dimension,
        unitSystem: UnitSystem
    ): this(
        value,
        unitSystem.createUnit(dimension)
    )

    /**
     *  Dimensionless quantity.
     */
    constructor(value: Double): this(value, UnitOfMeasure())

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): this(
        avroRecord.get("value") as Double,
        UnitOfMeasure.deserialize(
            avroRecord.get("unit") as ByteBuffer
        )
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            QuantityAvsc.schema,
            avroData
        ).first()
    )

    /**
     *  Uses [equals] for determining equality.
     */
    override fun compareTo(other: Quantity): Int =
        when {
            this == other -> 0
            value < other.value(unit) -> -1
            else -> 1
        }

    override fun hashCode() =
        unit.hashCode()

    /**
     *  Uses [nearlyEquals] for the value component.
     */
    override fun equals(other: Any?) =
        other is Quantity &&
        this::class == other::class &&
        (
            nearlyEquals(value, other.value(unit)) &&
            unit == other.unit
        )

    /**
     *  Numerical value in a given unit.
     */
    fun value(unit: UnitOfMeasure) =
        value * this.unit.relativeMagnitude(unit)

    /**
     *  Numerical value as having `dimension` using base units from
     *  `unitSystem`.
     *
     *  The unit that results from `dimension` and `unitSystem` must be
     *  commensurable with the unit of this quantity.
     */
    fun value(dimension: Dimension, unitSystem: UnitSystem) =
        value(unitSystem.createUnit(dimension))

    /**
     *  Whether the unit of this quantity is commensurable with the unit of
     *  another quantity.
     */
    fun isCommensurableWith(other: Quantity) =
        unit.isCommensurableWith(other.unit)

    /**
     *  Whether the unit of this quantity is commensurable with a given unit.
     */
    fun isCommensurableWith(unit: UnitOfMeasure) =
        unit.isCommensurableWith(this.unit)

    /**
     *  `n`-th power.
     */
    fun pow(n: Int) =
        Quantity(value.pow(n), unit.pow(n))

    /**
     *  `n`-th root.
     */
    fun root(n: Int) =
        Quantity(value.pow(1.0 / n), unit.root(n))

    /**
     *  Absolute value of this quantity.
     */
    fun abs() =
        Quantity(kotlin.math.abs(value), unit)

    operator fun plus(other: Quantity) =
        Quantity(value + other.value(unit), unit)

    operator fun unaryMinus() =
        Quantity(-value, unit)

    operator fun minus(other: Quantity) =
        this + -other

    operator fun times(other: Double) =
        Quantity(value * other, unit)

    operator fun times(other: Quantity) =
        Quantity(value * other.value, unit * other.unit)

    operator fun div(other: Double) =
        this * (1.0 / other)

    operator fun div(other: Quantity) =
        Quantity(value * 1.0 / other.value, unit / other.unit)

    companion object {
        /**
         *  Converts a value as having `dimension` using `fromUnitSystem` to
         *  `toUnitSystem`.
         */
        @JvmStatic
        fun convertUnit(
            value: Double,
            asDimension: Dimension,
            fromUnitSystem: UnitSystem,
            toUnitSystem: UnitSystem
        ): Double
        {
            return Quantity(value, asDimension, fromUnitSystem)
                .value(asDimension, toUnitSystem)
        }

        /**
         *  Converts a value as having `fromUnit` to `toUnit`.
         */
        @JvmStatic
        fun convertUnit(
            value: Double,
            fromUnit: UnitOfMeasure,
            toUnit: UnitOfMeasure
        ): Double
        {
            return Quantity(value, fromUnit).value(toUnit)
        }

        /**
         *  Converts a value as having `fromUnit` to as having `asDimension`
         *  using `toUnitSystem`.
         */
        @JvmStatic
        fun convertUnit(
            value: Double,
            fromUnit: UnitOfMeasure,
            asDimension: Dimension,
            toUnitSystem: UnitSystem
        ): Double
        {
            return Quantity(value, fromUnit).value(asDimension, toUnitSystem)
        }

        /**
         *  Converts a value as having `asDimension` using `fromUnitSystem` to
         *  a new unit, `toUnit`.
         */
        @JvmStatic
        fun convertUnit(
            value: Double,
            asDimension: Dimension,
            fromUnitSystem: UnitSystem,
            toUnit: UnitOfMeasure
        ): Double
        {
            return Quantity(value, asDimension, fromUnitSystem).value(toUnit)
        }

        /**
         *  Serializes a [Quantity] in Apache Avro.
         *
         *  @param obj
         *      [Quantity] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Quantity): ByteBuffer {
            val avroRecord = GenericData.Record(
                QuantityAvsc.schema
            )

            avroRecord.put("value", obj.value)

            avroRecord.put(
                "unit",
                UnitOfMeasure.serialize(obj.unit)
            )

            return AvroSimple.serializeData<GenericRecord>(
                QuantityAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [Quantity] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Quantity] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Quantity].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): Quantity =
            Quantity(avroData)
    }
}
