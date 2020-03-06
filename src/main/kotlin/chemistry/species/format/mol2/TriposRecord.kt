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
 *  Data record associated with a record type indicator (RTI) in Mol2 format.
 */
interface TriposRecord {
    /**
     *  Record type.
     */
    val recordType: TriposRecordType

    /**
     *  Writes the Tripos record in Mol2 format without the record type
     *  indicator.
     *
     *  A trailing newline is written.
     */
    fun exportMol2(writer: Writer)

    /**
     *  Builder of a Tripos data record by data lines.
     */
    interface Builder {
        /**
         *  Appends a data line.
         *
         *  Instead of returning the builder when invoked, it returns whether a
         *  data line has been successfully appended. This avoids reparsing the
         *  same data line after simply determining whether it is appendable.
         *
         *  @param dataLine
         *      Data line to append.
         *
         *  @return
         *      `true` if `dataLine` has been successfully appended. `false` if
         *      `dataLine` cannot be appended based on previously appended
         *      ones.
         */
        fun append(dataLine: String): Boolean

        /**
         *  Builds the Tripos data record from the data lines that were
         *  appended.
         */
        fun build(): TriposRecord

        /**
         *  Creates a new builder for the same [TriposRecord].
         */
        fun new(): Builder
    }

    companion object {
        /**
         *  Separator of the fields of a Tripos record exported to Mol2 format.
         */
        @JvmField
        val FIELD_SEPARATOR: String = " "
    }
}
