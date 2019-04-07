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
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure
import crul.serialize.MessagePackSimple

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

            if (!baseUnit.isCommensurable(baseDim.siUnit)) {
                throw IllegalArgumentException(
                    "Base unit is not commensurable with the " +
                    "corresponding SI base unit of '${baseDim.name}'."
                )
            }
        }

        this.baseUnits = baseUnits
    }

    /**
     *  Initializes from a MessagePack map.
     *
     *  @param msgpack
     *      MessagePack map for the entire inheritance tree.
     *
     *  @param unpackedMap
     *      Unpacked MessagePack map that is specific to this class.
     */
    private constructor(
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray,
        unpackedMap: Map<String, Value>
    ): this(
        unpackedMap["base-units"]!!
            .asMapValue()
            .map()
            .map { (key, value) ->
                val baseDim = BaseDimension
                    .getBySymbol(key.asStringValue().toString())!!

                val baseUnit = UnitOfMeasure(
                    value.asBinaryValue().asByteArray()
                )

                Pair(baseDim, baseUnit)
            }
            .toMap()
    )

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        msgpack,
        MessagePackSimple.getInnerMap(
            msgpack,
            UnitSystem::class.qualifiedName!!
        )
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
         *  Serializes a [UnitSystem] in MessagePack.
         *
         *  @param obj
         *      [UnitSystem] to serialize.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: UnitSystem): ByteBuffer {
            val packer = MessagePack.newDefaultBufferPacker()

            packer.packMapHeader(1)

            packer
                .packString(obj::class.qualifiedName)
                .packMapHeader(1)

            packer
                .packString("base-units")
                .packMapHeader(obj.baseUnits.count())

            for ((baseDim, baseUnit) in obj.baseUnits) {
                val baseUnitAsByteBuffer = UnitOfMeasure.serialize(baseUnit)

                val baseUnitAsBytes = ByteArray(
                    baseUnitAsByteBuffer.limit() -
                    baseUnitAsByteBuffer.position()
                ) {
                    baseUnitAsByteBuffer.get()
                }

                packer
                    .packString(baseDim.symbol)
                    .packBinaryHeader(baseUnitAsBytes.count())

                packer.writePayload(baseUnitAsBytes)
            }

            packer.close()

            return ByteBuffer.wrap(packer.toByteArray())
        }

        /**
         *  Deserializes a [UnitSystem] in MessagePack.
         *
         *  @param msgpack
         *      Serialized [UnitSystem] as returned by [serialize].
         *
         *  @return
         *      Deserialized [UnitSystem].
         */
        @JvmStatic
        fun deserialize(msgpack: ByteBuffer): UnitSystem {
            val msgpackByteArray = ByteArray(
                msgpack.limit() - msgpack.position()
            ) {
                msgpack.get()
            }

            return UnitSystem(msgpackByteArray)
        }
    }
}
