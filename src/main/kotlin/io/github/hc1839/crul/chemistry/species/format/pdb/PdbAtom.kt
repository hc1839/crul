package io.github.hc1839.crul.chemistry.species.format.pdb

import kotlin.math.roundToInt

import java.io.StringWriter
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.AtomIsland
import io.github.hc1839.crul.chemistry.species.Element

/**
 *  [PdbRecord.Type.ATOM] record from the PDB format as an [Atom].
 *
 *  Fields correspond to Version 3.3 of the PDB format.
 */
data class PdbAtom(
    val serial: Int,
    val name: String?,
    val altLoc: Char?,
    val resName: String?,
    val chainId: Char?,
    val resSeq: Int?,
    val iCode: Char?,
    override val position: Vector3D,
    val occupancy: Double?,
    val tempFactor: Double?,
    override val element: Element,
    val charge: Double?
) : PdbRecord,
    Atom
{
    override val type: PdbRecord.Type =
        PdbRecord.Type.ATOM

    override val island: AtomIsland<Atom> =
        AtomIsland(this)

    /**
     *  Exports as [PdbRecord.Type.ATOM] record.
     *
     *  It does not have a trailing newline.
     */
    fun exportPdb(): String {
        val writer = StringWriter()

        writer.write(PdbRecord.Type.ATOM.paddedName)

        writer.write("%5d".format(serial))

        writer.write("%1s".format(""))
        writer.write("%4s".format(name ?: ""))
        writer.write("%c".format(altLoc ?: ' '))
        writer.write("%3s".format(resName ?: ""))
        writer.write("%1s".format(""))
        writer.write("%c".format(chainId ?: ' '))

        if (resSeq != null) {
            writer.write("%4d".format(resSeq))
        } else {
            writer.write("%4s".format(""))
        }

        writer.write("%c".format(iCode ?: ' '))
        writer.write("%3s".format(""))
        writer.write("%8.3f".format(position.getX()))
        writer.write("%8.3f".format(position.getY()))
        writer.write("%8.3f".format(position.getZ()))

        if (occupancy != null) {
            writer.write("%6.2f".format(occupancy))
        } else {
            writer.write("%6s".format(""))
        }

        if (tempFactor != null) {
            writer.write("%6.2f".format(tempFactor))
        } else {
            writer.write("%6s".format(""))
        }

        writer.write("%10s".format(""))
        writer.write("%2s".format(element.symbol))

        if (charge != null) {
            writer.write("%2d".format(charge.roundToInt()))
        } else {
            writer.write("%2s".format(""))
        }

        writer.close()

        return writer.toString()
    }

    companion object {
        /**
         *  Parses a [PdbRecord.Type.ATOM] record.
         *
         *  @param input
         *      Single line that is the [PdbRecord.Type.ATOM] record in PDB. If
         *      it exceeds [PdbRecord.Type.ATOM.numColumns] characters, an
         *      exception is thrown.
         *
         *  @return
         *      [PdbAtom].
         */
        @JvmStatic
        fun parsePdb(input: String): PdbAtom {
            if (input.length > PdbRecord.Type.ATOM.numColumns) {
                throw IllegalArgumentException(
                    "Input exceeds ${PdbRecord.Type.ATOM.numColumns} " +
                    "characters."
                )
            }

            val paddedInput =
                "%-${PdbRecord.Type.ATOM.numColumns}s".format(input)

            if (!paddedInput.startsWith(PdbRecord.Type.ATOM.paddedName)) {
                throw IllegalArgumentException(
                    "First field is not '${PdbRecord.Type.ATOM.name}'."
                )
            }

            val serial = getTrimmedStringField(paddedInput, 6, 11)!!.toInt()
            val name = getTrimmedStringField(paddedInput, 12, 16)
            val altLoc = getCharField(paddedInput, 16)
            val resName = getTrimmedStringField(paddedInput, 17, 20)
            val chainId = getCharField(paddedInput, 21)
            val resSeq = getTrimmedStringField(paddedInput, 22, 26)?.toInt()
            val iCode = getCharField(paddedInput, 26)

            val position = Vector3D(
                getTrimmedStringField(paddedInput, 30, 38)!!.toDouble(),
                getTrimmedStringField(paddedInput, 38, 46)!!.toDouble(),
                getTrimmedStringField(paddedInput, 46, 54)!!.toDouble()
            )

            val occupancy = getTrimmedStringField(paddedInput, 54, 60)?.toDouble()
            val tempFactor = getTrimmedStringField(paddedInput, 60, 66)?.toDouble()

            val element = Element(
                getTrimmedStringField(paddedInput, 76, 78)!!
                    .toLowerCase()
                    .capitalize()
            )

            val charge = getTrimmedStringField(paddedInput, 78, 80)?.toDouble()

            return PdbAtom(
                serial = serial,
                name =  name,
                altLoc = altLoc,
                resName = resName,
                chainId = chainId,
                resSeq = resSeq,
                iCode = iCode,
                position = position,
                occupancy = occupancy,
                tempFactor = tempFactor,
                element = element,
                charge = charge
            )
        }
    }
}
