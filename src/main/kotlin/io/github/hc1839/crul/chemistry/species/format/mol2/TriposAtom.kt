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

package io.github.hc1839.crul.chemistry.species.format.mol2

import java.io.Writer
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.AtomIsland
import io.github.hc1839.crul.chemistry.species.Element

/**
 *  Tripos `ATOM` record.
 *
 *  Parameter names correspond to those in the Mol2 format.
 *
 *  Element of an atom is determined from the leading one or two alphabetical
 *  characters of [atomType] or [atomName], where [atomType] takes priority
 *  since it is the technically correct field. [atomName] is then considered if
 *  [atomType] does not contain the element.
 *
 *  [position] must be in Angstroms.
 *
 *  @constructor
 */
data class TriposAtom @JvmOverloads constructor(
    val atomId: Int,
    val atomName: String?,
    override val position: Vector3D,
    val atomType: String? = null,
    val substId: Int? = null,
    val substName: String? = null,
    val charge: Double? = null,
    val statusBit: Set<StatusBit> = setOf()
) : TriposRecord,
    Atom
{
    override val element: Element

    init {
        // Values that might contain an element symbol. Atom type is placed
        // first in the list, because it is tested first.
        val elementInputStrings = listOf(atomType, atomName).filterNotNull()

        val matchResults = elementInputStrings
            .map { elementSymbolRegex.find(it) }
            .filterNotNull()

        if (matchResults.isEmpty()) {
            throw RuntimeException(
                "Atom type and atom name do not " +
                "contain leading alphabetical characters " +
                "of a possible element symbol."
            )
        }

        val possibleElementSymbols = matchResults.map {
            it.groupValues[1]
        }

        val elementSymbol = possibleElementSymbols.find {
            Element.isValidSymbol(it)
        }

        if (elementSymbol == null) {
            throw RuntimeException(
                "No valid element symbol found " +
                "in atom type or atom name."
            )
        }

        this.element = Element(elementSymbol)
    }

    override val island: AtomIsland<Atom> =
        AtomIsland(this)

    override val recordType: TriposRecordType =
        TriposRecordType.ATOM

    override fun exportMol2(writer: Writer) {
        // Treat an empty set of status bits as being omitted.
        val statusBitOpt = if (!statusBit.isEmpty()) {
            statusBit
        } else {
            null
        }

        var mol2RecordBuilder = listOf<String>()

        mol2RecordBuilder += atomId.toString()
        mol2RecordBuilder += atomName ?: TriposStringField.FOUR_STARS
        mol2RecordBuilder += position.getX().toString()
        mol2RecordBuilder += position.getY().toString()
        mol2RecordBuilder += position.getZ().toString()
        mol2RecordBuilder += atomType ?: TriposStringField.FOUR_STARS

        mol2RecordBuilder +=
            listOf(
                substId,
                substName,
                charge,
                statusBitOpt?.map { it.value }?.joinToString("|")
            )
            .dropLastWhile { it == null }
            .map { it?.toString() ?: TriposStringField.FOUR_STARS }

        writer.write(
            mol2RecordBuilder.joinToString(TriposRecord.FIELD_SEPARATOR)
        )

        writer.write("\n")
    }

    /**
     *  Builder for [TriposAtom].
     */
    class Builder() : TriposRecord.Builder {
        private var atomId: Int? = null

        private var atomName: String? = null

        private var position: Vector3D? = null

        private var atomType: String? = null

        private var substId: Int? = null

        private var substName: String? = null

        private var charge: Double? = null

        private var statusBit: Set<StatusBit>? = null

        override fun append(dataLine: String): Boolean {
            if (atomId != null) {
                return false
            }

            val fields = dataLine.trim().split(whitespaceDelimRegex)

            try {
                atomId = fields[0].toInt()
                atomName = TriposStringField.stringValueOf(fields[1])

                position = Vector3D(
                    fields[2].toDouble(),
                    fields[3].toDouble(),
                    fields[4].toDouble()
                )

                atomType = TriposStringField.stringValueOf(fields[5])

                substId = if (
                    fields.lastIndex >= 6 &&
                    fields[6] != TriposStringField.FOUR_STARS
                ) {
                    fields[6].toInt()
                } else {
                    null
                }

                substName = if (
                    fields.lastIndex >= 7 &&
                    fields[7] != TriposStringField.FOUR_STARS
                ) {
                    fields[7]
                } else {
                    null
                }

                charge = if (
                    fields.lastIndex >= 8 &&
                    fields[8] != TriposStringField.FOUR_STARS
                ) {
                    fields[8].toDouble()
                } else {
                    null
                }

                statusBit = if (fields.lastIndex >= 9) {
                    fields[9]
                        .split("|")
                        .map {
                            TriposStringField.enumValueOf<StatusBit>(it)!!
                        }
                        .toSet()
                } else {
                    setOf()
                }
            } catch (_: Throwable) {
                return false
            }

            return true
        }

        override fun build(): TriposAtom =
            TriposAtom(
                atomId = atomId!!,
                atomName = atomName,
                position = position!!,
                atomType = atomType,
                substId = substId,
                substName = substName,
                charge = charge,
                statusBit = statusBit!!
            )

        override fun new(): Builder =
            Builder()

        companion object {
            /**
             *  Regular expression for matching whitespaces as a delimiter.
             */
            private val whitespaceDelimRegex =
                Regex("\\s+")
        }
    }

    /**
     *  Internal SYBYL status bits associated with the atom.
     */
    enum class StatusBit : TriposStringField {
        DSPMOD,
        TYPECOL,
        CAP,
        BACKBONE,
        DICT,
        ESSENTIAL,
        WATER,
        DIRECT;

        override val value: String = name
    }

    companion object {
        /**
         *  Regular expression for matching the symbol of an element in
         *  the Tripos atom-type or atom-name field.
         */
        private val elementSymbolRegex =
            Regex("^([A-Z][a-z]?)")
    }
}
