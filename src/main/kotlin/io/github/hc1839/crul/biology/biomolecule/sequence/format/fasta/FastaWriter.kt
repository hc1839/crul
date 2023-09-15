package io.github.hc1839.crul.biology.biomolecule.sequence.format.fasta

import java.io.Closeable
import java.io.Writer

import io.github.hc1839.crul.biology.biomolecule.sequence.BioSequenceUnit

/**
 *  Writer of FASTA.
 *
 *  @property writer
 *      Writer to output FASTA.
 *
 *  @constructor
 */
class FastaWriter(private val writer: Writer) : Closeable {
    /**
     *  Whether the first description line has been written.
     */
    private var hasWrittenDefline: Boolean = false

    /**
     *  Writes `defline` with a '>' prepended as a description line.
     *
     *  A trailing newline is written.
     */
    fun writeDefline(defline: String) {
        if (hasWrittenDefline) {
            writer.write("\n")
        }

        writer.write(">" + defline + "\n")

        hasWrittenDefline = true
    }

    /**
     *  Writes the code of a sequence unit as a sequence character.
     *
     *  If no description line has been written, an exception is thrown.
     */
    fun writeSequenceChar(seqUnit: BioSequenceUnit) {
        if (!hasWrittenDefline) {
            throw RuntimeException(
                "No description line has been written."
            )
        }

        writer.append(seqUnit.code)
    }

    /**
     *  Writes the codes of a list of sequence units.
     *
     *  If no description line has been written, an exception is thrown.
     */
    fun writeSequenceChars(seqUnits: List<BioSequenceUnit>) {
        if (!hasWrittenDefline) {
            throw RuntimeException(
                "No description line has been written."
            )
        }

        writer.write(seqUnits.map { it.code }.joinToString(""))
    }

    /**
     *  A trailing newline is written before closing the underlying writer.
     */
    override fun close() {
        writer.write("\n")
        writer.close()
    }
}
