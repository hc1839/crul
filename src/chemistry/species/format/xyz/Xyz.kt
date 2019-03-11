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

import crul.chemistry.species.Atom
import crul.chemistry.species.MoleculeComplex
import crul.measure.Quantity
import crul.measure.dimension.BaseDimension
import crul.measure.unit.UnitOfMeasure

/**
 *  Serializes this complex to XYZ.
 *
 *  @param fromLengthUnit
 *      The unit of length that the coordinates are in.
 *
 *  @param toLengthUnit
 *      The unit of length that the coordinates in the XYZ output are in.
 *
 *  @param label
 *      Label to use for the complex in the XYZ output.
 *
 *  @param separator
 *      Separator to use between columns in the output.
 */
@JvmOverloads
fun <A : Atom> MoleculeComplex<A>.toXyz(
    fromLengthUnit: UnitOfMeasure,
    toLengthUnit: UnitOfMeasure = UnitOfMeasure.parse("Ao"),
    label: String = id,
    separator: String = " "
): String
{
    if (!fromLengthUnit.isCommensurable(BaseDimension.LENGTH.siUnit)) {
        throw IllegalArgumentException(
            "Unit of a coordinate must be a unit of length."
        )
    }

    if (!toLengthUnit.isCommensurable(BaseDimension.LENGTH.siUnit)) {
        throw IllegalArgumentException(
            "Unit of a coordinate must be a unit of length."
        )
    }

    var xyzBuilder = ""
    val atomsBuf = atoms().asSequence().toList()

    xyzBuilder += atomsBuf.count().toString() + "\n"
    xyzBuilder += label + "\n"

    for (atom in atomsBuf) {
        xyzBuilder += atom.element.symbol + separator
        xyzBuilder += atom
            .position
            .components
            .map {
                Quantity
                    .convertUnit(it, fromLengthUnit, toLengthUnit)
                    .toString()
            }
            .joinToString(separator)
        xyzBuilder += "\n"
    }

    xyzBuilder = xyzBuilder.trim() + "\n"

    return xyzBuilder
}
