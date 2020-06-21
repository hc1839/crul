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

import java.io.File
import java.nio.ByteBuffer
import javax.json.Json
import javax.json.JsonObject

import crul.measure.Quantity
import crul.measure.QuantityJsonSerializer

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
     *  JSON of chemical elements.
     */
    val json: JsonObject by lazy {
        Json
            .createReader(
                this::class.java.getResourceAsStream(path).reader()
            )
            .use { it.readObject() }
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
     *      Symbol of the element. If invalid, an exception is raised.
     */
    constructor(symbol: String) {
        if (!isValidSymbol(symbol)) {
            throw IllegalArgumentException(
                "Not a valid symbol: $symbol"
            )
        }

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
     *  Name of the element.
     */
    val name: String
        get() = ElementStore
            .json
            .getJsonObject(symbol)
            .getString("name")

    /**
     *  Atomic number.
     */
    val number: Int
        get() = ElementStore
            .json
            .getJsonObject(symbol)
            .getInt("number")

    /**
     *  Atomic weight in atomic units.
     */
    val weight: Double
        get() = ElementStore
            .json
            .getJsonObject(symbol)
            .getJsonNumber("weight")
            .doubleValue()

    /**
     *  Van der Waals radius.
     */
    val radius: Quantity
        get() = QuantityJsonSerializer.deserialize(
            ElementStore
                .json
                .getJsonObject(symbol)
                .getJsonArray("radius")
        )

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
         *  Whether a given symbol is a symbol of a known element.
         */
        @JvmStatic
        fun isValidSymbol(symbol: String): Boolean =
            ElementStore.json.containsKey(symbol)

        /**
         *  Gets the symbol of an element by its atomic number.
         *
         *  If there is no such atomic number, an exception is raised.
         */
        @JvmStatic
        fun getSymbolByNumber(atomicNumber: Int): String {
            val symbol = ElementStore
                .json
                .filterValues {
                    it as JsonObject
                    it.getInt("number") == atomicNumber
                }
                .keys
                .singleOrNull()

            return if (symbol != null) {
                symbol
            } else {
                throw IllegalArgumentException(
                    "No such atomic number: $atomicNumber"
                )
            }
        }
    }
}
