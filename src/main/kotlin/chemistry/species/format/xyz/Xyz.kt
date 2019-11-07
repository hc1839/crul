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

import java.io.BufferedReader
import java.io.Reader
import java.io.Writer
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import crul.chemistry.species.Atom
import crul.chemistry.species.Element
import crul.chemistry.species.Fragment
import crul.measure.Quantity
import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure

/**
 *  Serializes this list of fragments to XYZ.
 *
 *  Fragments are outputted in the given order. For each fragment, atoms are
 *  outputted in the order as returned by [Fragment.atoms].
 *
 *  @param writer
 *      Writer of XYZ with a trailing newline.
 *
 *  @param label
 *      Non-empty string to use as the label in XYZ.
 *
 *  @param atomPosUnit
 *      Unit of the coordinates that the positions are in. It must be a unit of
 *      `L`.
 */
fun <A : Atom> List<Fragment<A>>.exportXyz(
    writer: Writer,
    label: String,
    atomPosUnit: UnitOfMeasure
)
{
    if (label.isEmpty()) {
        throw IllegalArgumentException(
            "Label is empty."
        )
    }

    if (!atomPosUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
        throw IllegalArgumentException(
            "Unit of the coordinates is not a unit of length."
        )
    }

    var xyzBuilder = ""

    xyzBuilder += sumBy { it.atoms().count() }.toString() + "\n"
    xyzBuilder += label + "\n"

    val angstromUnit = UnitOfMeasure.parse("Ao")

    for (fragment in this) {
        for (atom in fragment.atoms()) {
            xyzBuilder += atom.element.symbol + " "

            xyzBuilder += atom.position.toArray().map {
                Quantity.convertUnit(
                    it,
                    atomPosUnit,
                    angstromUnit
                ).toString()
            }.joinToString(" ") + "\n"
        }
    }

    writer.write(xyzBuilder.trim() + "\n")
    writer.flush()
}

/**
 *  Parses XYZ format.
 *
 *  Any columns after the coordinates are ignored.
 *
 *  @param reader
 *      Reader from which XYZ is to be read.
 *
 *  @param atomPosUnit
 *      Unit of the coordinates that the positions of the deserialized atoms
 *      are in. It must be a unit of `L`.
 *
 *  @return
 *      Fragment containing the atoms from the XYZ reader. Atom tags are set to
 *      one-based indices in the same order as the atoms specified in XYZ. All
 *      atoms in the returned fragment have `null` as their charge.
 */
fun Fragment.Companion.parseXyz(
    reader: Reader,
    atomPosUnit: UnitOfMeasure
): Fragment<Atom>
{
    if (!atomPosUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
        throw IllegalArgumentException(
            "Unit of the coordinates is not a unit of length."
        )
    }

    val commentRegex = Regex("^\\s*#")
    val bufferedReader = BufferedReader(reader)

    val xyzLines = bufferedReader
        .lines()
        .iterator()
        .asSequence()
        .map { it.trim() }
        .filterNot { commentRegex in it || it == "" }
        .toList()

    bufferedReader.close()

    // XYZ must contain at least one atom.
    if (xyzLines.count() < 3) {
        throw RuntimeException(
            "Not a valid XYZ format."
        )
    }

    if (xyzLines.first().toInt() != xyzLines.count() - 2) {
        throw RuntimeException(
            "Specified number of atoms does not match the actual number of " +
            "atoms in XYZ."
        )
    }

    val angstromUnit = UnitOfMeasure.parse("Ao")
    val whitespaceRegex = Regex("\\s+")

    val atoms = xyzLines.drop(2).mapIndexed { atomLineIndex, atomLine ->
        val columns = whitespaceRegex.split(atomLine).take(4)

        val position = Vector3D(
            columns.drop(1).map {
                Quantity.convertUnit(
                    it.toDouble(),
                    angstromUnit,
                    atomPosUnit
                )
            }.toDoubleArray()
        )

        Atom(
            Element(columns[0]),
            position,
            null,
            atomLineIndex + 1
        )
    }

    return Fragment(atoms.toList())
}
