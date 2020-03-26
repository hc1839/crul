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
import java.nio.ByteBuffer
import kotlin.math.pow
import org.apache.avro.Schema
import org.apache.avro.generic.*

import crul.float.FloatCompare.nearlyEquals
import crul.hierarchy.tree.Node
import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.parse.Production
import crul.measure.unit.parse.TokenIterator
import crul.parse.shiftreduce.Actuator
import crul.serialize.AvroSimple

/**
 *  Storage information for UCUM symbols.
 */
private object UcumSymbolStore {
    /**
     *  Path to the JSON file containing UCUM symbols with respect to the JAR
     *  resource.
     */
    val path: String = "/crul/measure/unit/ucum-symbols.json"

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

private object UnitOfMeasureAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.measure.unit",
       |    "name": "UnitOfMeasure",
       |    "fields": [
       |        { "type": "double", "name": "magnitude" },
       |        {
       |            "type": { "type": "map", "values": "int" },
       |            "name": "exponents"
       |        }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Unit of measure according to the Unified Code for Units of Measure (UCUM).
 *
 *  To instantiate this class with a derived unit, use [parse].
 */
class UnitOfMeasure {
    /**
     *  Magnitude.
     */
    val magnitude: Double

    /**
     *  Base units associated with their exponents.
     *
     *  Only entries where the exponent is non-zero is stored.
     */
    val exponents: Map<BaseUnit, Int>

    private constructor(magnitude: Double, exponents: Map<BaseUnit, Int>) {
        this.magnitude = magnitude
        this.exponents = exponents.filter { (_, exp) -> exp != 0 }
    }

    /**
     *  Base unit raised to the power of 1.
     */
    constructor(baseUnit: BaseUnit): this(
        1.0,
        mapOf(baseUnit to 1)
    )

    /**
     *  Dimensionless unit.
     */
    constructor(): this(
        1.0,
        mapOf()
    )

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): this(
        avroRecord.get("magnitude") as Double,
        @Suppress("UNCHECKED_CAST") (
            avroRecord.get("exponents") as Map<*, Int>
        ).mapKeys { (baseUnitCs, _) ->
            BaseUnit.getByCs(baseUnitCs.toString())!!
        }
    )

    /**
     *  Deserialization constructor.
     */
    constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            UnitOfMeasureAvsc.schema,
            avroData
        ).first()
    )

    /**
     *  Whether this unit is commensurable with another unit.
     */
    fun isCommensurableWith(other: UnitOfMeasure): Boolean =
        exponents == other.exponents

    /**
     *  Whether this unit is a unit of a given ISQ dimension.
     *
     *  Since `mol` is not one of the UCUM base units, the ISQ dimension, `N`,
     *  is ignored.
     *
     *  @param dimension
     *      ISQ dimension to test.
     *
     *  @return
     *      `true` if this unit is a unit of `dimension`; `false` if otherwise.
     */
    fun isUnitOf(dimension: Dimension): Boolean {
        // Exponents associated by base dimension without amount of substance.
        val expsByBaseDim = dimension.exponents -
            BaseDimension.AMOUNT_OF_SUBSTANCE

        val testDim = if (!expsByBaseDim.isEmpty()) {
            expsByBaseDim
                .map { (baseDim, exp) ->
                    Dimension(baseDim).pow(exp)
                }
                .reduce { acc, dim ->
                    acc * dim
                }
        } else {
            Dimension()
        }

        val thisDim = if (!exponents.isEmpty()) {
            exponents
                .entries
                .map { (baseUnit, exp) ->
                    baseUnit.dimension.pow(exp)
                }
                .reduce { acc, dim ->
                    acc * dim
                }
        } else {
            Dimension()
        }

        return thisDim == testDim
    }

    /**
     *  Whether this unit is a unit of a given dimension in ISQ format.
     *
     *  @param dimension
     *      Dimension in ISQ format to test.
     *
     *  @return
     *      `true` if this unit is a unit of `dimension`; `false` if otherwise.
     */
    fun isUnitOf(dimension: String): Boolean =
        isUnitOf(Dimension.parse(dimension))

    /**
     *  Magnitude of this unit relative to `other`.
     *
     *  `other` must be a commensurable unit.
     */
    fun relativeMagnitude(other: UnitOfMeasure): Double {
        if (!isCommensurableWith(other)) {
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
                val unitPositivePowered = pow(-n)
                val newMagnitude = 1.0 / unitPositivePowered.magnitude

                val newDim = unitPositivePowered
                    .exponents
                    .mapValues { (_, exp) -> -exp }

                UnitOfMeasure(newMagnitude, newDim)
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

        val newMagnitude = magnitude.pow(1.0 / n)

        val newDim = exponents.mapValues { (_, exp) ->
            if (exp % n != 0) {
                throw RuntimeException(
                    "$exp/$n has a non-zero remainder."
                )
            }

            exp / n
        }

        return UnitOfMeasure(newMagnitude, newDim)
    }

    operator fun times(other: UnitOfMeasure): UnitOfMeasure {
        val thisDim = exponents
        val otherDim = other.exponents

        val newMagnitude = magnitude * other.magnitude

        // Add the exponents of each base unit.
        val newDim = thisDim.keys.union(otherDim.keys).associateWith {
            baseUnit ->

            listOf(thisDim, otherDim).map { dim ->
                dim.getOrDefault(baseUnit, 0)
            }.sum()
        }

        return UnitOfMeasure(newMagnitude, newDim)
    }

    operator fun times(value: Double): UnitOfMeasure =
        UnitOfMeasure(magnitude * value, exponents)

    operator fun div(other: UnitOfMeasure): UnitOfMeasure {
        val thisDim = exponents
        val otherDim = other.exponents

        val newMagnitude = magnitude / other.magnitude

        // Subtract the exponents of each base unit.
        val newDim = thisDim.keys.union(otherDim.keys).associateWith {
            baseUnit ->

            listOf(thisDim, otherDim).map { dim ->
                dim.getOrDefault(baseUnit, 0)
            }.reduce(Int::minus)
        }

        return UnitOfMeasure(newMagnitude, newDim)
    }

    override fun hashCode(): Int =
        listOf(exponents).hashCode()

    /**
     *  Uses [nearlyEquals] for the magnitude component.
     */
    override fun equals(other: Any?): Boolean =
        other is UnitOfMeasure &&
        this::class == other::class &&
        (
            nearlyEquals(magnitude, other.magnitude) &&
            exponents == other.exponents
        )

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
        fun parse(unitText: String): UnitOfMeasure {
            val unitTextTrimmed = unitText.trim()

            return if (isBase(unitTextTrimmed)) {
                UnitOfMeasure(BaseUnit.getByCs(unitTextTrimmed)!!)
            } else if (unitTextTrimmed == "1") {
                UnitOfMeasure()
            } else if (isUnitAtom(unitTextTrimmed)) {
                // It is a unit atom that is not a base unit.

                val defValue =
                    derivedUnits[unitTextTrimmed]!!["definition-value"]
                        as Double

                val defUnitText =
                    derivedUnits[unitTextTrimmed]!!["definition-unit"]
                        as String

                parse(defUnitText) * defValue
            } else {
                val tokens = TokenIterator(unitTextTrimmed)
                val actuator = Actuator(tokens)
                val parseRoot = actuator.actuate()

                parseRoot.getUserData(Production.userDataKey) as UnitOfMeasure
            }
        }

        /**
         *  Serializes a [UnitOfMeasure] in Apache Avro.
         *
         *  @param obj
         *      [UnitOfMeasure] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: UnitOfMeasure): ByteBuffer {
            val avroRecord = GenericData.Record(
                UnitOfMeasureAvsc.schema
            )

            avroRecord.put("magnitude", obj.magnitude)

            avroRecord.put(
                "exponents",
                obj.exponents.mapKeys { (baseUnit, _) ->
                    baseUnit.cs
                }
            )

            return AvroSimple.serializeData<GenericRecord>(
                UnitOfMeasureAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [UnitOfMeasure] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [UnitOfMeasure] as returned by [serialize].
         *
         *  @return
         *      Deserialized [UnitOfMeasure].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): UnitOfMeasure =
            UnitOfMeasure(avroData)
    }
}
