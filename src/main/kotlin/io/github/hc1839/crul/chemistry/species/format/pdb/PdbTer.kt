package io.github.hc1839.crul.chemistry.species.format.pdb

import java.io.StringWriter

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.AtomIsland
import io.github.hc1839.crul.chemistry.species.Element

/**
 *  [PdbRecord.Type.TER] record from the PDB format as an [Atom].
 *
 *  Fields correspond to Version 3.3 of the PDB format.
 */
data class PdbTer(
    val serial: Int?,
    val resName: String?,
    val chainId: Char?,
    val resSeq: Int?,
    val iCode: Char?
) : PdbRecord
{
    override val type: PdbRecord.Type =
        PdbRecord.Type.TER

    /**
     *  Exports as [PdbRecord.Type.TER] record.
     *
     *  It does not have a trailing newline.
     */
    fun exportPdb(): String {
        val writer = StringWriter()

        writer.write(PdbRecord.Type.TER.paddedName)

        if (serial != null) {
            writer.write("%5d".format(serial))
        } else {
            writer.write("%5s".format(""))
        }

        writer.write("%6s".format(""))
        writer.write("%3s".format(resName ?: ""))
        writer.write("%1s".format(""))
        writer.write("%c".format(chainId ?: ' '))

        if (resSeq != null) {
            writer.write("%4d".format(resSeq))
        } else {
            writer.write("%4s".format(""))
        }

        writer.write("%c".format(iCode ?: ' '))

        writer.close()

        return writer.toString()
    }

    companion object {
        /**
         *  Parses a [PdbRecord.Type.TER] record.
         *
         *  @param input
         *      Single line that is the [PdbRecord.Type.TER] record in PDB. If
         *      it exceeds [PdbRecord.Type.TER.numColumns] characters, an
         *      exception is thrown.
         *
         *  @return
         *      [PdbTer].
         */
        @JvmStatic
        fun parsePdb(input: String): PdbTer {
            if (input.length > PdbRecord.Type.TER.numColumns) {
                throw IllegalArgumentException(
                    "Input exceeds ${PdbRecord.Type.TER.numColumns} " +
                    "characters."
                )
            }

            val paddedInput =
                "%-${PdbRecord.Type.TER.numColumns}s".format(input)

            if (!paddedInput.startsWith(PdbRecord.Type.TER.paddedName)) {
                throw IllegalArgumentException(
                    "First field is not '${PdbRecord.Type.TER.name}'."
                )
            }

            val serial = getTrimmedStringField(paddedInput, 6, 11)?.toInt()
            val resName = getTrimmedStringField(paddedInput, 17, 20)
            val chainId = getCharField(paddedInput, 21)
            val resSeq = getTrimmedStringField(paddedInput, 22, 26)?.toInt()
            val iCode = getCharField(paddedInput, 26)

            return PdbTer(
                serial = serial,
                resName = resName,
                chainId = chainId,
                resSeq = resSeq,
                iCode = iCode
            )
        }
    }
}
