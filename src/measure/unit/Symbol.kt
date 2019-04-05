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

import com.google.gson.Gson
import java.io.File
import kotlin.math.pow
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.hierarchy.tree.Node
import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.parse.Production
import crul.measure.unit.parse.TokenIterator
import crul.parse.shiftreduce.Actuator
import crul.serialize.BinarySerializable
import crul.serialize.MessagePackConv

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
 *  Prefix-related operations.
 */
object UnitPrefix {
    /**
     *  JSON of UCUM prefixes parsed by Gson.
     */
    private val prefixes: Map<String, Map<String, Any>> =
        @Suppress("UNCHECKED_CAST") (
            UcumSymbolStore.json["prefixes"]!!
                as Map<String, Map<String, Any>>
        )

    /**
     *  Whether a UCUM c/s symbol represents a prefix.
     *
     *  @param cs
     *      UCUM c/s symbol of the prefix.
     */
    @JvmStatic
    fun isPrefix(cs: String): Boolean =
        prefixes.containsKey(cs)

    /**
     *  Value of a prefix.
     *
     *  @param cs
     *      UCUM c/s symbol of the prefix. An exception is raised if there is
     *      no such prefix.
     */
    @JvmStatic
    fun getValue(cs: String): Double {
        if (!isPrefix(cs)) {
            throw IllegalArgumentException(
                "No such prefix: $cs"
            )
        }

        return prefixes[cs]!!["value"]!! as Double
    }
}

/**
 *  Unit of measure according to the Unified Code for Units of Measure (UCUM).
 *
 *  To instantiate this class with a derived unit, use [parse].
 */
class UnitOfMeasure : BinarySerializable {
    /**
     *  Magnitude.
     */
    var magnitude: Double
        private set

    /**
     *  Base units associated with their exponents as a backing property.
     */
    private val _dimension: MutableMap<BaseUnit, Int> = mutableMapOf()

    /**
     *  Base unit raised to the power of 1.
     */
    constructor(baseUnit: BaseUnit) {
        magnitude = 1.0
        _dimension[baseUnit] = 1
    }

    /**
     *  Dimensionless unit.
     */
    constructor() {
        magnitude = 1.0
    }

    /**
     *  Base units associated with their exponents.
     *
     *  Only entries where the exponent is non-zero is returned.
     */
    val dimension: Map<BaseUnit, Int>
        get() = _dimension.filter { (_, exp) -> exp != 0 }

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
                    val baseUnit = BaseUnit
                        .getByCs(key.asStringValue().toString())!!

                    val exp = value.asIntegerValue().toInt()

                    Pair(baseUnit, exp)
                }
                .toMap()
        )
    }

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        MessagePackConv.getInnerMap(
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

        return newObj
    }

    operator fun times(value: Double): UnitOfMeasure {
        val newObj = UnitOfMeasure()

        newObj.magnitude = magnitude * value

        for ((baseUnit, exp) in dimension) {
            newObj._dimension[baseUnit] = exp
        }

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

        return newObj
    }

    override fun hashCode(): Int =
        listOf(dimension).hashCode()

    /**
     *  Uses [float.Comparison.nearlyEquals] for the magnitude component.
     */
    override fun equals(other: Any?): Boolean =
        other is UnitOfMeasure &&
        this::class == other::class &&
        (
            crul.float.Comparison.nearlyEquals(
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
            .packMapHeader(2)

        packer
            .packString("magnitude")
            .packDouble(magnitude)

        val dimensionBuf = dimension

        packer
            .packString("dimension")
            .packMapHeader(dimensionBuf.count())

        for ((baseUnit, exp) in dimensionBuf) {
            packer.packString(baseUnit.cs)
            packer.packInt(exp)
        }

        packer.close()

        return packer.toByteArray()
    }

    companion object {
        /**
         *  JSON of UCUM derived units parsed by Gson.
         */
        private val derivedUnits: Map<String, Map<String, Any>> by lazy {
            @Suppress("UNCHECKED_CAST") (
                UcumSymbolStore.json["derived-units"]!!
                    as Map<String, Map<String, Any>>
            )
        }

        /**
         *  Whether a UCUM c/s symbol represents a base unit.
         */
        @JvmStatic
        fun isBase(cs: String): Boolean =
            BaseUnit.getByCs(cs) != null

        /**
         *  Whether a UCUM c/s symbol represents a metric unit.
         *
         *  `false` is returned if the c/s symbol does not exist in the JSON
         *  file that contains predefined units.
         */
        @JvmStatic
        fun isMetric(cs: String): Boolean {
            if (isBase(cs)) {
                return true
            }

            return if (derivedUnits.containsKey(cs)) {
                derivedUnits[cs]!!["metric"]!! as Boolean
            } else {
                false
            }
        }

        /**
         *  Whether a UCUM c/s symbol represents a known unit atom.
         */
        @JvmStatic
        fun isUnitAtom(cs: String): Boolean =
            isBase(cs) || derivedUnits.containsKey(cs)

        /**
         *  Parses a text that specifies a unit in the UCUM format, and creates
         *  a [UnitOfMeasure].
         *
         *  @param unitText
         *      Unit specified in the UCUM format.
         */
        @JvmStatic
        fun parse(unitText: String): UnitOfMeasure =
            if (isBase(unitText)) {
                UnitOfMeasure(BaseUnit.getByCs(unitText)!!)
            } else if (unitText == "1") {
                UnitOfMeasure()
            } else if (isUnitAtom(unitText)) {
                // It is a unit atom that is not a base unit.

                val defValue =
                    derivedUnits[unitText]!!["definition-value"] as Double

                val defUnitText =
                    derivedUnits[unitText]!!["definition-unit"] as String

                parse(defUnitText) * defValue
            } else {
                val tokens = TokenIterator(unitText)
                val actuator = Actuator(tokens)
                val parseRoot = actuator.actuate()

                parseRoot.getUserData(Production.userDataKey) as UnitOfMeasure
            }
    }
}
