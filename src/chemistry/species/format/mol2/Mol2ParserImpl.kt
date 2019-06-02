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

import java.io.BufferedReader
import java.io.Reader

/**
 *  Default implementation of [Mol2Parser].
 */
internal class Mol2ParserImpl :
    AbstractIterator<Mol2Parser.Event>,
    Mol2Parser
{
    /**
     *  Buffered reader from which Mol2 is read.
     */
    private val bufferedReader: BufferedReader

    /**
     *  Parse stack of lines for one token.
     */
    private val parseStack: MutableList<String>

    /**
     *  Lookahead line, or `null` if the reader has reached the end of the
     *  stream.
     */
    private var lookaheadLine: String?

    /**
     *  Record type of the section that the parser is currently in, or `null`
     *  if the parser has not parsed anything since its instantiation.
     */
    private var sectionType: TriposRecordType?

    /**
     *  List of lines as a token that the parser is currently at.
     *
     *  It is empty if the parser has not read in a group of lines that can be
     *  considered as a token.
     */
    private val currentToken: MutableList<String>

    /**
     *  @param reader
     *      Reader from which Mol2 is to be read.
     */
    constructor(reader: Reader) {
        this.parseStack = mutableListOf()
        this.sectionType = null
        this.currentToken = mutableListOf()

        val bufferedReader = BufferedReader(reader)
        this.bufferedReader = bufferedReader

        var line = bufferedReader.readLine()?.trim()

        while (line != null) {
            if (line.first() != '#' && line != "") {
                break
            }

            line = bufferedReader.readLine()?.trim()
        }

        this.lookaheadLine = line
    }

    override tailrec fun computeNext() {
        if (parseStack.isEmpty() && lookaheadLine == null) {
            done()
            return
        }

        // Clear the previously parsed token.
        currentToken.clear()

        /*
         *  Advance to the next line.
         */

        parseStack.add(lookaheadLine!!)

        var line = bufferedReader.readLine()?.trim()

        while (line != null) {
            if (line != "" && line.first() != '#') {
                break
            }

            line = bufferedReader.readLine()?.trim()
        }

        lookaheadLine = line

        /*
         *  Process the parse stack.
         */

        val rtiMatchResult = rtiRegex.find(parseStack.last())

        if (rtiMatchResult != null) {
            sectionType = enumValueOf<TriposRecordType>(
                rtiMatchResult.groupValues[1]
            )

            currentToken.add(parseStack.last())
            parseStack.clear()

            setNext(Mol2Parser.Event.RECORD_TYPE_INDICATOR)
        } else if (
            parseStack.count() == sectionType!!.numDataLines ||
            lookaheadLine == null ||
            rtiRegex.find(lookaheadLine!!) != null
        ) {
            currentToken.addAll(parseStack)
            parseStack.clear()

            setNext(Mol2Parser.Event.DATA_LINES_OF_RECORD)
        } else {
            computeNext()
        }
    }

    override fun getRecordType(): TriposRecordType {
        if (currentToken.count() != 1) {
            throw RuntimeException(
                "Parser is not at a record type indicator."
            )
        }

        val rtiMatchResult = rtiRegex.find(currentToken.single())

        if (rtiMatchResult == null) {
            throw RuntimeException(
                "Parser is not at a record type indicator."
            )
        }

        return enumValueOf<TriposRecordType>(
            rtiMatchResult.groupValues[1]
        )
    }

    override fun getDataLinesOfRecord(): List<String> {
        if (
            currentToken.count() == 1 &&
            rtiRegex.find(currentToken.single()) != null
        ) {
            throw RuntimeException(
                "Parser is not at the data lines of a record."
            )
        }

        return currentToken.toList()
    }

    companion object {
        /**
         *  Regular expression for matching an RTI.
         */
        private val rtiRegex: Regex =
            Regex("^@<TRIPOS>(.*)$")
    }
}
