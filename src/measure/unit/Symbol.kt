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

import com.google.gson.Gson
import java.io.File
import kotlin.math.pow
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import measure.dimension.BaseDimension
import measure.dimension.Dimension
import measure.unit.BaseUnit
import serialize.BinarySerializable

/**
 *  Storage information for UCUM symbols.
 */
private object UcumSymbolStore {
    /**
     *  Path to the JSON file containing UCUM symbols with respect to the JAR
     *  resource.
     */
    val path: String = "/measure/unit/ucum-symbols.json"

    /**
     *  JSON of UCUM symbols parsed by Gson.
     */
    val json: Map<String, Any> by lazy {
        val gson = Gson()

        @Suppress("UNCHECKED_CAST")
        gson.fromJson(
            this::class.java.getResourceAsStream(path).reader(),
            Map::class.java
        ) as Map<String, Any>
    }
}

/**
 *  Prefix for a metric unit according to the Unified Code for Units of Measure
 *  (UCUM).
 *
 *  To instantiate this class, use [create].
 */
class UnitPrefix : BinarySerializable {
    /**
     *  UCUM c/s symbol of the prefix.
     */
    val cs: String

    /**
     *  Value of the prefix.
     */
    val value: Double

    private constructor(cs: String, value: Double) {
        this.cs = cs
        this.value = value
    }

    override fun hashCode(): Int =
        cs.hashCode()

    /**
     *  Uses [float.Comparison.nearlyEquals] for the value component.
     */
    override fun equals(other: Any?): Boolean =
        other is UnitPrefix &&
        this::class == other::class &&
        (
            cs == other.cs &&
            float.Comparison.nearlyEquals(
                value,
                other.value
            )
        )

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
        unpackedMap["cs"]!!.asStringValue().toString(),
        unpackedMap["value"]!!.asFloatValue().toDouble()
    )

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        BinarySerializable.getInnerMap(
            msgpack,
            UnitPrefix::class.qualifiedName!!
        ),
        msgpack
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
            .packString("cs")
            .packString(cs)

        packer
            .packString("value")
            .packDouble(value)

        packer.close()

        return packer.toByteArray()
    }

    /**
     *  @param other
     *      Unit to be prepended with this prefix. Must be a metric unit.
     */
    operator fun times(other: UnitOfMeasure): UnitOfMeasure {
        if (!other.isMetric) {
            throw IllegalArgumentException(
                "Unit to be prepended with this prefix is not metric."
            )
        }

        return other * value
    }

    companion object {
        /**
         *  JSON of UCUM prefixes parsed by Gson.
         */
        private val prefixes: Map<String, Map<String, Any>> =
            @Suppress("UNCHECKED_CAST") (
                UcumSymbolStore.json["prefixes"]!!
                    as Map<String, Map<String, Any>>
            )

        /**
         *  Creates a [UnitPrefix] that has the UCUM c/s symbol, `cs`.
         *
         *  @param cs
         *      UCUM c/s symbol. It must exist in the JSON file that contains
         *      the predefined UCUM symbols.
         */
        @JvmStatic
        fun create(cs: String): UnitPrefix {
            if (!prefixes.containsKey(cs)) {
                throw IllegalArgumentException(
                    "Unknown prefix: $cs"
                )
            }

            return UnitPrefix(
                cs,
                prefixes[cs]!!["value"]!! as Double
            )
        }
    }
}

/**
 *  Unit of measure according to the Unified Code for Units of Measure (UCUM).
 *
 *  To instantiate this class with a derived unit, use [create].
 *
 *  Operations that yield new a unit assume that any unit that is not a base
 *  unit is not metric, which is conservative but not always accurate.
 */
class UnitOfMeasure : BinarySerializable {
    /**
     *  Magnitude.
     */
    var magnitude: Double
        private set

    /**
     *  Whether this unit can be prefixed by [UnitPrefix].
     */
    var isMetric: Boolean
        private set

    /**
     *  Base units associated with their exponents as a backing property.
     */
    private val _dimension: MutableMap<BaseUnit, Int> = mutableMapOf()

