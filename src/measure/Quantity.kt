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

import kotlin.math.pow
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure
import crul.measure.unit.UnitSystem
import crul.serialize.BinarySerializable
import crul.serialize.MessagePackConv

/**
 *  Quantity containing a numerical value and a unit.
 */
class Quantity : Comparable<Quantity>, BinarySerializable {
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
     *  Initializes from a MessagePack map.
     *
     *  @param unpackedMap
     *      Unpacked MessagePack map that is specific to this class.
     *
     *  @param msgpack
     *      MessagePack map for the entire inheritance tree.
     */
    private constructor(
        unpackedMap: Map<String, Value>,
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray
    ): this(
        unpackedMap["value"]!!.asFloatValue().toDouble(),
        UnitOfMeasure(
            unpackedMap["unit"]!!.asBinaryValue().asByteArray()
        )
    )

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        MessagePackConv.getInnerMap(
            msgpack,
            Quantity::class.qualifiedName!!
        ),
        msgpack
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
     *  Uses [float.Comparison.nearlyEquals] for the value component.
     */
    override fun equals(other: Any?) =
        other is Quantity &&
        this::class == other::class &&
        (
            crul.float.Comparison.nearlyEquals(
                value,
                other.value(unit)
            ) &&
            unit == other.unit
        )

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(2)

        packer
            .packString("value")
            .packDouble(value)

        val unitAsBytes = unit.serialize()

        packer
            .packString("unit")
            .packBinaryHeader(unitAsBytes.count())

        packer.writePayload(unitAsBytes)

        packer.close()

        return packer.toByteArray()
    }

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
     *  Whether the unit of `other` is commensurable with the unit of this
     *  quantity.
     */
    fun isCommensurable(other: Quantity) =
        unit.isCommensurable(other.unit)

    /**
     *  Whether `unit` is commensurable with the unit of this quantity.
     */
    fun isCommensurable(unit: UnitOfMeasure) =
        unit.isCommensurable(this.unit)

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
    }
}
