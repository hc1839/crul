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
 *  Data record associated with a record type indicator (RTI) in Mol2 format.
 */
interface TriposRecord {
    /**
     *  Record type.
     */
    val recordType: TriposRecordType

    /**
     *  Exports the record to Mol2 format.
     *
     *  It does not include the record type indicator and does not have a
     *  trailing newline.
     */
    fun exportMol2(): String

    companion object {
        /**
         *  Separator of the fields of a Tripos record exported to Mol2 format.
         */
        @JvmField
        val FIELD_SEPARATOR: String = " "
    }
}
