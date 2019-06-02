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
import crul.math.coordsys.Vector3D

/**
 *  Parses Mol2 format.
 *
 *  Element of an atom is determined from the Tripos atom type or Tripos atom
 *  name, where the former takes priority since it is the technically correct
 *  field. However, the Tripos atom name is considered if the Tripos atom type
 *  does not contain the element.
 *
 *  @param reader
 *      Reader from which Mol2 is to be read.
 *
 *  @param bondOrderMapper
 *      Bond order given a Tripos bond type.
 *
 *  @return
 *      List of molecule complexes specified in the same order as in `reader`.
 */
fun MoleculeComplex.Companion.parseMol2(
    reader: Reader,
    bondOrderMapper: (TriposBond.BondType?) -> String
): List<MoleculeComplex<Atom>>
{
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

    // Parse the rest of Mol2.
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

    // Construct atoms from Tripos atom data for each complex. Atoms in each
    // complex are associated by Tripos atom ID for bond construction.
    val atomsByComplexId = atomDataListsByComplexId.mapValues {
        (_, triposAtomDataList) ->

        triposAtomDataList
            .associateBy { it.atomId }
            .mapValues { (_, triposAtomData) ->
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

                val position = Vector3D(
                    triposAtomData.x,
                    triposAtomData.y,
                    triposAtomData.z
                )

                val formalCharge = triposAtomData.charge ?: 0.0

                Atom.newInstance(element, position, formalCharge)
            }
    }

    // Construct bonds from Tripos bond data for each complex.
    val bondsByComplexId = bondDataListsByComplexId.mapValues {
        (complexId, triposBondDataList) ->

        // Atoms by atom ID according to Tripos.
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

    // Construct the molecule complexes from the bonds.
    val complexesById = bondsByComplexId.mapValues {
        (complexId, bonds) ->

        val complexBuilder = MoleculeComplexBuilder.newInstance()

        complexBuilder.setId(complexId)

        // Add the bonds to the builder.
        for (bond in bonds) {
            complexBuilder.addBond(bond)
        }

        val atomsById = atomsByComplexId[complexId]!!
            .values
            .associateBy { it.id }

        // Add atoms that are not participating in a bond to the builder.
        for (
            atomId in
            atomsById.keys - bonds.flatMap { bond ->
                bond.atoms().map { atom -> atom.id }
            }.distinct()
        ) {
            complexBuilder.addAtom(atomsById[atomId]!!)
        }

        complexBuilder.build<Atom>()
    }

    return complexIds.map { complexesById[it]!! }
}

/**
 *  Exports a list of molecule complexes in Mol2 format.
 *
 *  Since determining the Tripos atom type is not supported, all atoms will
 *  have `Any` as the value of the Tripos atom type field. The element of an
 *  atom, then, is exported to the Tripos atom name field.
 *
 *  @param triposBondTypeMapper
 *      Tripos bond type given a [Bond.order].
 *
 *  @return
 *      Molecule complexes in Mol2 format.
 */
fun List<MoleculeComplex<Atom>>.exportMol2(
    triposBondTypeMapper: (String) -> TriposBond.BondType
): Reader
{
    // Builder of Mol2 lines.
    var mol2Builder = listOf<String>()

    for (complex in this) {
        mol2Builder += TriposRecordType.MOLECULE.rti()

        // All atoms.
        val atoms = complex.atoms().toList()

        mol2Builder += TriposMolecule(
            molName = complex.id,
            numAtoms = atoms.count()
        ).exportMol2()

        // Tripos atom IDs associated by internal atom ID.
        val atomIntsById = atoms.mapIndexed { index, atom ->
            Pair(atom.id, index + 1)
        }.toMap()

        val triposAtomDataList = atoms
            .map { atom ->
                TriposAtom(
                    atomId = atomIntsById[atom.id]!!,
                    atomName = atom.element.symbol,
                    x = atom.centroid.components[0],
                    y = atom.centroid.components[1],
                    z = atom.centroid.components[2],
                    atomType = "Any"
                )
            }
            .sortedBy { it.atomId }

        mol2Builder += TriposRecordType.ATOM.rti()

        for (triposAtomData in triposAtomDataList) {
            mol2Builder += triposAtomData.exportMol2()
        }

        val bonds = complex.mapNotNull {
            @Suppress("UNCHECKED_CAST")
            (it as? Molecule<*>)?.bonds()
        }.flatten()

        val triposBondDataList = bonds.mapIndexed { index, bond ->
            val bondId = index + 1
            val (originAtom, targetAtom) = bond.toAtomPair()

            TriposBond(
                bondId = bondId,
                originAtomId = atomIntsById[originAtom.id]!!,
                targetAtomId = atomIntsById[targetAtom.id]!!,
                bondType = triposBondTypeMapper.invoke(bond.order)
            )
        }

        mol2Builder += TriposRecordType.BOND.rti()

        for (triposBondData in triposBondDataList) {
            mol2Builder += triposBondData.exportMol2()
        }
    }

    return StringReader(mol2Builder.joinToString("\n"))
}
