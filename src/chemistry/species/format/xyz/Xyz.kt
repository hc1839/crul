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

@file:JvmName("Xyz")
@file:JvmMultifileClass

package crul.chemistry.species.format.xyz

import java.io.Writer

import crul.chemistry.species.Atom
import crul.chemistry.species.Fragment
import crul.measure.Quantity
import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure

/**
 *  Serializes this fragment to XYZ.
 *
 *  Atoms are outputted in the order as returned by [Fragment.atoms].
 *
 *  @param writer
 *      Writer of XYZ with a trailing newline.
 *
 *  @param label
 *      Non-empty string to use as the complex label in the XYZ output.
 *
 *  @param fromLengthUnit
 *      The unit of length that the coordinates are in.
 *
 *  @param toLengthUnit
 *      The unit of length that the coordinates in the XYZ output are in.
 *
 *  @param separator
 *      Separator to use between columns in the output.
 */
@JvmOverloads
fun <A : Atom> Fragment<A>.exportXyz(
    writer: Writer,
    label: String,
    fromLengthUnit: UnitOfMeasure,
    toLengthUnit: UnitOfMeasure = UnitOfMeasure.parse("Ao"),
    separator: String = " "
)
{
    if (label.isEmpty()) {
        throw IllegalArgumentException(
            "Complex label is empty."
        )
    }

    if (!fromLengthUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
        throw IllegalArgumentException(
            "Unit of a coordinate must be a unit of length."
        )
    }

    if (!toLengthUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
        throw IllegalArgumentException(
            "Unit of a coordinate must be a unit of length."
        )
    }

    var xyzBuilder = ""

    xyzBuilder += atoms().count().toString() + "\n"
    xyzBuilder += label + "\n"

    for (atom in atoms()) {
        xyzBuilder += atom.element.symbol + separator
        xyzBuilder += atom
            .position
            .toArray()
            .map {
                Quantity
                    .convertUnit(it, fromLengthUnit, toLengthUnit)
                    .toString()
            }
            .joinToString(separator)
        xyzBuilder += "\n"
    }

    writer.write(xyzBuilder.trim() + "\n")
    writer.flush()
}
