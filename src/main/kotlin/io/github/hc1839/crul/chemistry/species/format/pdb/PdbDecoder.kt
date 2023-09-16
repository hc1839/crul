package io.github.hc1839.crul.chemistry.species.format.pdb

import java.io.BufferedReader
import java.io.Closeable
import java.io.Reader

/**
 *  Streaming PDB record decoder that passes [PdbRecord] instances to a
 *  listener.
 *
 *  PDB record types that are not part of [PdbDecodingListener] are ignored.
 *
 *  @property listener
 *      Listener of [PdbRecord] instances.
 *
 *  @constructor
 *
 *  @param reader
 *      Reader of PDB.
 */
class PdbDecoder(
    reader: Reader,
    private val listener: PdbDecodingListener
) : Closeable
{
    /**
     *  Buffered reader of PDB.
     */
    private val bufferedReader: BufferedReader =
        BufferedReader(reader)

    /**
     *  Runs the decoding of the PDB records on the reader.
     */
    fun run() {
        for (line in bufferedReader.lines()) {
            val recordType = enumValues<PdbRecord.Type>().singleOrNull {
                line.take(PdbRecord.paddedNameLength).trim() == it.name
            }

            when (recordType) {
                PdbRecord.Type.ATOM -> listener.onAtom(
                    PdbAtom.parsePdb(line)
                )

                PdbRecord.Type.CONECT -> listener.onConect(
                    PdbConect.parsePdb(line)
                )

                PdbRecord.Type.TER -> listener.onTer(
                    PdbTer.parsePdb(line)
                )
            }
        }
    }

    override fun close() {
        bufferedReader.close()
    }
}
