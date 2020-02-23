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

package crul.chemistry.species.format.mol2

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
 *  Default implementation of [ComplexBuilder].
 *
 *  A derived class may override specific member functions for
 *  implementation-specific behavior.
 *
 *  @property atomPosUnit
 *      Unit of the coordinates that the positions of the deserialized atoms
 *      are in. It must be a unit of `L`.
 *
 *  @constructor
 */
open class DefaultComplexBuilder(
    private val atomPosUnit: UnitOfMeasure = UnitOfMeasure.parse("Ao")
): ComplexBuilder
{
    init {
        if (!atomPosUnit.isUnitOf(Dimension.parse("L"))) {
            throw IllegalArgumentException(
                "Unit of atom position is not a unit of length."
            )
        }
    }

    /**
     *  Names of complexes that the decoder has encountered in order.
     */
    protected var complexNames: MutableList<String> =
        mutableListOf()

    /**
     *  Mutable list of [TriposAtom] associated by complex name.
     */
    protected val atomRecordsByComplexName:
        MutableMap<String, MutableList<TriposAtom>> = mutableMapOf()

    /**
     *  Mutable list of [TriposBond] associated by complex name.
     */
    protected val bondRecordsByComplexName:
        MutableMap<String, MutableList<TriposBond>> = mutableMapOf()

    /**
     *  Marks the building of a new complex.
     *
     *  If another complex of the same name exists, an exception is thrown.
     */
    override fun onMolecule(record: TriposMolecule) {
        if (record.molName in complexNames) {
            throw RuntimeException(
                "Molecule name exists: ${record.molName}"
            )
        }

        complexNames.add(record.molName!!)
        atomRecordsByComplexName[complexNames.last()] = mutableListOf()
        bondRecordsByComplexName[complexNames.last()] = mutableListOf()
    }

    override fun onAtom(record: TriposAtom) {
        atomRecordsByComplexName[complexNames.last()]!!.add(record)
    }

    override fun onBond(record: TriposBond) {
        bondRecordsByComplexName[complexNames.last()]!!.add(record)
    }

    /**
     *  Element of an atom is determined from the leading one or two
     *  alphabetical characters of the Tripos atom type or Tripos atom name,
     *  where Tripos atom type takes priority since it is the technically
     *  correct field. The Tripos atom name is considered if the Tripos atom
     *  type does not contain the element.
     *
     *  If a following key's corresponding property in [TriposAtom] is not
     *  `null`, the key in [Atom.userData] is set to the property's value with
     *  the same type. Otherwise, the key is not set.
     *      - `atomId`
     *      - `atomName`
     *      - `atomType`
     *      - `substId`
     *      - `substName`
     *      - `statusBit` (as `Enum.name`)
     *
     *  Bond order is set to [TriposBond.BondType.name]. The key, `statusBits`,
     *  is not set.
     */
    override fun build(): List<MoleculeComplex<Atom>> {
        val complexes = mutableListOf<MoleculeComplex<Atom>>()

        // Build each complex.
        for (complexName in complexNames) {
            val triposAtoms = atomRecordsByComplexName[complexName]!!
            val atomsByTriposId = mutableMapOf<Int, Atom>()

            // Build each atom.
            for (triposAtom in triposAtoms) {
                // Input strings that possibly contain an element symbol.
                // Atom-type field is tested first.
                val elementInputStrings = listOf(
                    triposAtom.atomType,
                    triposAtom.atomName
                ).filterNotNull()

                val matchResults = elementInputStrings
                    .map { elementSymbolRegex.find(it) }
                    .filterNotNull()

                if (matchResults.isEmpty()) {
                    throw RuntimeException(
                        "Tripos atom-type and atom-name fields do not " +
                        "contain leading alphabetical characters " +
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
                    listOf(triposAtom.x, triposAtom.y, triposAtom.z).map {
                        Quantity.convertUnit(it, ANGSTROM_UNIT, atomPosUnit)
                    }.toDoubleArray()
                )

                val charge = triposAtom.charge

                val atom = Atom(element, position, charge)

                atom.userData["atomId"] = triposAtom.atomId

                if (triposAtom.atomName != null) {
                    atom.userData["atomName"] = triposAtom.atomName
                }

                if (triposAtom.atomType != null) {
                    atom.userData["atomType"] = triposAtom.atomType
                }

                if (triposAtom.substId != null) {
                    atom.userData["substId"] = triposAtom.substId
                }

                if (triposAtom.substName != null) {
                    atom.userData["substName"] = triposAtom.substName
                }

                if (triposAtom.statusBit != null) {
                    atom.userData["statusBit"] = triposAtom.statusBit.name
                }

                atomsByTriposId[triposAtom.atomId] = atom
            }

            val triposBonds = bondRecordsByComplexName[complexName]!!
            val bondsByTriposId = mutableMapOf<Int, Bond<Atom>>()

            // Build each bond.
            for (triposBond in triposBonds) {
                bondsByTriposId[triposBond.bondId] = Bond(
                    atomsByTriposId[triposBond.originAtomId]!!,
                    atomsByTriposId[triposBond.targetAtomId]!!,
                    triposBond.bondType!!.name
                )
            }

            // Set of wrapped atoms that are participating in a bond.
            val wrappedBondedAtoms = bondsByTriposId
                .values
                .flatMap { bond -> bond.atoms() }
                .map { atom -> Referential(atom) }
                .toSet()

            // Set of atoms that are not participating in a bond.
            val unbondedAtoms = atomsByTriposId.values.filter { atom ->
                Referential(atom) !in wrappedBondedAtoms
            }.toSet()

            val atomIslands = mutableListOf<Island<Atom>>()

            // Add atoms that are not participating in a bond.
            for (unbondedAtom in unbondedAtoms) {
                atomIslands.add(unbondedAtom.getIsland<Atom>())
            }

            val molecules = BondAggregator
                .aggregate(bondsByTriposId.values)
                .map { Molecule(it) }

            complexes.add(
                MoleculeComplex(atomIslands + molecules)
            )
        }

        return complexes.toList()
    }

    companion object {
        /**
         *  Regular expression for matching the symbol of an element in
         *  the Tripos atom-type or atom-name field.
         */
        private val elementSymbolRegex =
            Regex("^([A-Z][a-z]?)")

        /**
         *  Angstrom unit.
         */
        private val ANGSTROM_UNIT: UnitOfMeasure =
            UnitOfMeasure.parse("Ao")
    }
}
