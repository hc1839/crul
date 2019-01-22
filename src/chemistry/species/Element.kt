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

package chemistry.species

import com.google.gson.Gson
import java.io.File
import org.msgpack.core.MessagePack
import org.msgpack.value.Value
import serialize.BinarySerializable

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
class Element : BinarySerializable {
    /**
     *  Symbol of the element.
     */
    val symbol: String

    constructor(symbol: String) {
        this.symbol = symbol
    }

    /**
     *  Data-based constructor.
     */
    private constructor(ctorArgs: CtorArgs): this(ctorArgs.symbol)

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(getCtorArgs(msgpack))

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
            .packString("symbol")
            .packString(symbol)

        packer.close()

        return packer.toByteArray()
    }

    override fun hashCode() =
        listOf(symbol).hashCode()

    override fun equals(other: Any?): Boolean =
        other is Element &&
        this::class == other::class &&
        (
            symbol == other.symbol
        )

    companion object {
        /**
         *  Constructor arguments.
         */
        private data class CtorArgs(val symbol: String)

        /**
         *  Gets the constructor arguments from [serialize].
         */
        private fun getCtorArgs(msgpack: ByteArray): CtorArgs {
            val (unpackedMap, _) = BinarySerializable
                .getMapRestPair(
                    msgpack,
                    Element::class.qualifiedName!!
                )

            return CtorArgs(
                unpackedMap["symbol"]!!.asStringValue().toString()
            )
        }
    }
}
