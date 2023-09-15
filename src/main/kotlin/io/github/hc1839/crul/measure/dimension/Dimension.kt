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

package io.github.hc1839.crul.measure.dimension

import java.nio.ByteBuffer
import kotlin.math.pow
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import io.github.hc1839.crul.measure.dimension.parse.Production
import io.github.hc1839.crul.measure.dimension.parse.TokenIterator
import io.github.hc1839.crul.parse.shiftreduce.Actuator
import io.github.hc1839.crul.serialize.MessagePackSimple

/**
 *  Dimension of a quantity according to the International System of Quantities
 *  (ISQ).
 *
 *  Dimension does not include the magnitude or the unit.
 */
class Dimension {
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
        msgpack,
        MessagePackSimple.getInnerMap(
            msgpack,
            Dimension::class.qualifiedName!!
        )
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
        fun parse(dimensionText: String): Dimension {
            val dimTextTrimmed = dimensionText.trim()

            return if (isBase(dimTextTrimmed)) {
                Dimension(BaseDimension.getBySymbol(dimTextTrimmed)!!)
            } else if (dimTextTrimmed == "1") {
                Dimension()
            } else {
                val tokens = TokenIterator(dimTextTrimmed)
                val actuator = Actuator(tokens)
                val parseRoot = actuator.actuate()

                parseRoot.getUserData(Production.userDataKey) as Dimension
            }
        }

        /**
         *  Serializes a [Dimension] in MessagePack.
         *
         *  @param obj
         *      [Dimension] to serialize.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Dimension): ByteBuffer {
            val packer = MessagePack.newDefaultBufferPacker()

            packer.packMapHeader(1)

            packer
                .packString(obj::class.qualifiedName)
                .packMapHeader(1)

            val exponentsBuf = obj.exponents

            packer
                .packString("exponents")
                .packMapHeader(exponentsBuf.count())

            for ((baseDim, exp) in exponentsBuf) {
                packer
                    .packString(baseDim.symbol)
                    .packInt(exp)
            }

            packer.close()

            return ByteBuffer.wrap(packer.toByteArray())
        }

        /**
         *  Deserializes a [Dimension] in MessagePack.
         *
         *  @param msgpack
         *      Serialized [Dimension] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Dimension].
         */
        @JvmStatic
        fun deserialize(msgpack: ByteBuffer): Dimension {
            val msgpackByteArray = ByteArray(
                msgpack.limit() - msgpack.position()
            ) {
                msgpack.get()
            }

            return Dimension(msgpackByteArray)
        }
    }
}
