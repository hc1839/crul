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

import crul.chemistry.species.Atom
import crul.chemistry.species.Bond
import crul.chemistry.species.BondAggregator
import crul.chemistry.species.Element
import crul.chemistry.species.Island
import crul.chemistry.species.Molecule
import crul.chemistry.species.MoleculeComplex
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
 *  @param complexBuilder
 *      Builder of the complexes.
 *
 *  @return
 *      List of deserialized molecule complexes in the same order as from
 *      `reader`.
 */
fun MoleculeComplex.Companion.parseMol2(
    reader: Reader,
    complexBuilder: ComplexBuilder = DefaultComplexBuilder()
): List<MoleculeComplex<Atom>>
{
    val mol2Decoder = Mol2Decoder(reader, complexBuilder)
    mol2Decoder.run()
    return complexBuilder.build()
}

/**
 *  Exports a list of molecule complexes in Mol2 format.
 *
 *  If any of the keys (except `atomId`) specified in
 *  [DefaultComplexBuilder.build] for atoms exists in [Atom.userData], their
 *  values are used as the values of the corresponding fields for the Tripos
 *  record, `ATOM`. The key, `atomId`, is ignored.
 *
 *  @param writer
 *      Writer of Mol2 with a trailing newline.
 *
 *  @param atomPosUnit
 *      Unit of the coordinates that the atom positions are in. It must be a
 *      unit of `L`.
 *
 *  @param atomIdMapper
 *      Tripos atom identifier given an atom and the complex that it is in. If
 *      `null`, atoms are arbitrarily assigned a Tripos atom identifier.
 *
 *  @param complexNameMapper
 *      Tripos molecule name given a complex. If `null`, UUID Version 4 is
 *      used.
 *
 *  @param numSubstMapper
 *      Number of substructures given a complex. If `null`, nothing is written
 *      for the Tripos field, 'num_subst`, in the Tripos record, `MOLECULE`.
 *
 *  @param triposBondTypeMapper
 *      Tripos bond type given a [Bond.order].
 */
@JvmOverloads
fun <A : Atom> List<MoleculeComplex<A>>.exportMol2(
    writer: Writer,
    atomPosUnit: UnitOfMeasure,
    atomIdMapper: ((A, MoleculeComplex<A>) -> Int)? = null,
    complexNameMapper: ((MoleculeComplex<A>) -> String)? = null,
    numSubstMapper: ((MoleculeComplex<A>) -> Int)? = null,
    triposBondTypeMapper: (String) -> TriposBond.BondType
) {
    if (!atomPosUnit.isUnitOf(Dimension.parse("L"))) {
        throw IllegalArgumentException(
            "Unit of atom position is not a unit of length."
        )
    }

    // Tripos molecule names.
    val complexNames = mutableListOf<String>()

    // Add or create Tripos molecule names.
    for (complex in this) {
        val complexName = complexNameMapper?.invoke(complex)

        if (complexName != null) {
            if (complexNames.contains(complexName)) {
                throw RuntimeException(
                    "Tripos molecule name is not unique: $complexName"
                )
            }

            complexNames.add(complexName)
        } else {
            var uuid: String

            do {
                uuid = crul.uuid.UuidGenerator.asNCName()
            } while (complexNames.contains(uuid))

            complexNames.add(uuid)
        }
    }

    val angstromUnit = UnitOfMeasure.parse("Ao")

    // Serialize each complex.
    for ((complexIndex, complex) in withIndex()) {
        writer.write(TriposRecordType.MOLECULE.rti())
        writer.write("\n")

        // Atoms in this complex.
        val atoms = complex.atoms().toList()

        writer.write(
            TriposMolecule(
                molName = complexNames[complexIndex],
                numAtoms = atoms.count(),
                numBonds = complex.subspecies.map {
                    if (it is Molecule<*>) {
                        it.bonds().count()
                    } else {
                        0
                    }
                }.sum(),
                numSubst = numSubstMapper?.invoke(complex)
            ).exportMol2()
        )
        writer.write("\n")

        // Tripos atom IDs associated by wrapped atom.
        val atomIdsByAtom = mutableMapOf<Referential<A>, Int>()

        // Add or create Tripos atom IDs.
        for ((atomIndex, atom) in atoms.withIndex()) {
            val atomId = atomIdMapper?.invoke(atom, complex) ?: atomIndex + 1

            if (atomIdsByAtom.containsValue(atomId)) {
                throw RuntimeException(
                    "Tripos atom identifier is not unique: $atomId"
                )
            }

            atomIdsByAtom[Referential(atom)] = atomId
        }

        // Construct Tripos atom data.
        val triposAtomDataList = atoms
            .map { atom ->
                // Components of the atom position in Angstroms.
                val atomPosCmptsAo = atom.position.toArray().map {
                    Quantity.convertUnit(it, atomPosUnit, angstromUnit)
                }

                val atomName = atom
                    .userData
                    .getOrDefault("atomName", atom.element.symbol)
                    .toString()

                val atomType = atom
                    .userData
                    .getOrDefault("atomType", atom.element.symbol)
                    .toString()

                val substId = atom
                    .userData
                    .getOrDefault("substId", null) as Int?

                val substName = atom
                    .userData
                    .getOrDefault("substName", null) as String?

                val statusBit = atom
                    .userData
                    .getOrDefault("statusBit", null) as TriposAtom.StatusBit?

                TriposAtom(
                    atomId = atomIdsByAtom[Referential(atom)]!!,
                    atomName = atomName,
                    x = atomPosCmptsAo[0],
                    y = atomPosCmptsAo[1],
                    z = atomPosCmptsAo[2],
                    atomType = atomType,
                    substId = substId,
                    substName = substName,
                    charge = atom.charge,
                    statusBit = statusBit
                )
            }
            .sortedBy { it.atomId }

        writer.write(TriposRecordType.ATOM.rti())
        writer.write("\n")

        // Serialize each atom.
        for (triposAtomData in triposAtomDataList) {
            writer.write(triposAtomData.exportMol2())
            writer.write("\n")
        }

        val bonds = complex.subspecies.flatMap { island ->
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
