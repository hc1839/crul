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
import org.apache.avro.Schema
import org.apache.avro.generic.*

import crul.serialize.AvroSimple

private object ElementAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.chemistry.species",
       |    "name": "Element",
       |    "fields": [
       |        { "type": "string", "name": "symbol" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Storage information for elements.
 */
private object ElementStore {
    /**
     *  Path to the JSON file containing elements with respect to the JAR
     *  resource.
     */
    val path: String = "/crul/chemistry/species/elements.json"

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
    constructor(atomicNumber: Int): this(
        getSymbolByNumber(atomicNumber)
    )

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): this(
        avroRecord.get("symbol").toString()
    )

    /**
     *  Deserialization constructor.
     */
    private constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            ElementAvsc.schema,
            avroData
        ).first()
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
         *  Serializes an [Element] in Apache Avro.
         *
         *  @param obj
         *      [Element] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Element): ByteBuffer {
            val avroRecord = GenericData.Record(
                ElementAvsc.schema
            )

            avroRecord.put("symbol", obj.symbol)

            return AvroSimple.serializeData<GenericRecord>(
                ElementAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes an [Element] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Element] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Element].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): Element =
            Element(avroData)
    }
}
