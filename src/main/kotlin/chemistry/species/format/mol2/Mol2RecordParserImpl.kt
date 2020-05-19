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
 *  Default implementation of [Mol2RecordParser].
 *
 *  @constructor
 *
 *  @param reader
 *      Reader of Mol2.
 */
internal class Mol2RecordParserImpl(reader: Reader) :
    AbstractIterator<TriposRecordType>(),
    Mol2RecordParser
{
    /**
     *  Mol2 line parser.
     */
    private val lineParser: Mol2LineParser =
        Mol2LineParser(reader)

    /**
     *  Builder of the current Tripos record, or `null` if parsing has not
     *  begun.
     */
    private var recordBuilder: TriposRecord.Builder? = null

    /**
     *  Whether the first data line of the current section has been
     *  encountered.
     *
     *  It is used to skip empty sections.
     */
    private var hasEncounteredDataLine: Boolean = false

    /**
     *  Tripos record, or `null` if parsing has not begun.
     */
    private var record: TriposRecord? = null

    override tailrec fun computeNext() {
        if (!lineParser.hasNext()) {
            if (recordBuilder != null && hasEncounteredDataLine) {
                record = recordBuilder!!.build()
                setNext(record!!.recordType)
                recordBuilder = null
            } else {
                done()
            }

            return
        }

        val lineParserEvent = lineParser.next()

        when (lineParserEvent) {
            Mol2LineParser.Event.RECORD_TYPE_INDICATOR -> {
                if (recordBuilder == null) {
                    // Create the builder for the next record, or `null` if the
                    // record type is unsupported.
                    recordBuilder = lineParser
                        .getRecordType()
                        .createBuilder()

                    hasEncounteredDataLine = false

                    computeNext()
                } else {
                    if (hasEncounteredDataLine) {
                        // Build the Tripos record for the previous record
                        // type.
                        record = recordBuilder!!.build()
                        setNext(record!!.recordType)
                    }

                    // Create the builder for the next record, or `null` if the
                    // record type is unsupported.
                    recordBuilder = lineParser
                        .getRecordType()
                        .createBuilder()

                    hasEncounteredDataLine = false
                }
            }

            Mol2LineParser.Event.DATA_LINE -> {
                hasEncounteredDataLine = true

                val dataLine = lineParser.getDataLine()

                // Append data lines for the current record.
                if (recordBuilder == null ||
                    recordBuilder!!.append(dataLine)
                ) {
                    computeNext()
                } else {
                    // Another record of the same type is encountered. Build
                    // the previous record from the appended data lines.
                    record = recordBuilder!!.build()
                    setNext(record!!.recordType)

                    recordBuilder = recordBuilder!!.new()

                    // Initialize the next builder for the same record type by
                    // appending the current data line to it.
                    if (!recordBuilder!!.append(dataLine)) {
                        throw RuntimeException(
                            "Unrecognized data line " +
                            "for ${record!!.recordType}:\n" +
                            dataLine
                        )
                    }
                }
            }
        }
    }

    override fun close() {
        lineParser.close()
    }

    override fun getRecord(): TriposRecord =
        record ?: throw RuntimeException(
            "Parsing has not begun."
        )
}
