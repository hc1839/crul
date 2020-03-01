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

package crul.chemistry.species.format.mol2

import java.io.Reader
import java.io.Writer
import kotlin.math.roundToInt
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import crul.chemistry.species.Bond
import crul.chemistry.species.BondAggregator
import crul.chemistry.species.Element
import crul.chemistry.species.Island
import crul.chemistry.species.Molecule
import crul.chemistry.species.Supermolecule
import crul.distinct.Referential
import crul.measure.Quantity
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure
import crul.uuid.UuidGenerator

/**
 *  Parses Mol2 format.
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
fun Supermolecule.Companion.parseMol2(
    reader: Reader,
    supermolBuilder: SupermoleculeBuilder = DefaultSupermoleculeBuilder()
): List<Supermolecule<TriposAtom>>
{
    val mol2Decoder = Mol2Decoder(reader, supermolBuilder)
    mol2Decoder.run()
    return supermolBuilder.build()
}

/**
 *  Exports a list of supermolecules in Mol2 format.
 *
 *  @param writer
 *      Writer of Mol2 with a trailing newline.
 *
 *  @param supermolNameMapper
 *      Tripos molecule name given a supermolecule. If `null`, UUID Version 4
 *      is used.
 *
 *  @param numSubstMapper
 *      Number of substructures given a supermolecule. If `null`, nothing is
 *      written for the Tripos field, 'num_subst`, in the Tripos record,
 *      `MOLECULE`.
 *
 *  @param triposBondTypeMapper
 *      Tripos bond type given a [Bond.order].
 */
@JvmOverloads
fun List<Supermolecule<TriposAtom>>.exportMol2(
    writer: Writer,
    supermolNameMapper: ((Supermolecule<TriposAtom>) -> String)? = null,
    numSubstMapper: ((Supermolecule<TriposAtom>) -> Int)? = null,
    triposBondTypeMapper: (String) -> TriposBond.BondType
) {
    // Tripos molecule names.
    val supermolNames = mutableListOf<String>()

    // Add or create Tripos molecule names.
    for (supermol in this) {
        val supermolName = supermolNameMapper?.invoke(supermol)

        if (supermolName != null) {
            if (supermolNames.contains(supermolName)) {
                throw RuntimeException(
                    "Tripos molecule name is not unique: $supermolName"
                )
            }

            supermolNames.add(supermolName)
        } else {
            var uuid: String

            do {
                uuid = crul.uuid.UuidGenerator.asNCName()
            } while (supermolNames.contains(uuid))

            supermolNames.add(uuid)
        }
    }

    // Serialize each supermolecule.
    for ((supermolIndex, supermol) in withIndex()) {
        writer.write(TriposRecordType.MOLECULE.rti())
        writer.write("\n")

        val atoms = supermol.atoms

        writer.write(
            TriposMolecule(
                molName = supermolNames[supermolIndex],
                numAtoms = atoms.count(),
                numBonds = supermol.subspecies.map {
                    if (it is Molecule<*>) {
                        it.bonds().count()
                    } else {
                        0
                    }
                }.sum(),
                numSubst = numSubstMapper?.invoke(supermol)
            ).exportMol2()
        )
        writer.write("\n")

        // Tripos atom IDs associated by wrapped atom.
        val atomIdsByAtom = mutableMapOf<Referential<TriposAtom>, Int>()

        // Add or create Tripos atom IDs.
        for (atom in atoms) {
            if (atomIdsByAtom.containsValue(atom.atomId)) {
                throw RuntimeException(
                    "Tripos atom identifier is not unique: ${atom.atomId}"
                )
            }

            atomIdsByAtom[Referential(atom)] = atom.atomId
        }

        // Construct Tripos atom data.
        val triposAtomDataList = atoms.sortedBy { it.atomId }

        writer.write(TriposRecordType.ATOM.rti())
        writer.write("\n")

        // Serialize each atom.
        for (triposAtomData in triposAtomDataList) {
            writer.write(triposAtomData.exportMol2())
            writer.write("\n")
        }

        val bonds = supermol.subspecies.flatMap { island ->
            island.bonds()
        }

        // Construct Tripos bond data.
        val triposBondDataList = bonds.mapIndexed { index, bond ->
            val bondId = index + 1
            val (originAtom, targetAtom) = bond.toAtomPair()

            TriposBond(
                bondId = bondId,
                originAtomId = atomIdsByAtom[Referential(originAtom)]!!,
                targetAtomId = atomIdsByAtom[Referential(targetAtom)]!!,
                bondType = triposBondTypeMapper.invoke(bond.order)
            )
        }

        writer.write(TriposRecordType.BOND.rti())
        writer.write("\n")

        // Serialize each bond.
        for (triposBondData in triposBondDataList) {
            writer.write(triposBondData.exportMol2())
            writer.write("\n")
        }
    }

    writer.flush()
}
