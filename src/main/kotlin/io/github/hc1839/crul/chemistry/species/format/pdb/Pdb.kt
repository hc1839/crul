@file:JvmName("Pdb")
@file:JvmMultifileClass

package io.github.hc1839.crul.chemistry.species.format.pdb

import java.io.Reader
import java.io.Writer
import kotlin.math.roundToInt
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.Bond
import io.github.hc1839.crul.chemistry.species.BondAggregator
import io.github.hc1839.crul.chemistry.species.Element
import io.github.hc1839.crul.chemistry.species.Fragment
import io.github.hc1839.crul.chemistry.species.FragmentedSupermolecule
import io.github.hc1839.crul.chemistry.species.Island
import io.github.hc1839.crul.chemistry.species.Molecule
import io.github.hc1839.crul.chemistry.species.Supermolecule
import io.github.hc1839.crul.distinct.Referential
import io.github.hc1839.crul.measure.Quantity
import io.github.hc1839.crul.measure.dimension.Dimension
import io.github.hc1839.crul.measure.unit.UnitOfMeasure

/**
 *  Gets the PDB field as a trimmed string in `recordLine` between `startIndex`
 *  (inclusive) and `endIndex` (exclusive), or `null` if the field is blank.
 *
 *  Indices are zero-based.
 */
internal fun getTrimmedStringField(
    recordLine: String,
    startIndex: Int,
    endIndex: Int
): String?
{
    val field = recordLine.substring(startIndex, endIndex)

    return if (field.isNotBlank()) {
        field.trim()
    } else {
        null
    }
}

/**
 *  Gets the PDB field as a character in `recordLine` at zero-based `index`, or
 *  `null` if the field is blank.
 */
internal fun getCharField(recordLine: String, index: Int): Char? {
    val field = recordLine[index]

    return if (field != ' ') {
        field
    } else {
        null
    }
}

/**
 *  Exports a supermolecule in PDB format.
 *
 *  Atom serial numbers must be unique. Only [PdbRecord.Type.ATOM] records are
 *  exported.
 *
 *  @param writer
 *      Writer of PDB. A trailing newline is written.
 */
fun Supermolecule<PdbAtom>.exportPdb(writer: Writer) {
    if (atoms.map { it.serial }.distinct().count() != atoms.count()) {
        throw IllegalArgumentException(
            "Atom serial numbers not unique."
        )
    }

    // Write `ATOM` records ordered by serial number.
    for (atom in this.atoms.sortedBy { it.serial }) {
        writer.write(atom.exportPdb() + "\n")
    }

    writer.write("%-6s".format("END"))

    writer.flush()
}

/**
 *  Parses PDB as a supermolecule.
 *
 *  Records other than [PdbRecord.Type.ATOM] and [PdbRecord.Type.TER] are
 *  ignored.
 *
 *  Since [PdbRecord.Type.CONECT] records are not explicit for all bonds, they
 *  are ignored. Connectivities are never inferred. As such, the returned
 *  supermolecule contains no bonds.
 *
 *  [PdbRecord.Type.TER] records, if present, are used to group the atoms into
 *  fragments. If not such records are present, the supermolecule is the
 *  fragment.
 *
 *  @param reader
 *      Reader of PDB.
 *
 *  @param supermolName
 *      Arbitrary name of the supermolecule, or `null` if not applicable.
 *
 *  @return
 *      Supermolecule with no bonds.
 */
fun FragmentedSupermolecule.Companion.parsePdb(
    reader: Reader,
    supermolName: String?
): FragmentedSupermolecule<PdbAtom>
{
    val atomBySerial = mutableMapOf<Int, PdbAtom>()
    val fragmentGroups = mutableListOf(mutableListOf<PdbAtom>())

    val processor = object : PdbDecodingListener {
        override fun onAtom(record: PdbAtom) {
            if (atomBySerial.containsKey(record.serial)) {
                throw RuntimeException(
                    "Atom serial number for '${PdbRecord.Type.ATOM.name}' " +
                    "is not unique: ${record.serial}"
                )
            }

            atomBySerial[record.serial] = record
            fragmentGroups.last().add(record)
        }

        override fun onTer(record: PdbTer) {
            fragmentGroups.add(mutableListOf())
        }
    }

    PdbDecoder(reader, processor).use { it.run() }

    if (fragmentGroups.last().isEmpty()) {
        fragmentGroups.removeAt(fragmentGroups.lastIndex)
    }

    val atomIslands = atomBySerial.values.map {
        @Suppress("UNCHECKED_CAST")
        it.island as Island<PdbAtom>
    }

    val fragments = fragmentGroups.map { Fragment(it) }

    return FragmentedSupermolecule(
        Supermolecule(atomIslands),
        fragments,
        supermolName
    )
}
