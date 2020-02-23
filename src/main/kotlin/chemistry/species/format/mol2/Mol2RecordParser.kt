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
 *  Streaming record parser of Mol2.
 *
 *  It is a parser that is higher level than [Mol2LineParser] by parsing Mol2
 *  in tokens of [TriposRecord]. Only supported Tripos record types (where
 *  [TriposRecordType.createBuilder] does not return `null`) are iterated. A
 *  supported Tripos record type indicator followed by no data lines is
 *  considered to be invalid. The behavior of encountering an unsupported
 *  Tripos record type indicator followed by no data lines is
 *  implementation-dependent.
 */
interface Mol2RecordParser :
    Iterator<TriposRecordType>,
    Closeable
{
    /**
     *  Gets the Tripos record that the parser is at without advancing the
     *  parser.
     *
     *  If the parsing has not begun, an exception is thrown.
     */
    fun getRecord(): TriposRecord

    companion object {
        /**
         *  Constructs a new [Mol2RecordParser].
         *
         *  @param reader
         *      Reader of Mol2.
         */
        @JvmStatic
        fun newInstance(reader: Reader): Mol2RecordParser =
            Mol2RecordParser(reader)
    }
}

/**
 *  See [Mol2RecordParser.newInstance].
 */
fun Mol2RecordParser(reader: Reader): Mol2RecordParser =
    Mol2RecordParserImpl(reader)
