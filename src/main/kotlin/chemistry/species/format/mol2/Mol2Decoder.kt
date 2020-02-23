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
 *  Streaming Mol2 record decoder that passes [TriposRecord] instances to a
 *  listener.
 *
 *  Tripos record types that are not part of [Mol2DecodingListener] are
 *  ignored.
 *
 *  @property listener
 *      Listener of [TriposRecord] instances.
 *
 *  @constructor
 *
 *  @param reader
 *      Reader of Mol2.
 */
class Mol2Decoder(
    reader: Reader,
    private val listener: Mol2DecodingListener
) : Closeable
{
    /**
     *  Mol2 record parser.
     */
    private val recordParser: Mol2RecordParser =
        Mol2RecordParser(reader)

    /**
     *  Runs the decoding of the Tripos records on the Mol2 reader.
     */
    fun run() {
        for (recordType in recordParser) {
            when (recordType) {
                TriposRecordType.MOLECULE -> listener.onMolecule(
                    recordParser.getRecord() as TriposMolecule
                )

                TriposRecordType.ATOM -> listener.onAtom(
                    recordParser.getRecord() as TriposAtom
                )

                TriposRecordType.BOND -> listener.onBond(
                    recordParser.getRecord() as TriposBond
                )

                else -> { }
            }
        }
    }

    override fun close() {
        recordParser.close()
    }
}
