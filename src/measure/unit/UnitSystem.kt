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
        siBaseUnits.toMutableMap()

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
                    val baseDim = enumValueOf<BaseDimension>(
                        key.asStringValue().toString()
                    )
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
                .packString(baseDim.name)
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
        if (!baseUnit.isCommensurable(siBaseUnits[baseDimension]!!)) {
            throw IllegalArgumentException(
                "New base unit is not commensurable with the " +
                "corresponding SI base unit of '${baseDimension.name}'."
            )
        }

        _baseUnits[baseDimension] = baseUnit

        return this
    }

    /**
     *  Sets the base unit of a base dimension using its symbol.
     *
     *  @param baseDimensionSymbol
     *      Symbol of the base dimension whose base unit is to be set.
     *
     *  @param baseUnit
     *      New base unit for `baseDimension`. It must be commensurable with
     *      the corresponding SI base unit.
     *
     *  @return
     *      `this`.
     */
    fun set(
        baseDimensionSymbol: String,
        baseUnit: UnitOfMeasure
    ): UnitSystem
    {
        set(enumValueOf<BaseDimension>(baseDimensionSymbol), baseUnit)

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

    companion object {
        /**
         *  SI base units associated by base dimensions.
         */
        private val siBaseUnits: Map<BaseDimension, UnitOfMeasure> by lazy {
            enumValues<BaseDimension>()
                .asIterable()
                .associateWith {
                    when (it) {
                        BaseDimension.L ->
                            UnitOfMeasure("m")

                        BaseDimension.M ->
                            UnitOfMeasure.parse("kg")

                        BaseDimension.T ->
                            UnitOfMeasure("s")

                        BaseDimension.I ->
                            UnitOfMeasure("A")

                        BaseDimension.Th ->
                            UnitOfMeasure("K")

                        BaseDimension.N ->
                            UnitOfMeasure("mol")

                        BaseDimension.J ->
                            UnitOfMeasure("cd")
                    }
                }
                .toMap()
        }
    }
}
