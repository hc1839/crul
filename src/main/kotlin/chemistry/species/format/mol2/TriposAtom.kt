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

package crul.chemistry.species.format.mol2

/**
 *  Tripos `ATOM` record.
 *
 *  Parameter names correspond to those in the Mol2 format. [x], [y], and [z]
 *  are in Angstroms.
 *
 *  @constructor
 */
data class TriposAtom @JvmOverloads constructor(
    val atomId: Int,
    val atomName: String?,
    val x: Double,
    val y: Double,
    val z: Double,
    val atomType: String? = null,
    val substId: Int? = null,
    val substName: String? = null,
    val charge: Double? = null,
    val statusBit: StatusBit? = null
) : TriposRecord
{
    override val recordType: TriposRecordType =
        TriposRecordType.ATOM

    override fun exportMol2(): String {
        var mol2RecordBuilder = listOf<String>()

        mol2RecordBuilder += atomId.toString()
        mol2RecordBuilder += atomName ?: TriposStringField.FOUR_STARS
        mol2RecordBuilder += x.toString()
        mol2RecordBuilder += y.toString()
        mol2RecordBuilder += z.toString()
        mol2RecordBuilder += atomType ?: TriposStringField.FOUR_STARS

        mol2RecordBuilder +=
            listOf(
                substId,
                substName,
                charge,
                statusBit?.value
            )
            .dropLastWhile { it == null }
            .map { it?.toString() ?: TriposStringField.FOUR_STARS }

        return mol2RecordBuilder.joinToString(TriposRecord.FIELD_SEPARATOR)
    }

    /**
     *  Builder for [TriposAtom].
     */
    class Builder() : TriposRecord.Builder {
        private var atomId: Int? = null

        private var atomName: String? = null

        private var x: Double? = null

        private var y: Double? = null

        private var z: Double? = null

        private var atomType: String? = null

        private var substId: Int? = null

        private var substName: String? = null

        private var charge: Double? = null

        private var statusBit: StatusBit? = null

        override fun append(dataLine: String): Boolean {
            if (atomId != null) {
                return false
            }

            val fields = dataLine.trim().split(whitespaceDelimRegex)

            try {
                atomId = fields[0].toInt()
                atomName = TriposStringField.stringValueOf(fields[1])
                x = fields[2].toDouble()
                y = fields[3].toDouble()
                z = fields[4].toDouble()
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
                    TriposStringField.enumValueOf<StatusBit>(fields[9])
                } else {
                    null
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
                x = x!!,
                y = y!!,
                z = z!!,
                atomType = atomType,
                substId = substId,
                substName = substName,
                charge = charge,
                statusBit = statusBit
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
}