    /**
     *  Base units associated with their exponents.
     *
     *  Only entries where the exponent is non-zero is returned.
     */
    val dimension: Map<BaseUnit, Int>
        get() = _dimension.filter { (_, exp) -> exp != 0 }

    /**
     *  Base unit raised to the power of 1.
     *
     *  @param baseUnit
     *      Base unit to construct. It must exist in the JSON file that
     *      contains the predefined UCUM symbols.
     */
    constructor(baseUnit: BaseUnit) {
        magnitude = 1.0
        _dimension[baseUnit] = 1
        isMetric = true
    }

    /**
     *  Dimensionless unit.
     */
    constructor() {
        magnitude = 1.0
        isMetric = false
    }

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
        this.magnitude =
            unpackedMap["magnitude"]!!.asFloatValue().toDouble()

        this._dimension.putAll(
            unpackedMap["dimension"]!!
                .asMapValue()
                .map()
                .map { (key, value) ->
                    val baseUnit = enumValueOf<BaseUnit>(
                        key.asStringValue().toString()
                    )
                    val exp = value.asIntegerValue().toInt()

                    Pair(baseUnit, exp)
                }
                .toMap()
        )

        this.isMetric =
            unpackedMap["is-metric"]!!.asBooleanValue().boolean
    }

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        BinarySerializable.getInnerMap(
            msgpack,
            UnitOfMeasure::class.qualifiedName!!
        ),
        msgpack
    )

    /**
     *  Whether `other` is commensurable with this unit.
     */
    fun isCommensurable(other: UnitOfMeasure): Boolean =
        dimension == other.dimension

    /**
     *  Magnitude of this unit relative to `other`.
     *
     *  `other` must be a commensurable unit.
     */
    fun relativeMagnitude(other: UnitOfMeasure): Double {
        if (!isCommensurable(other)) {
            throw IllegalArgumentException(
                "Not a commensurable unit."
            )
        }

        return magnitude / other.magnitude
    }

    /**
     *  `n`-th power.
     */
    fun pow(n: Int): UnitOfMeasure =
        when {
            n > 0 -> {
                var ans = this

                repeat(n - 1) {
                    ans *= this
                }

                ans
            }

            n < 0 -> {
                val newObj = UnitOfMeasure()
                val unitPositivePowered = pow(-n)
                val newMagnitude = 1.0 / unitPositivePowered.magnitude

                val newDim = unitPositivePowered
                    .dimension
                    .mapValues { (_, exp) -> -exp }

                newObj.magnitude = newMagnitude

                for ((baseUnit, exp) in newDim) {
                    newObj._dimension[baseUnit] = exp
                }

                newObj.isMetric =
                    newDim.count() == 1 &&
                    newDim.toList()[0].second == 1 &&
                    float.Comparison.nearlyEquals(
                        newObj.magnitude,
                        1.0
                    )

                newObj
            }

            else ->
                UnitOfMeasure()
        }

    /**
     *  `n`-th root.
     */
    fun root(n: Int): UnitOfMeasure {
        if (n <= 0) {
            throw IllegalArgumentException(
                "'n' must be positive."
            )
        }

        if (n == 1) {
            return this
        }

        val newObj = UnitOfMeasure()

        newObj.magnitude = magnitude.pow(1.0 / n)

        val newDim = dimension
            .mapValues { (_, exp) ->
                if (exp % n != 0) {
                    throw RuntimeException(
                        "$exp/$n has a non-zero remainder."
                    )
                }

                exp / n
            }

        for ((baseUnit, exp) in newDim) {
            newObj._dimension[baseUnit] = exp
        }

        newObj.isMetric =
            newDim.count() == 1 &&
            newDim.toList()[0].second == 1 &&
            float.Comparison.nearlyEquals(
                newObj.magnitude,
                1.0
            )

        return newObj
    }

    operator fun times(other: UnitOfMeasure): UnitOfMeasure {
        val newObj = UnitOfMeasure()

        val thisDim = dimension
        val otherDim = other.dimension

        newObj.magnitude = magnitude * other.magnitude

        // Add the exponents of each base unit.
        for (baseUnit in thisDim.keys.union(otherDim.keys)) {
            newObj._dimension[baseUnit] =
                thisDim.getOrDefault(baseUnit, 0) +
                otherDim.getOrDefault(baseUnit, 0)
        }

        newObj.isMetric =
            newObj._dimension.count() == 1 &&
            newObj._dimension.toList()[0].second == 1 &&
            float.Comparison.nearlyEquals(
                newObj.magnitude,
                1.0
            )

        return newObj
    }

    operator fun times(value: Double): UnitOfMeasure {
        val newObj = UnitOfMeasure()

        newObj.magnitude = magnitude * value

        for ((baseUnit, exp) in dimension) {
            newObj._dimension[baseUnit] = exp
        }

        newObj.isMetric =
            newObj._dimension.count() == 1 &&
            newObj._dimension.toList()[0].second == 1 &&
            float.Comparison.nearlyEquals(
                newObj.magnitude,
                1.0
            )

        return newObj
    }

    operator fun div(other: UnitOfMeasure): UnitOfMeasure {
        val newObj = UnitOfMeasure()

        val thisDim = dimension
        val otherDim = other.dimension

        newObj.magnitude = magnitude / other.magnitude

        // Subtract the exponents of each base unit.
        for (baseUnit in thisDim.keys.union(otherDim.keys)) {
            newObj._dimension[baseUnit] =
                thisDim.getOrDefault(baseUnit, 0) -
                otherDim.getOrDefault(baseUnit, 0)
        }

        newObj.isMetric =
            newObj._dimension.count() == 1 &&
            newObj._dimension.toList()[0].second == 1 &&
            float.Comparison.nearlyEquals(
                newObj.magnitude,
                1.0
            )

        return newObj
    }

    override fun hashCode(): Int =
        listOf(dimension, isMetric).hashCode()

    /**
     *  Uses [float.Comparison.nearlyEquals] for the magnitude component.
     */
    override fun equals(other: Any?): Boolean =
        other is UnitOfMeasure &&
        this::class == other::class &&
        (
            float.Comparison.nearlyEquals(
                magnitude,
                other.magnitude
            ) &&
            dimension == other.dimension
        )

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(3)

        packer
            .packString("magnitude")
            .packDouble(magnitude)

        val dimensionBuf = dimension

        packer
            .packString("dimension")
            .packMapHeader(dimensionBuf.count())

        for ((baseUnit, exp) in dimensionBuf) {
            packer.packString(baseUnit.name)
            packer.packInt(exp)
        }

        packer
            .packString("is-metric")
            .packBoolean(isMetric)

        packer.close()

        return packer.toByteArray()
    }

    companion object {
        /**
         *  Creates a [UnitOfMeasure] that is represented by a UCUM c/s symbol.
         *
         *  @param cs
         *      UCUM c/s symbol. It must exist in the JSON file that contains
         *      the predefined UCUM symbols.
         */
        @JvmStatic
        fun create(cs: String): UnitOfMeasure {
            val csAsBaseUnit = BaseUnit.getBaseUnit(cs)

            if (csAsBaseUnit != null) {
                return UnitOfMeasure(csAsBaseUnit)
            }

            val derivedUnits = @Suppress("UNCHECKED_CAST") (
                UcumSymbolStore.json["derived-units"]!!
                    as Map<String, Map<String, Any>>
            )

            if (!derivedUnits.containsKey(cs)) {
                throw RuntimeException(
                    "Unknown unit type for $cs"
                )
            }

            val newObj = UnitOfMeasure()

            newObj.magnitude = derivedUnits[cs]!!["magnitude"]!! as Double

            val expsByBaseCs = @Suppress("UNCHECKED_CAST") (
                derivedUnits[cs]!!["dimension"]!!
                    as Map<String, Int>
            )

            for ((baseCs, exp) in expsByBaseCs) {
                newObj._dimension[BaseUnit.getBaseUnit(baseCs)!!] = exp
            }

            newObj.isMetric = derivedUnits[cs]!!["is-metric"]!! as Boolean

            return newObj
        }
    }
}
