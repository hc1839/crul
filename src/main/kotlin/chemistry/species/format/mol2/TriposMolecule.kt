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

import java.io.Writer

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
    val statusBits: Set<StatusBit> = setOf(),
    val molComment: String? = null
) : TriposRecord
{
    override val recordType: TriposRecordType =
        TriposRecordType.MOLECULE

    override fun exportMol2(writer: Writer) {
        // Treat an empty set of status bits as being omitted.
        val statusBitsOpt = if (!statusBits.isEmpty()) {
            statusBits
        } else {
            null
        }

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
            statusBitsOpt?.map { it.value }?.joinToString("|") ?:
            TriposStringField.FOUR_STARS
        )
        mol2RecordBuilder.add(
            molComment ?: TriposStringField.FOUR_STARS
        )

        writer.write(
            mol2RecordBuilder.joinToString("\n")
        )

        writer.write("\n")
    }

    /**
     *  Builder for [TriposMolecule].
     */
    class Builder() : TriposRecord.Builder {
        private var state: State = State.BEGIN

        private var molName: String? = null

        private var numAtoms: Int? = null

        private var numBonds: Int? = null

        private var numSubst: Int? = null

        private var numFeat: Int? = null

        private var numSets: Int? = null

        private var molType: MolType? = null

        private var chargeType: ChargeType? = null

        private var statusBits: Set<StatusBit>? = null

        private var molComment: String? = null

        override fun append(dataLine: String): Boolean = when (state) {
            // Append molecule name.
            State.BEGIN -> {
                molName = TriposStringField.stringValueOf(
                    dataLine.trim()
                )

                state = State.MOL_NAME
                true
            }

            // Append the data line containing the number of atoms.
            State.MOL_NAME -> try {
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

                state = State.NUM_ATOMS
                true
            } catch (_: Throwable) {
                state = State.END
                false
            }

            // Append molecule type.
            State.NUM_ATOMS -> try {
                molType =
                    TriposStringField.enumValueOf<MolType>(dataLine)

                state = State.MOL_TYPE
                true
            } catch (_: Throwable) {
                state = State.END
                false
            }

            // Append charge type.
            State.MOL_TYPE -> try {
                chargeType =
                    TriposStringField.enumValueOf<ChargeType>(
                        dataLine
                    )

                state = State.CHARGE_TYPE
                true
            } catch (_: Throwable) {
                state = State.END
                false
            }

            // Append status bits.
            State.CHARGE_TYPE -> try {
                val statusBitsValue =
                    TriposStringField.stringValueOf(dataLine.trim())

                statusBits = statusBitsValue
                    ?.trim()
                    ?.split("|")
                    ?.map {
                        TriposStringField.enumValueOf<StatusBit>(it)!!
                    }
                    ?.toSet()

                state = State.STATUS_BITS
                true
            } catch (_: Throwable) {
                state = State.END
                false
            }

            // Append comment.
            State.STATUS_BITS -> {
                molComment =
                    TriposStringField.stringValueOf(dataLine.trim())

                state = State.MOL_COMMENT
                true
            }

            State.MOL_COMMENT -> {
                state = State.END
                false
            }

            State.END -> false
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
                statusBits = statusBits ?: setOf(),
                molComment = molComment
            )

        override fun new(): Builder =
            Builder()

        /**
         *  State of the builder.
         *
         *  It reflects the type of data line that was appended.
         */
        private enum class State {
            BEGIN,
            MOL_NAME,
            NUM_ATOMS,
            MOL_TYPE,
            CHARGE_TYPE,
            STATUS_BITS,
            MOL_COMMENT,
            END
        }

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
    enum class StatusBit : TriposStringField {
        SYSTEM,
        INVALID_CHARGES,
        ANALYZED,
        SUBSTITUTED,
        ALTERED,
        REF_ANGLE;

        override val value: String = name
    }
}
