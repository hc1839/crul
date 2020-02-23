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

import java.io.Closeable
import java.io.Reader

/**
 *  Streaming line parser of Mol2 format.
 */
interface Mol2LineParser :
    Iterator<Mol2LineParser.Event>,
    Closeable
{
    /**
     *  Gets the token that the parser is at without advancing the parser.
     *
     *  Return type is [TriposRecordType] or `String`.
     */
    fun getToken(): Any

    /**
     *  Gets the type of Tripod record from the record type indicator that the
     *  parser is at without advancing the parser.
     *
     *  It is the same as `getToken() as TriposRecordType`.
     *
     *  If the parser state is not [Event.RECORD_TYPE_INDICATOR], an exception
     *  is thrown.
     */
    fun getRecordType(): TriposRecordType

    /**
     *  Gets the data line that the parser is at without advancing the parser.
     *
     *  It is the same as `getToken() as String`.
     *
     *  If the parser state is not [Event.DATA_LINE], an exception is thrown.
     */
    fun getDataLine(): String

    /**
     *  Event from [Mol2LineParser].
     */
    enum class Event {
        /**
         *  Record type indicator.
         */
        RECORD_TYPE_INDICATOR,

        /**
         *  Data line.
         */
        DATA_LINE
    }

    companion object {
        /**
         *  Constructs a new [Mol2LineParser].
         *
         *  @param reader
         *      Reader of Mol2.
         */
        @JvmStatic
        fun newInstance(reader: Reader): Mol2LineParser =
            Mol2LineParser(reader)
    }
}

/**
 *  See [Mol2LineParser.newInstance].
 */
fun Mol2LineParser(reader: Reader): Mol2LineParser =
    Mol2LineParserImpl(reader)
