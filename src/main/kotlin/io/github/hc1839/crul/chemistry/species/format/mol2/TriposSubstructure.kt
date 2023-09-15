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
 *  Tripos `SUBSTRUCTURE` record.
 *
 *  Parameter names correspond to those in the Mol2 format.
 *
 *  @constructor
 */
data class TriposSubstructure @JvmOverloads constructor(
    val substId: Int,
    val substName: String?,
    val rootAtom: Int,
    val substType: SubstType? = null,
    val dictType: Int? = null,
    val chain: String? = null,
    val subType: String? = null,
    val interBonds: Int? = null,
    val status: Set<StatusBit> = setOf(),
    val comment: String? = null
) : TriposRecord
{
    override val recordType: TriposRecordType =
        TriposRecordType.SUBSTRUCTURE

    override fun exportMol2(writer: Writer) {
        // Treat an empty set of status bits as being omitted.
        val statusBitsOpt = if (!status.isEmpty()) {
            status
        } else {
            null
        }

        var mol2RecordBuilder = listOf<String>()

        mol2RecordBuilder += substId.toString()
        mol2RecordBuilder += substName ?: TriposStringField.FOUR_STARS
        mol2RecordBuilder += rootAtom.toString()

        mol2RecordBuilder +=
            listOf(
                substType?.value,
                dictType,
                chain,
                subType,
                interBonds,
                statusBitsOpt?.map { it.value }?.joinToString("|"),
                comment
            )
            .dropLastWhile { it == null }
            .map { it?.toString() ?: TriposStringField.FOUR_STARS }

        writer.write(
            mol2RecordBuilder.joinToString(TriposRecord.FIELD_SEPARATOR)
        )

        writer.write("\n")
    }

    /**
     *  Builder for [TriposSubstructure].
     */
    class Builder() : TriposRecord.Builder {
        private var substId: Int? = null

        private var substName: String? = null

        private var rootAtom: Int? = null

        private var substType: SubstType? = null

        private var dictType: Int? = null

        private var chain: String? = null

        private var subType: String? = null

        private var interBonds: Int? = null

        private var status: Set<StatusBit>? = null

        private var comment: String? = null

        override fun append(dataLine: String): Boolean {
            if (substId != null) {
                return false
            }

            val fields = dataLine.trim().split(whitespaceDelimRegex)

            try {
                substId = fields[0].toInt()

                substName = if (fields[1] != TriposStringField.FOUR_STARS) {
                    fields[1]
                } else {
                    null
                }

                rootAtom = fields[2].toInt()

                substType = if (fields.lastIndex >= 3) {
                    TriposStringField.enumValueOf<SubstType>(fields[3])
                } else {
                    null
                }

                dictType = if (fields.lastIndex >= 4) {
                    fields[4].toInt()
                } else {
                    null
                }

                chain = if (fields.lastIndex >= 5) {
                    fields[5]
                } else {
                    null
                }

                subType = if (fields.lastIndex >= 6) {
                    fields[6]
                } else {
                    null
                }

                interBonds = if (fields.lastIndex >= 7) {
                    fields[7].toInt()
                } else {
                    null
                }

                status = if (fields.lastIndex >= 8) {
                    fields[8]
                        .split("|")
                        .map {
                            TriposStringField.enumValueOf<StatusBit>(it)!!
                        }
                        .toSet()
                } else {
                    setOf()
                }

                comment = if (fields.lastIndex >= 9) {
                    fields[9]
                } else {
                    null
                }
            } catch (_: Throwable) {
                return false
            }

            return true
        }

        override fun build(): TriposSubstructure =
            TriposSubstructure(
                substId = substId!!,
                substName = substName,
                rootAtom = rootAtom!!,
                substType = substType,
                dictType = dictType,
                chain = chain,
                subType = subType,
                interBonds = interBonds,
                status = status!!,
                comment = comment
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
     *  Substructure type.
     */
    enum class SubstType : TriposStringField {
        TEMP,
        PERM,
        RESIDUE,
        GROUP,
        DOMAIN;

        override val value: String = name.toLowerCase()
    }

    /**
     *  Internal SYBYL status bit.
     */
    enum class StatusBit : TriposStringField {
        LEAF,
        ROOT,
        TYPECOL,
        DICT,
        BACKWARD,
        BLOCK;

        override val value: String = name
    }
}
