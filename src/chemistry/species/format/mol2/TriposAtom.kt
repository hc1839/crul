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
 *  Parameter names correspond to those in the Mol2 format. `x`, `y`, and `z`
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
    init {
        if (substName != null && substId == null) {
            throw IllegalArgumentException(
                "'substName' is not null, but 'substId' is."
            )
        }

        if (charge != null && substName == null) {
            throw IllegalArgumentException(
                "'charge' is not null, but 'substName' is."
            )
        }

        if (statusBit != null && charge == null) {
            throw IllegalArgumentException(
                "'statusBit' is not null, but 'charge' is."
            )
        }
    }

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
            .map { it!!.toString() }

        return mol2RecordBuilder.joinToString(TriposRecord.FIELD_SEPARATOR)
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
         *  Parses an `ATOM` record.
         *
         *  @param input
         *      Record in Mol2 format without comment and blank lines.
         *
         *  @return
         *      [TriposAtom].
         */
        @JvmStatic
        fun parseMol2(input: String): TriposAtom {
            if (
                input.split("\n").count() >
                    TriposRecordType.ATOM.numDataLines
            ) {
                throw RuntimeException(
                    "Number of data lines is greater than " +
                    "${TriposRecordType.ATOM.numDataLines}."
                )
            }

            val fields = Regex("\\s+").split(input.trim())

            val atomId = fields[0].toInt()
            val atomName = TriposStringField.stringValueOf(fields[1])
            val x = fields[2].toDouble()
            val y = fields[3].toDouble()
            val z = fields[4].toDouble()
            val atomType = TriposStringField.stringValueOf(fields[5])

            val substId = if (fields.lastIndex >= 6) {
                fields[6].toInt()
            } else {
                null
            }

            val substName = if (fields.lastIndex >= 7) {
                fields[7]
            } else {
                null
            }

            val charge = if (fields.lastIndex >= 8) {
                fields[8].toDouble()
            } else {
                null
            }

            val statusBit = if (fields.lastIndex >= 9) {
                TriposStringField.enumValueOf<StatusBit>(fields[9])
            } else {
                null
            }

            return TriposAtom(
                atomId = atomId,
                atomName = atomName,
                x = x,
                y = y,
                z = z,
                atomType = atomType,
                substId = substId,
                substName = substName,
                charge = charge,
                statusBit = statusBit
            )
        }
    }
}
