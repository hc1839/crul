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

package io.github.hc1839.crul.measure

import javax.json.JsonArrayBuilder

import io.github.hc1839.crul.measure.unit.UnitOfMeasure

/**
 *  Writer of a [Quantity] to a JSON array output source.
 *
 *  @constructor
 *
 *  @param output
 *      JSON array output source.
 */
class QuantityJsonWriter(
    private val output: JsonArrayBuilder
) : QuantityWriter
{
    override fun write(obj: Quantity, unitUcum: String) {
        output
            .add(obj.value(UnitOfMeasure.parse(unitUcum)))
            .add(unitUcum)
    }

    override fun close() { }
}
