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

/**
 *  Tripos `BOND` record.
 *
 *  Parameter names correspond to those in the Mol2 format.
 *
 *  @constructor
 */
data class TriposBond @JvmOverloads constructor(
    val bondId: Int,
    val originAtomId: Int,
    val targetAtomId: Int,
    val bondType: BondType? = null,
    val statusBits: Set<StatusBit> = setOf()
) : TriposRecord
{
    override val recordType: TriposRecordType =
        TriposRecordType.BOND

    override fun exportMol2(writer: Writer) {
        // Treat an empty set of status bits as being omitted.
        val statusBitsOpt = if (!statusBits.isEmpty()) {
            statusBits
        } else {
            null
        }

        var mol2RecordBuilder = listOf<String>()

        mol2RecordBuilder += bondId.toString()
        mol2RecordBuilder += originAtomId.toString()
        mol2RecordBuilder += targetAtomId.toString()
        mol2RecordBuilder += bondType?.value ?: TriposStringField.FOUR_STARS

        if (statusBitsOpt != null) {
            mol2RecordBuilder += statusBitsOpt
                .map { it.value }
                .joinToString("|")
        }

        writer.write(
            mol2RecordBuilder.joinToString(TriposRecord.FIELD_SEPARATOR)
        )

        writer.write("\n")
    }

    /**
     *  Builder for [TriposBond].
     */
    class Builder() : TriposRecord.Builder {
        private var bondId: Int? = null

        private var originAtomId: Int? = null

        private var targetAtomId: Int? = null

        private var bondType: BondType? = null

        private var statusBits: Set<StatusBit>? = null

        override fun append(dataLine: String): Boolean {
            if (bondId != null) {
                return false
            }

            val fields = dataLine.trim().split(whitespaceDelimRegex)

            try {
                bondId = fields[0].toInt()
                originAtomId = fields[1].toInt()
                targetAtomId = fields[2].toInt()
                bondType = TriposStringField.enumValueOf<BondType>(fields[3])

                statusBits = if (fields.lastIndex >= 4) {
                    fields[4]
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

        override fun build(): TriposBond =
            TriposBond(
                bondId = bondId!!,
                originAtomId = originAtomId!!,
                targetAtomId = targetAtomId!!,
                bondType = bondType,
                statusBits = statusBits!!
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
     *  Bond type.
     */
    enum class BondType : TriposStringField {
        SINGLE {
            override val value: String = "1"
        },
        DOUBLE {
            override val value: String = "2"
        },
        TRIPLE {
            override val value: String = "3"
        },
        AMIDE {
            override val value: String = "am"
        },
        AROMATIC {
            override val value: String = "ar"
        },
        DUMMY {
            override val value: String = "du"
        },
        UNKNOWN {
            override val value: String = "un"
        },
        NOT_CONNECTED {
            override val value: String = "nc"
        }
    }

    /**
     *  Internal SYBYL status bits associated with the bond.
     */
    enum class StatusBit : TriposStringField {
        TYPECOL,
        GROUP,
        CAP,
        BACKBONE,
        DICT,
        INTERRES;

        override val value: String = name
    }
}
