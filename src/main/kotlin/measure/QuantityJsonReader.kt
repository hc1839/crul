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

package crul.measure

import javax.json.JsonArray

import crul.measure.unit.UnitOfMeasure

/**
 *  Reader of a [Quantity] from a JSON array input source.
 *
 *  @constructor
 *
 *  @param input
 *      JSON array input source.
 */
class QuantityJsonReader(private val input: JsonArray) : QuantityReader {
    override fun read(): Quantity {
        if (input.count() != 2) {
            throw RuntimeException(
                "JSON array does not have exactly two elements."
            )
        }

        val value = input.getJsonNumber(0).doubleValue()
        val unitText = input.getString(1)

        return Quantity(
            value,
            UnitOfMeasure.parse(unitText)
        )
    }

    override fun close() { }
}
