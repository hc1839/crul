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

package measure.dimension

import kotlin.math.pow
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import measure.dimension.parse.Production
import measure.dimension.parse.TokenIterator
import parse.shiftreduce.Actuator
import serialize.BinarySerializable

/**
 *  Dimension of a quantity according to the International System of Quantities
 *  (ISQ).
 *
 *  Dimension does not include the magnitude or the unit.
 */
class Dimension : BinarySerializable {
    /**
     *  Exponents associated by base dimensions as a backing property.
     */
    private val _exponents: MutableMap<BaseDimension, Int> = mutableMapOf()

    /**
     *  Exponents associated by base dimensions.
     *
     *  Only entries where the exponent is non-zero is returned.
     */
    val exponents: Map<BaseDimension, Int>
        get() = _exponents.filter { (_, exp) -> exp != 0 }

    /**
     *  Base dimension raised to the power of 1.
     */
    constructor(baseDimension: BaseDimension) {
        _exponents[baseDimension] = 1
    }

    /**
     *  Dimensionless instance.
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
        this._exponents.putAll(
            unpackedMap["exponents"]!!
                .asMapValue()
                .map()
                .map { (key, value) ->
                    val baseDim = BaseDimension.getBySymbol(
                        key.asStringValue().toString()
                    )!!
                    val exp = value.asIntegerValue().toInt()

                    Pair(baseDim, exp)
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
            Dimension::class.qualifiedName!!
        ),
        msgpack
    )

    override fun hashCode(): Int =
        exponents.hashCode()

    override fun equals(other: Any?): Boolean =
        other is Dimension &&
        this::class == other::class &&
        (
            exponents == other.exponents
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

        val exponentsBuf = exponents

        packer
            .packString("exponents")
            .packMapHeader(exponentsBuf.count())

        for ((baseDim, exp) in exponentsBuf) {
            packer
                .packString(baseDim.symbol)
                .packInt(exp)
        }

        packer.close()

        return packer.toByteArray()
    }

    /**
     *  `n`-th power.
     */
    fun pow(n: Int): Dimension {
        val newObj = Dimension()

        for ((baseDim, exp) in _exponents) {
            val newExp = exp * n

            if (newExp != 0) {
                newObj._exponents[baseDim] = newExp
            }
        }

        return newObj
    }

    /**
     *  `n`-th root.
     */
    fun root(n: Int): Dimension {
        val newObj = Dimension()

        for ((baseDim, exp) in _exponents) {
            if (exp % n != 0) {
                throw IllegalArgumentException(
                    "Exponent of $baseDim, $exp, " +
                    "is not divisible by $n."
                )
            }

            val newExp = exp / n

            if (newExp != 0) {
                newObj._exponents[baseDim] = newExp
            }
        }

        return newObj
    }

    operator fun times(other: Dimension): Dimension {
        val newObj = Dimension()

        val thisExps = exponents
        val otherExps = other.exponents

        // Add the exponents of each base dimension.
        for (baseDim in thisExps.keys.union(otherExps.keys)) {
            val newExp =
                thisExps.getOrDefault(baseDim, 0) +
                otherExps.getOrDefault(baseDim, 0)

            if (newExp != 0) {
                newObj._exponents[baseDim] = newExp
            }
        }

        return newObj
    }

    operator fun div(other: Dimension): Dimension {
        val newObj = Dimension()

        val thisExps = exponents
        val otherExps = other.exponents

        // Subtract the exponents of each base dimension.
        for (baseDim in thisExps.keys.union(otherExps.keys)) {
            val newExp =
                thisExps.getOrDefault(baseDim, 0) -
                otherExps.getOrDefault(baseDim, 0)

            if (newExp != 0) {
                newObj._exponents[baseDim] = newExp
            }
        }

        return newObj
    }

    companion object {
        /**
         *  Whether a letter symbol represents a base dimension.
         */
        @JvmStatic
        fun isBase(symbol: String): Boolean =
            BaseDimension.getBySymbol(symbol) != null

        /**
         *  Creates a [Dimension] by parsing a text that specifies a dimension
         *  according to [Production].
         */
        @JvmStatic
        fun parse(dimensionText: String): Dimension =
            if (isBase(dimensionText)) {
                Dimension(BaseDimension.getBySymbol(dimensionText)!!)
            } else {
                val tokens = TokenIterator(dimensionText)
                val actuator = Actuator(tokens.asSequence())
                val parseRoot = actuator.actuate()

                parseRoot.getUserData(Production.userDataKey) as Dimension
            }
    }
}
