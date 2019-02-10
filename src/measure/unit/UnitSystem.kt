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

package measure.unit

import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import measure.dimension.BaseDimension
import measure.dimension.Dimension
import measure.dimension.parse.Production
import measure.unit.UnitOfMeasure
import serialize.BinarySerializable

/**
 *  System of units.
 */
class UnitSystem : BinarySerializable {
    /**
     *  Base units associated by base dimensions as a backing property.
     */
    private val _baseUnits: MutableMap<BaseDimension, UnitOfMeasure> =
        enumValues<BaseDimension>()
            .asIterable()
            .associateWith { it.siUnit }
            .toMutableMap()

    /**
     *  Constructs a unit system initialized with SI base units. Use [set] to
     *  modify the base units afterwards if needed.
     */
    constructor()

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
    ) {
        this._baseUnits.putAll(
            unpackedMap["base-units"]!!
                .asMapValue()
                .map()
                .map { (key, value) ->
                    val baseDim = BaseDimension.getBySymbol(
                        key.asStringValue().toString()
                    )!!

                    val baseUnit = UnitOfMeasure(
                        value.asBinaryValue().asByteArray()
                    )

                    Pair(baseDim, baseUnit)
                }
                .toMap()
        )
    }

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        BinarySerializable.getInnerMap(
            msgpack,
            UnitSystem::class.qualifiedName!!
        ),
        msgpack
    )

    /**
     *  Base units associated by base dimensions.
     *
     *  Base units for all base dimensions are returned.
     */
    val baseUnits: Map<BaseDimension, UnitOfMeasure>
        get() = _baseUnits.toMap()

    override fun hashCode(): Int =
        baseUnits.hashCode()

    override fun equals(other: Any?): Boolean =
        other is UnitSystem &&
        this::class == other::class &&
        (
            baseUnits == other.baseUnits
        )

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(1)

        packer
            .packString("base-units")
            .packMapHeader(_baseUnits.count())

        for ((baseDim, baseUnit) in _baseUnits) {
            val baseUnitAsBytes = baseUnit.serialize()

            packer
                .packString(baseDim.symbol)
                .packBinaryHeader(baseUnitAsBytes.count())

            packer.writePayload(baseUnitAsBytes)
        }

        packer.close()

        return packer.toByteArray()
    }

    /**
     *  Sets the base unit of a base dimension.
     *
     *  @param baseDimension
     *      Base dimension whose base unit is to be set.
     *
     *  @param baseUnit
     *      New base unit for `baseDimension`. It must be commensurable with
     *      the corresponding SI base unit.
     *
     *  @return
     *      `this`.
     */
    fun set(
        baseDimension: BaseDimension,
        baseUnit: UnitOfMeasure
    ): UnitSystem
    {
        if (!baseUnit.isCommensurable(baseDimension.siUnit)) {
            throw IllegalArgumentException(
                "New base unit is not commensurable with the " +
                "corresponding SI base unit of '$baseDimension'."
            )
        }

        _baseUnits[baseDimension] = baseUnit

        return this
    }

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
                _baseUnits[baseDim]!!.pow(exp)
            }
            .reduce { acc, item -> acc * item }
}
