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

package crul.chemistry.species

import com.google.gson.Gson
import java.io.File
import java.nio.ByteBuffer
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.serialize.MessagePackSimple

/**
 *  Storage information for elements.
 */
private object ElementStore {
    /**
     *  Path to the JSON file containing elements with respect to the JAR
     *  resource.
     */
    val path: String = "/chemistry/species/elements.json"

    /**
     *  JSON of elements parsed by Gson.
     */
    val json: Map<String, Map<String, Any>> by lazy {
        val gson = Gson()

        @Suppress("UNCHECKED_CAST")
        gson.fromJson(
            this::class.java.getResourceAsStream(path).reader(),
            Map::class.java
        ) as Map<String, Map<String, Any>>
    }
}

/**
 *  Chemical element.
 *
 *  Element data were obtained from Los Alamos National Laboratory.
 */
class Element {
    /**
     *  Symbol of the element.
     */
    val symbol: String

    /**
     *  @param symbol
     *      Symbol of the element.
     */
    constructor(symbol: String) {
        this.symbol = symbol
    }

    /**
     *  @param atomicNumber
     *      Atomic number of the element.
     */
    constructor(atomicNumber: Int) {
        this.symbol = getSymbolByNumber(atomicNumber)
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
        unpackedMap["symbol"]!!.asStringValue().toString()
    )

    /**
     *  Deserialization constructor.
     */
    private constructor(msgpack: ByteArray): this(
        msgpack,
        MessagePackSimple.getInnerMap(
            msgpack,
            Element::class.qualifiedName!!
        )
    )

    /**
     *  Name of the element.
     */
    val name: String
        get() =
            @Suppress("UNCHECKED_CAST")
            ElementStore.json[symbol]!!["name"] as String

    /**
     *  Atomic number.
     */
    val number: Int
        get() =
            @Suppress("UNCHECKED_CAST")
            (ElementStore.json[symbol]!!["number"] as Double).toInt()

    /**
     *  Atomic weight.
     */
    val weight: Double
        get() =
            @Suppress("UNCHECKED_CAST")
            ElementStore.json[symbol]!!["weight"] as Double

    /**
     *  Atomic radius in bohr.
     */
    val radius: Double
        get() =
            @Suppress("UNCHECKED_CAST")
            ElementStore.json[symbol]!!["radius"] as Double

    override fun hashCode(): Int =
        listOf(symbol).hashCode()

    override fun equals(other: Any?): Boolean =
        other is Element &&
        this::class == other::class &&
        (
            symbol == other.symbol
        )

    companion object {
        /**
         *  Gets the symbol of an element by its atomic number.
         *
         *  If there is no such atomic number, an exception is raised.
         */
        @JvmStatic
        fun getSymbolByNumber(atomicNumber: Int): String {
            val symbol = ElementStore
                .json
                .entries
                .filter { (_, info) ->
                    (info["number"] as Double).toInt() == atomicNumber
                }
                .singleOrNull()
                ?.key

            return if (symbol != null) {
                symbol
            } else {
                throw IllegalArgumentException(
                    "No such atomic number: $atomicNumber"
                )
            }
        }

        /**
         *  Serializates an [Element] in MessagePack.
         *
         *  @param obj
         *      [Element] to serialize.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Element): ByteBuffer {
            val packer = MessagePack.newDefaultBufferPacker()

            packer.packMapHeader(1)

            packer
                .packString(obj::class.qualifiedName)
                .packMapHeader(1)

            packer
                .packString("symbol")
                .packString(obj.symbol)

            packer.close()

            return ByteBuffer.wrap(packer.toByteArray())
        }

        /**
         *  Deserializes an [Element] in MessagePack.
         *
         *  @param msgpack
         *      Serialized [Element] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Element].
         */
        @JvmStatic
        fun deserialize(msgpack: ByteBuffer): Element {
            val msgpackByteArray = ByteArray(
                msgpack.limit() - msgpack.position()
            ) {
                msgpack.get()
            }

            return Element(msgpackByteArray)
        }
    }
}
