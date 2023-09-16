package io.github.hc1839.crul.chemistry.species.format.pdb

import java.io.StringWriter

/**
 *  [PdbRecord.Type.CONECT] record from the PDB format.
 *
 *  Fields correspond to Version 3.3 of the PDB format.
 *
 *  @property serial
 *      Atom serial number.
 *
 *  @property serials
 *      Serial numbers of atoms bonded to [serial]. Number of elements must be
 *      between 1 and 4 (inclusive), it cannot contain [serial], and its
 *      elements must be unique.
 */
data class PdbConect(
    val serial: Int,
    val serials: List<Int>
) : PdbRecord
{
    init {
        if (this.serials.count() !in (1..4)) {
            throw IllegalArgumentException(
                "Number of serial numbers is not between 1 and 4, inclusive."
            )
        }

        if (this.serial in this.serials ||
            this.serials.distinct().count() != this.serials.count()
        ) {
            throw IllegalArgumentException(
                "Serial numbers not unique."
            )
        }
    }

    override val type: PdbRecord.Type =
        PdbRecord.Type.CONECT

    /**
     *  Exports as [PdbRecord.Type.CONECT] record.
     *
     *  It does not have a trailing newline.
     */
    fun exportPdb(): String {
        val writer = StringWriter()

        writer.write(PdbRecord.Type.CONECT.paddedName)

        writer.write("%5d".format(serial))

        for (bondedSerial in serials) {
            writer.write("%5d".format(bondedSerial))
        }

        writer.close()

        return "%-${PdbRecord.Type.CONECT.numColumns}s".format(
            writer.toString()
        )
    }

    companion object {
        /**
         *  Parses a [PdbRecord.Type.CONECT] record.
         *
         *  @param input
         *      Single line that is the [PdbRecord.Type.CONECT] record in PDB.
         *      If it exceeds [PdbRecord.Type.CONECT.numColumns] characters, an
         *      exception is thrown.
         *
         *  @return
         *      [PdbConect].
         */
        @JvmStatic
        fun parsePdb(input: String): PdbConect {
            if (input.length > PdbRecord.Type.CONECT.numColumns) {
                throw IllegalArgumentException(
                    "Input exceeds ${PdbRecord.Type.CONECT.numColumns} " +
                    "characters."
                )
            }

            val paddedInput =
                "%-${PdbRecord.Type.CONECT.numColumns}s".format(input)

            if (!paddedInput.startsWith(PdbRecord.Type.CONECT.paddedName)) {
                throw IllegalArgumentException(
                    "First field is not '${PdbRecord.Type.CONECT.name}'."
                )
            }

            val serial = getTrimmedStringField(paddedInput, 6, 11)!!.toInt()

            val bondedSerials = (11..26 step 5).mapNotNull { startIndex ->
                getTrimmedStringField(
                    paddedInput,
                    startIndex,
                    startIndex + 5
                )?.toInt()
            }

            return PdbConect(
                serial = serial,
                serials = bondedSerials
            )
        }
    }
}
