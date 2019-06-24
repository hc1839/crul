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
import java.io.StringReader

import crul.chemistry.species.Atom
import crul.chemistry.species.Bond
import crul.chemistry.species.Element
import crul.chemistry.species.Molecule
import crul.chemistry.species.MoleculeComplex
import crul.chemistry.species.MoleculeComplexBuilder
import crul.chemistry.species.SpeciesSetElement
import crul.math.coordsys.Vector3D
import crul.measure.Quantity
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure

/**
 *  Exports a list of molecule complexes in Mol2 format.
 *
 *  Since determining the Tripos atom type is not supported, all atoms will
 *  have `Any` as the value of the Tripos atom type field. The element of an
 *  atom, then, is exported to the Tripos atom name field.
 *
 *  @param atomPosUnit
 *      Unit of the coordinates that the atom positions are in. It must be a
 *      unit of `L`.
 *
 *  @param atomIdMapper
 *      Tripos atom identifier given an atom and the complex that it is in. If
 *      `null`, atoms are arbitrarily assigned a Tripos atom identifier.
 *
 *  @param molNameMapper
 *      Tripos molecule name given a complex. If `null`, UUID Version 4 is
 *      used.
 *
 *  @param triposBondTypeMapper
 *      Tripos bond type given a [Bond.order].
 *
 *  @return
 *      Reader of molecule complexes in Mol2 format with a trailing newline.
 */
@JvmOverloads
fun <A : Atom> List<MoleculeComplex<A>>.exportMol2(
    atomPosUnit: UnitOfMeasure,
    atomIdMapper: ((A, MoleculeComplex<A>) -> Int)? = null,
    molNameMapper: ((MoleculeComplex<A>) -> String)? = null,
    triposBondTypeMapper: (String) -> TriposBond.BondType
): Reader
{
    if (!atomPosUnit.isUnitOf(Dimension.parse("L"))) {
        throw IllegalArgumentException(
            "Unit of atom position is not a unit of length."
        )
    }

    // Builder of Mol2 lines.
    var mol2Builder = listOf<String>()

    // Tripos molecule names.
    var molNames: List<String> = listOf()

    // Add or create Tripos molecule names.
    for (complex in this) {
        val molName = molNameMapper?.invoke(complex)

        if (molName != null) {
            if (molNames.contains(molName)) {
                throw RuntimeException(
                    "Tripos molecule name is not unique: $molName"
                )
            }

            molNames = molNames.plusElement(molName)
        } else {
            var uuid: String

            do {
                uuid = crul.uuid.Generator.inNCName()
            } while (molNames.contains(uuid))

            molNames = molNames.plusElement(uuid)
        }
    }

    val angstromUnit = UnitOfMeasure.parse("Ao")

    // Serialize each complex.
    for ((complexIndex, complex) in withIndex()) {
        mol2Builder += TriposRecordType.MOLECULE.rti()

        // Atoms in this complex.
        val atoms = complex.atoms().toList()

        mol2Builder += TriposMolecule(
            molName = molNames[complexIndex],
            numAtoms = atoms.count(),
            numBonds = complex
                .map {
                    if (it is Molecule<*>) {
                        it.bonds().count()
                    } else {
                        0
                    }
                }
                .sum()
        ).exportMol2()

        // Tripos atom IDs associated by wrapped atom.
        val atomIdsByAtom: MutableMap<SpeciesSetElement<A>, Int> =
            mutableMapOf()

        // Add or create Tripos atom IDs.
        for ((atomIndex, atom) in atoms.withIndex()) {
            val atomId = atomIdMapper?.invoke(atom, complex) ?: atomIndex + 1

            if (atomIdsByAtom.containsValue(atomId)) {
                throw RuntimeException(
                    "Tripos atom identifier is not unique: $atomId"
                )
            }

            atomIdsByAtom[SpeciesSetElement(atom)] = atomId
        }

        // Construct Tripos-atom data.
        val triposAtomDataList = atoms
            .map { atom ->
                // Components of the atom position in Angstroms.
                val atomPosCmptsAo = atom.centroid.components.map {
                    Quantity.convertUnit(it, atomPosUnit, angstromUnit)
                }

                TriposAtom(
                    atomId = atomIdsByAtom[SpeciesSetElement(atom)]!!,
                    atomName = atom.element.symbol,
                    x = atomPosCmptsAo[0],
                    y = atomPosCmptsAo[1],
                    z = atomPosCmptsAo[2],
                    atomType = "Any"
                )
            }
            .sortedBy { it.atomId }

        mol2Builder += TriposRecordType.ATOM.rti()

        // Serialize each atom.
        for (triposAtomData in triposAtomDataList) {
            mol2Builder += triposAtomData.exportMol2()
        }

        val bonds = complex.mapNotNull {
            @Suppress("UNCHECKED_CAST")
            (it as? Molecule<A>)?.bonds()
        }.flatten()

        // Construct Tripos-bond data.
        val triposBondDataList = bonds.mapIndexed { index, bond ->
            val bondId = index + 1
            val (originAtom, targetAtom) = bond.toAtomPair()

            TriposBond(
                bondId = bondId,
                originAtomId = atomIdsByAtom[SpeciesSetElement(originAtom)]!!,
                targetAtomId = atomIdsByAtom[SpeciesSetElement(targetAtom)]!!,
                bondType = triposBondTypeMapper.invoke(bond.order)
            )
        }

        mol2Builder += TriposRecordType.BOND.rti()

        // Serialize each bond.
        for (triposBondData in triposBondDataList) {
            mol2Builder += triposBondData.exportMol2()
        }
    }

    return StringReader(mol2Builder.joinToString("\n") + "\n")
}

