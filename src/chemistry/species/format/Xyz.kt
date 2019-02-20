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

@file:JvmMultifileClass

package chemistry.species.format

import chemistry.species.Atom
import chemistry.species.MoleculeComplex
import measure.dimension.BaseDimension
import measure.unit.UnitOfMeasure

/**
 *  Functions related to XYZ.
 */
object Xyz {
    /**
     *  Serializes this complex to XYZ.
     *
     *  @param fromLengthUnit
     *      The unit of length that the coordinates are in.
     *
     *  @param toLengthUnit
     *      The unit of length that the coordinates in the XYZ output are in.
     *
     *  @param name
     *      Name of the complex to use in the XYZ output.
     *
     *  @param separator
     *      Separator to use between columns in the output.
     */
    @JvmStatic
    @JvmOverloads
    fun <A : Atom> MoleculeComplex<A>.toXyz(
        fromLengthUnit: UnitOfMeasure,
        toLengthUnit: UnitOfMeasure = UnitOfMeasure.parse("Ao"),
        name: String = uuid.Generator.inNCName(),
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
        xyzBuilder += name + "\n"

        for (atom in atomsBuf) {
            xyzBuilder += atom.element.symbol + separator
            xyzBuilder += atom
                .position
                .components
                .map { it.toString() }
                .joinToString(separator)
            xyzBuilder += "\n"
        }

        xyzBuilder = xyzBuilder.trim() + "\n"

        return xyzBuilder
    }
}
