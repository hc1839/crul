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

import java.io.Reader

/**
 *  Parser for forward, read-only access to the lines of Mol2 format.
 */
interface Mol2Parser : Iterator<Mol2Parser.Event> {
    /**
     *  Gets the record type.
     *
     *  If the parser state is not [Event.RECORD_TYPE_INDICATOR], an exception
     *  is raised.
     */
    fun getRecordType(): TriposRecordType

    /**
     *  Gets the data lines of a record.
     *
     *  If the parser state is not [Event.DATA_LINES_OF_RECORD], an exception
     *  is raised.
     */
    fun getDataLinesOfRecord(): List<String>

    /**
     *  Event from [Mol2Parser].
     *
     *  Each event corresponds to the last line parsed.
     */
    enum class Event {
        /**
         *  Record type indicator.
         */
        RECORD_TYPE_INDICATOR,

        /**
         *  Data lines of a record.
         */
        DATA_LINES_OF_RECORD
    }

    companion object {
        /**
         *  Constructs a new instance [Mol2Parser].
         *
         *  @param reader
         *      Reader from which Mol2 is to be read.
         */
        @JvmStatic
        fun newInstance(reader: Reader): Mol2Parser =
            Mol2ParserImpl(reader)
    }
}
