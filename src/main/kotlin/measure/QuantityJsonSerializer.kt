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

import javax.json.Json
import javax.json.JsonArray

import crul.measure.unit.UnitOfMeasure
import crul.serialize.Serializer

/**
 *  Serializer of [Quantity] in JSON.
 */
object QuantityJsonSerializer : Serializer<Quantity, JsonArray> {
    /**
     *  @param obj
     *      [Quantity] to serialize.
     *
     *  @param args
     *      Additional arguments. This method will register the following in
     *      order:
     *          1. `String`. UCUM unit in text format.
     */
    override fun serialize(obj: Quantity, vararg args: Any?): JsonArray {
        val unitText = args[0] as String

        return Json
            .createArrayBuilder()
            .add(obj.value(UnitOfMeasure.parse(unitText)))
            .add(unitText)
            .build()
    }

    /**
     *  @param dataFormat
     *      Data format to deserialize [Quantity] from.
     *
     *  @param args
     *      Not used.
     *
     *  @return
     *      Deserialized [Quantity].
     */
    override fun deserialize(
        dataFormat: JsonArray,
        vararg args: Any?
    ): Quantity
    {
        val value = dataFormat.getJsonNumber(0).doubleValue()
        val unitText = dataFormat.getString(1)

        return Quantity(
            value,
            UnitOfMeasure.parse(unitText)
        )
    }
}
