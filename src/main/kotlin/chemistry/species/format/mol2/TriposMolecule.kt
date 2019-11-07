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
 *  Tripos `MOLECULE` record.
 *
 *  Parameter names correspond to those in the Mol2 format.
 *
 *  @constructor
 */
data class TriposMolecule @JvmOverloads constructor(
    val molName: String?,
    val numAtoms: Int,
    val numBonds: Int? = null,
    val numSubst: Int? = null,
    val numFeat: Int? = null,
    val numSets: Int? = null,
    val molType: MolType? = null,
    val chargeType: ChargeType? = null,
    val statusBits: StatusBits? = null,
    val molComment: String? = null
) : TriposRecord
{
    override val recordType: TriposRecordType =
        TriposRecordType.MOLECULE

    override fun exportMol2(): String {
        val mol2RecordBuilder = mutableListOf<String>()

        mol2RecordBuilder.add(molName ?: TriposStringField.FOUR_STARS)

        // Drop the trailing `null`s.
        mol2RecordBuilder.add(
            listOf(
                numAtoms,
                numBonds,
                numSubst,
                numFeat,
                numSets
            )
            .dropLastWhile { it == null }
            .map { it?.toString() ?: TriposStringField.FOUR_STARS }
            .joinToString(TriposRecord.FIELD_SEPARATOR)
        )

        mol2RecordBuilder.add(
            molType?.value ?: TriposStringField.FOUR_STARS
        )
        mol2RecordBuilder.add(
            chargeType?.value ?: TriposStringField.FOUR_STARS
        )
        mol2RecordBuilder.add(
            statusBits?.value ?: TriposStringField.FOUR_STARS
        )
        mol2RecordBuilder.add(
            molComment ?: TriposStringField.FOUR_STARS
        )

        return mol2RecordBuilder.joinToString("\n")
    }

    /**
     *  Molecule type.
     */
    enum class MolType : TriposStringField {
        SMALL,
        BIOPOLYMER,
        PROTEIN,
        NUCLEIC_ACID,
        SACCHARIDE;

        override val value: String = name
    }

    /**
     *  Type of charges associated with the molecule.
     */
    enum class ChargeType : TriposStringField {
        NO_CHARGES,
        DEL_RE,
        GASTEIGER,
        GAST_HUCK,
        HUCKEL,
        PULLMAN,
        GAUSS80_CHARGES,
        AMPAC_CHARGES,
        MULLIKEN_CHARGES,
        DICT_CHARGES,
        MMFF94_CHARGES,
        USER_CHARGES;

        override val value: String = name
    }

    /**
     *  Internal SYBYL status bits associated with the molecule.
     */
    enum class StatusBits : TriposStringField {
        SYSTEM,
        INVALID_CHARGES,
        ANALYZED,
        SUBSTITUTED,
        ALTERED,
        REF_ANGLE;

        override val value: String = name
    }

    companion object {
        /**
         *  Parses a `MOLECULE` record.
         *
         *  @param input
         *      Record in Mol2 format without comment and blank lines.
         *
         *  @return
         *      [TriposMolecule].
         */
        @JvmStatic
        fun parseMol2(input: String): TriposMolecule {
            val dataLines = input.split("\n").map { it.trim() }

            if (
                dataLines.count() >
                    TriposRecordType.MOLECULE.numDataLines
            ) {
                throw RuntimeException(
                    "Number of data lines is greater than " +
                    "${TriposRecordType.MOLECULE.numDataLines}."
                )
            }

            val whitespaceDelim = Regex("\\s+")

            val molName = TriposStringField.stringValueOf(dataLines[0])

            val numAtomsFields = whitespaceDelim
                .split(dataLines[1])
                .map {
                    if (it != TriposStringField.FOUR_STARS) {
                        it.toInt()
                    } else {
                        null
                    }
                }

            val numAtoms = numAtomsFields[0]!!
            val numBonds = numAtomsFields.getOrNull(1)
            val numSubst = numAtomsFields.getOrNull(2)
            val numFeat = numAtomsFields.getOrNull(3)
            val numSets = numAtomsFields.getOrNull(4)

            val molType =
                TriposStringField.enumValueOf<MolType>(dataLines[2])

            val chargeType =
                TriposStringField.enumValueOf<ChargeType>(dataLines[3])

            val statusBits = if (dataLines.lastIndex >= 4) {
                TriposStringField.enumValueOf<StatusBits>(dataLines[4])
            } else {
                null
            }

            val molComment = if (dataLines.lastIndex >= 5) {
                TriposStringField.stringValueOf(dataLines[5])
            } else {
                null
            }

            return TriposMolecule(
                molName = molName,
                numAtoms = numAtoms,
                numBonds = numBonds,
                numSubst = numSubst,
                numFeat = numFeat,
                numSets = numSets,
                molType = molType,
                chargeType = chargeType,
                statusBits = statusBits,
                molComment = molComment
            )
        }
    }
}