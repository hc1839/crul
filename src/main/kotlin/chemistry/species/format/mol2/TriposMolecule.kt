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
     *  Builder for [TriposMolecule].
     */
    class Builder() : TriposRecord.Builder {
        private var molName: String? = null

        private var numAtoms: Int? = null

        private var numBonds: Int? = null

        private var numSubst: Int? = null

        private var numFeat: Int? = null

        private var numSets: Int? = null

        private var molType: MolType? = null

        private var chargeType: ChargeType? = null

        private var statusBits: StatusBits? = null

        private var molComment: String? = null

        override fun append(dataLine: String): Boolean {
            if (molComment != null) {
                return false
            }

            // Append comment.
            if (statusBits != null) {
                molComment = dataLine.trim()
                return true
            }

            // Append status bits.
            if (chargeType != null) {
                statusBits = try {
                    TriposStringField.enumValueOf<StatusBits>(dataLine)
                } catch (_: Throwable) {
                    null
                }

                return statusBits != null
            }

            // Append charge type.
            if (molType != null) {
                chargeType = try {
                    TriposStringField.enumValueOf<ChargeType>(dataLine)
                } catch (_: Throwable) {
                    null
                }

                return chargeType != null
            }

            // Append molecule type.
            if (numAtoms != null) {
                molType = try {
                    TriposStringField.enumValueOf<MolType>(dataLine)
                } catch (_: Throwable) {
                    null
                }

                return molType != null
            }

            // Append the data line containing the number of atoms.
            if (molName != null) {
                try {
                    val fields = dataLine
                        .trim()
                        .split(whitespaceDelimRegex)
                        .map {
                            if (it != TriposStringField.FOUR_STARS) {
                                it.toInt()
                            } else {
                                null
                            }
                        }

                    numAtoms = fields[0]!!
                    numBonds = fields.getOrNull(1)
                    numSubst = fields.getOrNull(2)
                    numFeat = fields.getOrNull(3)
                    numSets = fields.getOrNull(4)

                    return true
                } catch (_: Throwable) {
                    return false
                }
            }

            // Append molecule name.
            molName = dataLine.trim()

            return true
        }

        override fun build(): TriposMolecule =
            TriposMolecule(
                molName = molName,
                numAtoms = numAtoms!!,
                numBonds = numBonds,
                numSubst = numSubst,
                numFeat = numFeat,
                numSets = numSets,
                molType = molType,
                chargeType = chargeType,
                statusBits = statusBits,
                molComment = molComment
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
}
