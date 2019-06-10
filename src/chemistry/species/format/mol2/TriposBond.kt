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
 *  Tripos `BOND` record.
 *
 *  Parameter names correspond to those in the Mol2 format.
 */
data class TriposBond @JvmOverloads constructor(
    val bondId: Int,
    val originAtomId: Int,
    val targetAtomId: Int,
    val bondType: BondType? = null,
    val statusBits: StatusBits? = null
) : TriposRecord
{
    override val recordType: TriposRecordType =
        TriposRecordType.BOND

    override fun exportMol2(): String {
        var mol2RecordBuilder = listOf<String>()

        mol2RecordBuilder += bondId.toString()
        mol2RecordBuilder += originAtomId.toString()
        mol2RecordBuilder += targetAtomId.toString()
        mol2RecordBuilder += bondType?.value ?: TriposStringField.FOUR_STARS

        if (statusBits != null) {
            mol2RecordBuilder += statusBits.value
        }

        return mol2RecordBuilder.joinToString(TriposRecord.FIELD_SEPARATOR)
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
    enum class StatusBits : TriposStringField {
        TYPECOL,
        GROUP,
        CAP,
        BACKBONE,
        DICT,
        INTERRES;

        override val value: String = name
    }

    companion object {
        /**
         *  Parses a `BOND` record.
         *
         *  @param input
         *      Record in Mol2 format without comment and blank lines.
         *
         *  @return
         *      [TriposBond].
         */
        @JvmStatic
        fun parseMol2(input: String): TriposBond {
            if (
                input.split("\n").count() >
                    TriposRecordType.BOND.numDataLines
            ) {
                throw RuntimeException(
                    "Number of data lines is greater than " +
                    "${TriposRecordType.BOND.numDataLines}."
                )
            }

            val fields = Regex("\\s+").split(input.trim())

            val bondId = fields[0].toInt()
            val originAtomId = fields[1].toInt()
            val targetAtomId = fields[2].toInt()
            val bondType = TriposStringField.enumValueOf<BondType>(fields[3])

            val statusBits = if (fields.lastIndex >= 4) {
                TriposStringField.enumValueOf<StatusBits>(fields[4])
            } else {
                null
            }

            return TriposBond(
                bondId = bondId,
                originAtomId = originAtomId,
                targetAtomId = targetAtomId,
                bondType = bondType,
                statusBits = statusBits
            )
        }
    }
}
