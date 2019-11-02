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

/**
 *  Exports a list of molecule complexes in Mol2 format.
 *
 *  Since determining the Tripos atom type is not supported, the values of the
 *  Tripos atom-type and atom-name fields are the atom's element.
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
 *  @param triposBondTypeMapper
 *      Tripos bond type given a [Bond.order].
 */
@JvmOverloads
fun <A : Atom> List<MoleculeComplex<A>>.exportMol2(
    writer: Writer,
    atomPosUnit: UnitOfMeasure,
    atomIdMapper: ((A, MoleculeComplex<A>) -> Int)? = null,
    complexNameMapper: ((MoleculeComplex<A>) -> String)? = null,
    triposBondTypeMapper: (String) -> TriposBond.BondType
)
{
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
                uuid = crul.uuid.Generator.inNCName()
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
                }.sum()
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

                TriposAtom(
                    atomId = atomIdsByAtom[Referential(atom)]!!,
                    atomName = atom.element.symbol,
                    x = atomPosCmptsAo[0],
                    y = atomPosCmptsAo[1],
                    z = atomPosCmptsAo[2],
                    atomType = atom.element.symbol,
                    charge = atom.charge
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

/**
 *  Parses Mol2 format.
 *
 *  Element of an atom is determined from the leading one or two alphabetical
 *  characters of the Tripos atom type or Tripos atom name, where Tripos atom
 *  type takes priority since it is the technically correct field. The Tripos
 *  atom name is considered if the Tripos atom type does not contain the
 *  element.
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

    // Tripos atom data.
    val atomDataListsByComplexId =
        mutableMapOf<String, MutableList<TriposAtom>>()

    // Tripos bond data.
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
                // Regex to find the symbol of an element.
                val elementSymbolRegex = Regex("^([A-Z][a-z]?)")

                // Input strings possibly containing an element symbol.
                val elementInputStrings = listOf(
                    triposAtomData.atomType,
                    triposAtomData.atomName
                ).filterNotNull()

                val matchResults = elementInputStrings
                    .map { elementSymbolRegex.find(it) }
                    .filterNotNull()

                if (matchResults.isEmpty()) {
                    throw RuntimeException(
                        "Tripos atom type and atom name do not contain " +
                        "leading alphabetical characters " +
                        "of a possible element symbol."
                    )
                }

                val possibleElementSymbols = matchResults.map {
                    it.groupValues[1]
                }

                val elementSymbol = possibleElementSymbols.find {
                    Element.isValidSymbol(it)
                }

                if (elementSymbol == null) {
                    throw RuntimeException(
                        "No valid element symbol found " +
                        "in Tripos atom type or atom name."
                    )
                }

                val element = Element(elementSymbol)

                val position = Vector3D(
                    listOf(
                        triposAtomData.x,
                        triposAtomData.y,
                        triposAtomData.z
                    ).map {
                        Quantity.convertUnit(it, angstromUnit, atomPosUnit)
                    }.toDoubleArray()
                )

                val charge = triposAtomData.charge

                Atom(
                    element,
                    position,
                    charge,
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

            Bond(
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

        // Set of wrapped atoms that are participating in a bond.
        val wrappedBondedAtoms = bonds
            .flatMap { bond -> bond.atoms() }
            .map { atom -> Referential(atom) }
            .toSet()

        val atomIslands = mutableListOf<Island<Atom>>()

        // Add atoms that are not participating in a bond to the builder.
        for (
            unbondedAtom in
            atoms.filter { atom ->
                Referential(atom) !in wrappedBondedAtoms
            }
        ) {
            atomIslands.add(unbondedAtom.getIsland<Atom>())
        }

        val molecules = BondAggregator.aggregate(bonds).map { bondGroup ->
            Molecule(bondGroup)
        }

        MoleculeComplex(molecules + atomIslands)
    }

    // Return the complexes in the same order as in Mol2.
    return complexIds.map { complexesById[it]!! }
}