/**
 *  Parses Mol2 format.
 *
 *  Element of an atom is determined from the Tripos atom type or Tripos atom
 *  name, where the former takes priority since it is the technically correct
 *  field. However, the Tripos atom name is considered if the Tripos atom type
 *  does not contain the element.
 *
 *  Atom tags are populated with the atom identifiers in Mol2.
 *
 *  @param reader
 *      Reader from which Mol2 is to be read.
 *
 *  @param atomPosUnit
 *      Unit of the coordinates that the positions of the deserialized atoms
 *      are in. It must be a unit of `L`.
 *
 *  @param bondOrderMapper
 *      Bond order given a Tripos bond type.
 *
 *  @return
 *      List of deserialized molecule complexes in the same order as from
 *      `reader`.
 */
fun MoleculeComplex.Companion.parseMol2(
    reader: Reader,
    atomPosUnit: UnitOfMeasure,
    bondOrderMapper: (TriposBond.BondType?) -> String
): List<MoleculeComplex<Atom>>
{
    if (!atomPosUnit.isUnitOf(Dimension.parse("L"))) {
        throw IllegalArgumentException(
            "Unit of atom position is not a unit of length."
        )
    }

    val parser = Mol2Parser.newInstance(reader)

    parser.next()

    var currSectionType = parser.getRecordType()

    // Complex IDs in the same order as in the reader.
    val complexIds = mutableListOf<String>()

    // Tripos-atom data.
    val atomDataListsByComplexId =
        mutableMapOf<String, MutableList<TriposAtom>>()

    // Tripos-bond data.
    val bondDataListsByComplexId =
        mutableMapOf<String, MutableList<TriposBond>>()

    // Parse and add the rest of Mol2 as Tripos data.
    for (parserEvent in parser) {
        when (parserEvent) {
            Mol2Parser.Event.RECORD_TYPE_INDICATOR ->
                currSectionType = parser.getRecordType()

            Mol2Parser.Event.DATA_LINES_OF_RECORD -> {
                val dataLines = parser.getDataLinesOfRecord()

                when (currSectionType) {
                    TriposRecordType.MOLECULE -> {
                        val triposMoleculeData = TriposMolecule.parseMol2(
                            dataLines.joinToString("\n")
                        )

                        val currComplexId = triposMoleculeData.molName ?:
                                crul.uuid.Generator.inNCName()

                        complexIds.add(currComplexId)

                        atomDataListsByComplexId[currComplexId] =
                            mutableListOf()

                        bondDataListsByComplexId[currComplexId] =
                            mutableListOf()
                    }

                    TriposRecordType.ATOM -> {
                        val triposAtomData = TriposAtom.parseMol2(
                            dataLines.joinToString("\n")
                        )

                        atomDataListsByComplexId[complexIds.last()]!!.add(
                            triposAtomData
                        )
                    }

                    TriposRecordType.BOND -> {
                        val triposBondData = TriposBond.parseMol2(
                            dataLines.joinToString("\n")
                        )

                        bondDataListsByComplexId[complexIds.last()]!!.add(
                            triposBondData
                        )
                    }

                    else -> {  }
                }
            }
        }
    }

    val angstromUnit = UnitOfMeasure.parse("Ao")

    // Construct atoms from Tripos atom data for each complex. Atoms in each
    // complex are associated by Tripos atom ID for bond construction.
    val atomsByComplexId = atomDataListsByComplexId.mapValues {
        (_, triposAtomDataList) ->

        triposAtomDataList
            .associateBy { it.atomId }
            .mapValues { (_, triposAtomData) ->
                // Strings to test for symbol of an element.
                val hypotheticalSymbols = listOf(
                    triposAtomData
                        .atomType
                        ?.split(".")
                        ?.firstOrNull(),
                    triposAtomData.atomName
                ).filterNotNull()

                val element = hypotheticalSymbols.mapNotNull {
                    try {
                        Element(it)
                    } catch (e: Throwable) {
                        null
                    }
                }.firstOrNull()

                if (element == null) {
                    throw RuntimeException(
                        "Not at least one of the following " +
                        "is a recognized element: " +
                        hypotheticalSymbols.joinToString(", ")
                    )
                }

                val centroid = Vector3D(
                    listOf(
                        triposAtomData.x,
                        triposAtomData.y,
                        triposAtomData.z
                    ).map {
                        Quantity.convertUnit(it, angstromUnit, atomPosUnit)
                    }
                )

                val formalCharge = triposAtomData.charge ?: 0.0

                Atom.newInstance(
                    element,
                    centroid,
                    formalCharge,
                    triposAtomData.atomId
                )
            }
    }

    // Construct bonds from Tripos bond data for each complex.
    val bondsByComplexId = bondDataListsByComplexId.mapValues {
        (complexId, triposBondDataList) ->

        // Atoms associated by atom ID according to Tripos.
        val atomsByTriposId = atomsByComplexId[complexId]!!

        // Construct the bonds for this complex.
        triposBondDataList.map { triposBondData ->
            val originAtom = atomsByTriposId[triposBondData.originAtomId]!!
            val targetAtom = atomsByTriposId[triposBondData.targetAtomId]!!

            Bond.newInstance(
                originAtom,
                targetAtom,
                bondOrderMapper(triposBondData.bondType)
            )
        }
    }

    // Construct the complexes from the bonds.
    val complexesById = bondsByComplexId.mapValues {
        (complexId, bonds) ->

        // Atoms in this complex.
        val atoms = atomsByComplexId[complexId]!!.values

        val complexBuilder = MoleculeComplexBuilder.newInstance()

        // Add the bonds to the builder.
        for (bond in bonds) {
            complexBuilder.addBond(bond)
        }

        // Set of wrapped atoms that are participating in a bond.
        val wrappedBondedAtoms = bonds
            .flatMap { bond -> bond.atoms() }
            .map { atom -> SpeciesSetElement(atom) }
            .toSet()

        // Add atoms that are not participating in a bond to the builder.
        for (
            atom in
            atoms.filter { atom ->
                SpeciesSetElement(atom) !in wrappedBondedAtoms
            }
        ) {
            complexBuilder.addAtom(atom)
        }

        complexBuilder.build<Atom>()
    }

    // Return the complexes in the same order as in Mol2.
    return complexIds.map { complexesById[it]!! }
}
