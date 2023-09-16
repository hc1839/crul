@file:JvmName("Txyz")
@file:JvmMultifileClass

package io.github.hc1839.crul.chemistry.tinker.species.format.txyz

import java.io.BufferedReader
import java.io.Reader
import java.io.Writer
import kotlin.text.Regex
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.chemistry.species.AtomIsland
import io.github.hc1839.crul.chemistry.species.Bond
import io.github.hc1839.crul.chemistry.species.BondAggregator
import io.github.hc1839.crul.chemistry.species.Element
import io.github.hc1839.crul.chemistry.species.Molecule
import io.github.hc1839.crul.chemistry.species.Supermolecule
import io.github.hc1839.crul.distinct.Referential
import io.github.hc1839.crul.measure.Quantity
import io.github.hc1839.crul.measure.dimension.BaseDimension
import io.github.hc1839.crul.measure.dimension.Dimension
import io.github.hc1839.crul.measure.unit.UnitOfMeasure

/**
 *  Exports a supermolecule in TXYZ format.
 *
 *  @param writer
 *      Writer of TXYZ with a trailing newline.
 *
 *  @param label
 *      String to use as the label in TXYZ. It can be empty.
 *
 *  @param atomPosUnit
 *      Unit of the coordinates that the atom positions are in. It must be a
 *      unit of `L`.
 */
fun Supermolecule<TinkerAtom>.exportTxyz(
    writer: Writer,
    label: String,
    atomPosUnit: UnitOfMeasure
) {
    if (!atomPosUnit.isUnitOf(Dimension.parse("L"))) {
        throw IllegalArgumentException(
            "Unit of atom position is not a unit of length."
        )
    }

    writer.write(atoms.count().toString())

    val trimmedLabel = label.trim()

    if (!trimmedLabel.isEmpty()) {
        writer.write(" " + trimmedLabel)
    }

    writer.write("\n")

    val angstromUnit = UnitOfMeasure.parse("Ao")

    // Add each atom as a row in the output.
    for (atom in atoms.sortedBy { it.atomId }) {
        writer.write(atom.atomId.toString() + " ")
        writer.write(atom.element.symbol + " ")

        // Write the position components of the atom.
        for (cmpt in atom.position.toArray()) {
            writer.write(
                Quantity.convertUnit(
                    cmpt,
                    atomPosUnit,
                    angstromUnit
                ).toString()
            )

            writer.write(" ")
        }

        writer.write(atom.atomType.toString())

        // Get indices of the atoms bonded to this atom.
        val partnerIds = getIslandWithAtom(atom)
            .getAtomsBondedTo(atom)
            .map { it.atomId }

        if (!partnerIds.isEmpty()) {
            writer.write(" ")
        }

        writer.write(
            partnerIds.sorted().map { it.toString() }.joinToString(" ")
        )

        writer.write("\n")
    }
}

/**
 *  Parses TXYZ format.
 *
 *  @param reader
 *      Reader from which TXYZ is to be read.
 *
 *  @param atomPosUnit
 *      Unit of the coordinates that the positions of the deserialized atoms
 *      are in. It must be a unit of `L`.
 *
 *  @return
 *      Supermolecule deserialized from the TXYZ reader. Atom tags are set to
 *      the corresponding atom numbers in TXYZ. Bond orders are set to the
 *      empty string.
 */
fun Supermolecule.Companion.parseTxyz(
    reader: Reader,
    atomPosUnit: UnitOfMeasure
): Supermolecule<TinkerAtom>
{
    if (!atomPosUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
        throw IllegalArgumentException(
            "Unit of the coordinates is not a unit of length."
        )
    }

    val commentRegex = Regex("^\\s*#")
    val bufferedReader = BufferedReader(reader)

    val txyzLines = bufferedReader
        .lines()
        .iterator()
        .asSequence()
        .map { it.trim() }
        .filterNot { commentRegex in it || it == "" }
        .toList()

    bufferedReader.close()

    // TXYZ must contain at least one atom.
    if (txyzLines.count() < 2) {
        throw RuntimeException(
            "Not a valid TXYZ format."
        )
    }

    val whitespaceRegex = Regex("\\s+")

    if (
        whitespaceRegex.split(txyzLines.first()).first().toInt() !=
        txyzLines.count() - 1
    ) {
        throw RuntimeException(
            "Specified number of atoms does not match the actual number of " +
            "atoms in TXYZ."
        )
    }

    val angstromUnit = UnitOfMeasure.parse("Ao")

    // Tinker atoms and connectivities associated by their atom number.
    val atomsByNumber = mutableMapOf<Int, TinkerAtom>()
    val connectivitiesByNumber = mutableMapOf<Int, Set<Int>>()

    for (atomLine in  txyzLines.drop(1)) {
        val columns = whitespaceRegex.split(atomLine)

        val atomNumber = columns[0].toInt()

        if (atomsByNumber.containsKey(atomNumber)) {
            throw RuntimeException(
                "Atom number already exists: $atomNumber"
            )
        }

        val atomName = columns[1]

        val elementRegex = Regex("^([A-Z][a-z]?)")
        val elementMatchResult = elementRegex.find(atomName)

        if (elementMatchResult == null) {
            throw RuntimeException(
                "Cannot determine the element from atom name '$atomName'."
            )
        }

        val elementSymbol = elementMatchResult.groupValues[1]

        if (!Element.isValidSymbol(elementSymbol)) {
            throw RuntimeException(
                "Cannot determine the element from atom name '$atomName'."
            )
        }

        val element = Element(elementSymbol)

        val position = Vector3D(
            columns.slice(2..4).map {
                Quantity.convertUnit(
                    it.toDouble(),
                    angstromUnit,
                    atomPosUnit
                )
            }.toDoubleArray()
        )

        atomsByNumber[atomNumber] = TinkerAtom(
            element,
            position,
            atomId = atomNumber,
            atomType = columns[5].toInt()
        )

        val connectivity = columns.drop(6).map { it.toInt() }.toSet()

        if (atomNumber in connectivity) {
            throw RuntimeException(
                "Connectivity contains its own atom number: $atomNumber"
            )
        }

        connectivitiesByNumber[atomNumber] = connectivity
    }

    val bonds = connectivitiesByNumber.flatMap {
        (atomNumber, connectivity) ->

        connectivity.map { bondedAtomNumber ->
            Bond(
                atomsByNumber[atomNumber]!!,
                atomsByNumber[bondedAtomNumber]!!,
                ""
            )
        }
    }

    val molecules = BondAggregator.aggregate(bonds).map {
        Molecule(it)
    }

    val atomIslands = connectivitiesByNumber
        .filterValues { it.isEmpty() }
        .keys
        .map {
            @Suppress("UNCHECKED_CAST")
            atomsByNumber[it]!!.island as AtomIsland<TinkerAtom>
        }

    return Supermolecule<TinkerAtom>(molecules + atomIslands)
}
