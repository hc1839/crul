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

@file:JvmName("Mol2")
@file:JvmMultifileClass

package io.github.hc1839.crul.chemistry.species.format.mol2

import java.io.Reader
import java.io.Writer
import kotlin.math.roundToInt
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.chemistry.species.Bond
import io.github.hc1839.crul.chemistry.species.BondAggregator
import io.github.hc1839.crul.chemistry.species.Element
import io.github.hc1839.crul.chemistry.species.Island
import io.github.hc1839.crul.chemistry.species.Molecule
import io.github.hc1839.crul.chemistry.species.Supermolecule
import io.github.hc1839.crul.distinct.Referential
import io.github.hc1839.crul.measure.Quantity
import io.github.hc1839.crul.measure.dimension.Dimension
import io.github.hc1839.crul.measure.unit.UnitOfMeasure
import io.github.hc1839.crul.uuid.UuidGenerator

/**
 *  Parses Mol2 format.
 *
 *  @param M
 *      Type of supermolecules being parsed.
 *
 *  @param reader
 *      Reader from which Mol2 is to be read.
 *
 *  @param supermolBuilder
 *      Builder of the supermolecules.
 *
 *  @return
 *      List of deserialized supermolecules in the same order as from `reader`,
 *      where each Tripos `MOLECULE` corresponds to a supermolecule.
 */
fun <M : Supermolecule<TriposAtom>> Supermolecule.Companion.parseMol2(
    reader: Reader,
    supermolBuilder: Mol2SupermoleculeBuilder<M>
): List<M>
{
    val mol2Decoder = Mol2Decoder(reader, supermolBuilder)
    mol2Decoder.run()
    return supermolBuilder.build()
}

/**
 *  Exports a list of supermolecules in Mol2 format.
 *
 *  Atom identifiers must be unique within each supermolecule.
 *
 *  @param writer
 *      Writer of Mol2 with a trailing newline.
 *
 *  @param triposRecordMapper
 *      Mapper of supported Tripos records.
 */
@JvmOverloads
fun List<Supermolecule<TriposAtom>>.exportMol2(
    writer: Writer,
    triposRecordMapper: TriposRecordMapper = DefaultTriposRecordMapper()
) {
    // Tripos molecule names that have been used. It is for checking
    // uniqueness.
    val supermolNames = mutableListOf<String>()

    // Serialize each supermolecule.
    for (supermol in this) {
        val molName = if (supermol.name == null) {
            var uuid: String

            do {
                uuid = io.github.hc1839.crul.uuid.UuidGenerator.asNCName()
            } while (supermolNames.contains(uuid))

            uuid
        } else {
            supermol.name
        }

        val atoms = supermol.atoms

        val inputMoleculeRecord = TriposMolecule(
            molName = molName,
            numAtoms = atoms.count(),
            numBonds = supermol.subspecies.map { it.bonds.count() }.sum()
        )

        val outputMoleculeRecord = triposRecordMapper.onMolecule(
            supermol,
            inputMoleculeRecord
        )

        val outputMolName = outputMoleculeRecord.molName ?:
            throw RuntimeException(
                "Tripos molecule name is null."
            )

        if (!supermolNames.contains(outputMolName)) {
            supermolNames.add(outputMolName)
        } else {
            throw RuntimeException(
                "Tripos molecule name is not unique: $outputMolName"
            )
        }

        writer.write(TriposRecordType.MOLECULE.rti() + "\n")
        outputMoleculeRecord.exportMol2(writer)

        if (atoms.distinctBy { it.atomId }.count() != atoms.count()) {
            throw RuntimeException(
                "Tripos atom identifiers are not unique for Tripos " +
                "molecule, ${outputMoleculeRecord.molName}"
            )
        }

        writer.write(TriposRecordType.ATOM.rti() + "\n")

        // Serialize each atom.
        for (atom in atoms.sortedBy { it.atomId }) {
            atom.exportMol2(writer)
        }

        val bonds = supermol.subspecies.flatMap { island ->
            island.bonds
        }

        writer.write(TriposRecordType.BOND.rti() + "\n")

        // Serialize each bond.
        for ((bondIndex, bond) in bonds.withIndex()) {
            val bondId = bondIndex + 1
            val (originAtom, targetAtom) = bond.toAtomPair()

            val inputBondRecord = TriposBond(
                bondId = bondId,
                originAtomId = originAtom.atomId,
                targetAtomId = targetAtom.atomId,
                bondType = triposRecordMapper.triposBondTypeOf(
                    supermol,
                    bond,
                    bond.bondType
                )
            )

            triposRecordMapper
                .onBond(supermol, bond, inputBondRecord)
                .exportMol2(writer)
        }

        val additionalRecords = triposRecordMapper.onOther(supermol)

        // Check that there are no supported Tripos record types in the
        // returned additional records.
        val containsSupportedType = additionalRecords.any {
            it.recordType in TriposRecordMapper.supportedTypes
        }

        if (containsSupportedType) {
            throw RuntimeException(
                "At least one additional Tripos record is a Tripos record " +
                "type that has already been mapped."
            )
        }

        if (!additionalRecords.isEmpty()) {
            // For writing a contiguous list of records of the same type.
            var currRecordType = additionalRecords.first().recordType

            writer.write(currRecordType.rti() + "\n")

            // Write the additional Tripos records for this supermolecule.
            for (triposRecord in additionalRecords) {
                if (triposRecord.recordType != currRecordType) {
                    currRecordType = triposRecord.recordType
                    writer.write(currRecordType.rti() + "\n")
                }

                triposRecord.exportMol2(writer)
            }
        }
    }

    writer.flush()
}
