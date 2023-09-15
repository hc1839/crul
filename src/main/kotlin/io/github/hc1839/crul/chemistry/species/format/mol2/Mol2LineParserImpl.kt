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

import java.io.BufferedReader
import java.io.Reader

/**
 *  Default implementation of [Mol2LineParser].
 *
 *  @constructor
 *
 *  @param reader
 *      Reader of Mol2.
 */
internal class Mol2LineParserImpl(reader: Reader) :
    AbstractIterator<Mol2LineParser.Event>(),
    Mol2LineParser
{
    /**
     *  Buffered reader of Mol2.
     */
    private val reader: BufferedReader =
        BufferedReader(reader)

    /**
     *  State of the parser, or `null` if nothing has been parsed.
     */
    private var state: Mol2LineParser.Event? = null

    /**
     *  Line in Mol2 where the parser is at.
     */
    private var line: String = ""

    override fun computeNext() {
        var lineBuffer = reader.readLine()?.trim()

        // Find the next line that is not blank and is not a comment.
        while (lineBuffer != null) {
            if (lineBuffer != "" && lineBuffer.first() != '#') {
                break
            }

            lineBuffer = reader.readLine()?.trim()
        }

        if (lineBuffer != null) {
            line = lineBuffer

            if (rtiRegex in line) {
                state = Mol2LineParser.Event.RECORD_TYPE_INDICATOR
            } else {
                state = Mol2LineParser.Event.DATA_LINE
            }

            setNext(state!!)
        } else {
            done()
        }
    }

    override fun close() {
        reader.close()
    }

    override fun getToken(): Any {
        if (state == null) {
            throw RuntimeException(
                "Parsing has not begun."
            )
        }

        return when (state!!) {
            Mol2LineParser.Event.RECORD_TYPE_INDICATOR ->
                enumValueOf<TriposRecordType>(
                    rtiRegex.find(line)!!.groupValues[1]
                )

            Mol2LineParser.Event.DATA_LINE ->
                line
        }
    }

    override fun getRecordType(): TriposRecordType {
        if (state != Mol2LineParser.Event.RECORD_TYPE_INDICATOR) {
            throw RuntimeException(
                "Parser is not at a record type indicator."
            )
        }

        return getToken() as TriposRecordType
    }

    override fun getDataLine(): String {
        if (state != Mol2LineParser.Event.DATA_LINE) {
            throw RuntimeException(
                "Parser is not at a data line."
            )
        }

        return getToken() as String
    }

    companion object {
        /**
         *  Regular expression for matching an RTI.
         */
        private val rtiRegex: Regex =
            Regex("^\\s*@<TRIPOS>(\\S+)\\s*$")
    }
}
