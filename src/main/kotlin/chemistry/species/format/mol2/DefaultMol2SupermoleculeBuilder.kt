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

import crul.chemistry.species.AtomIsland
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

/**
 *  Default implementation of [Mol2SupermoleculeBuilder].
 *
 *  Each Tripos `MOLECULE` record corresponds to a [Supermolecule].
 *
 *  A derived class may override specific member functions for
 *  implementation-specific behavior.
 *
 *  @constructor
 */
open class DefaultMol2SupermoleculeBuilder() :
    Mol2SupermoleculeBuilder<Supermolecule<TriposAtom>>
{
    /**
     *  Names of supermolecules that the decoder has encountered in order.
     */
    protected var supermolNames: MutableList<String> =
        mutableListOf()

    /**
     *  Mutable list of [TriposAtom] associated by supermolecule name.
     */
    protected val atomRecordsBySupermolName:
        MutableMap<String, MutableList<TriposAtom>> = mutableMapOf()

    /**
     *  Mutable list of [TriposBond] associated by supermolecule name.
     */
    protected val bondRecordsBySupermolName:
        MutableMap<String, MutableList<TriposBond>> = mutableMapOf()

    /**
     *  Marks the building of a new supermolecule.
     *
     *  If another supermolecule of the same name exists, an exception is
     *  thrown.
     */
    override fun onMolecule(record: TriposMolecule) {
        if (record.molName in supermolNames) {
            throw RuntimeException(
                "Molecule name exists: ${record.molName}"
            )
        }

        supermolNames.add(record.molName!!)
        atomRecordsBySupermolName[supermolNames.last()] = mutableListOf()
        bondRecordsBySupermolName[supermolNames.last()] = mutableListOf()
    }

    override fun onAtom(record: TriposAtom) {
        atomRecordsBySupermolName[supermolNames.last()]!!.add(record)
    }

    override fun onBond(record: TriposBond) {
        bondRecordsBySupermolName[supermolNames.last()]!!.add(record)
    }

    /**
     *  See [TriposAtom] for how the element of an atom is determined.
     *
     *  Bond order is set to [TriposBond.BondType.name].
     */
    override fun build(): List<Supermolecule<TriposAtom>> {
        val supermols = mutableListOf<Supermolecule<TriposAtom>>()

        // Build each supermolecule.
        for (supermolName in supermolNames) {
            val triposAtoms = atomRecordsBySupermolName[supermolName]!!
            val atomsByTriposId = mutableMapOf<Int, TriposAtom>()

            // Build each atom.
            for (triposAtom in triposAtoms) {
                atomsByTriposId[triposAtom.atomId] = triposAtom
            }

            val triposBonds = bondRecordsBySupermolName[supermolName]!!
            val bondsByTriposId = mutableMapOf<Int, Bond<TriposAtom>>()

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
                .flatMap { bond -> bond.atoms }
                .map { atom -> Referential(atom) }
                .toSet()

            // Set of atoms that are not participating in a bond.
            val unbondedAtoms = atomsByTriposId
                .values
                .filter { atom -> Referential(atom) !in wrappedBondedAtoms }
                .toSet()

            // Add atoms that are not participating in a bond.
            val atomIslands = unbondedAtoms.map {
                @Suppress("UNCHECKED_CAST")
                it.island as AtomIsland<TriposAtom>
            }

            val molecules = BondAggregator
                .aggregate(bondsByTriposId.values)
                .map { Molecule(it) }

            supermols.add(
                Supermolecule<TriposAtom>(
                    atomIslands + molecules,
                    supermolName
                )
            )
        }

        return supermols.toList()
    }

    companion object {
        /**
         *  Angstrom unit.
         */
        private val ANGSTROM_UNIT: UnitOfMeasure =
            UnitOfMeasure.parse("Ao")
    }
}
