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

package crul.chemistry.species

import crul.distinct.Referential

/**
 *  Complex of molecules and atoms as islands.
 *
 *  @param A
 *      Type of atoms.
 */
interface MoleculeComplex<A : Atom> : Complex<Island<A>> {
    override fun atoms(): List<A> =
        super.atoms().map {
            @Suppress("UNCHECKED_CAST")
            it as A
        }

    abstract override fun clone(): MoleculeComplex<A>

    /**
     *  Sum of the charges of the islands, or `null` if any of the island
     *  charges is `null`.
     */
    fun charge(): Int? =
        subspecies.map { island -> island.charge() }.reduce {
            acc, islandCharge ->

            if (acc != null && islandCharge != null) {
                acc + islandCharge
            } else {
                null
            }
        }

    /**
     *  Gets the island that contains a given atom by referential equality.
     *
     *  If `atom` does not exist in this complex, an exception is thrown.
     */
    fun getIslandWithAtom(atom: A): Island<A>

    /**
     *  Returns a complex without the atoms in a given collection.
     *
     *  Comparison is referential. If an atom does not exist in this complex,
     *  an exception is thrown.
     */
    fun minusAtoms(atoms: Collection<A>): MoleculeComplex<A>

    /**
     *  Returns a complex without the given atom and the bonds that it was
     *  participating in.
     *
     *  Comparison is referential. If `atom` does not exist in this complex, an
     *  exception is thrown.
     */
    fun minusAtom(atom: A): MoleculeComplex<A> =
        minusAtoms(listOf(atom))

    /**
     *  Returns a complex without the bonds in a given collection.
     *
     *  Comparison is referential. If a bond does not exist in this complex, an
     *  exception is thrown.
     */
    fun minusBonds(bonds: Collection<Bond<A>>): MoleculeComplex<A>

    /**
     *  Returns a complex without the given bond, adding new atom islands that
     *  result from the bond removals if necessary.
     *
     *  Comparison is referential. If `bond` does not exist in this complex, an
     *  exception is thrown.
     */
    fun minusBond(bond: Bond<A>): MoleculeComplex<A> =
        minusBonds(listOf(bond))

    /**
     *  Returns a complex without the islands in a given collection.
     *
     *  Comparison is referential. If an island does not exist in this complex,
     *  an exception is thrown.
     */
    fun minusIslands(islands: Collection<Island<A>>): MoleculeComplex<A>

    /**
     *  Returns a complex without the given island.
     *
     *  Comparison is referential. If `island` does not exist in this complex,
     *  an exception is thrown.
     */
    fun minusIsland(island: Island<A>): MoleculeComplex<A> =
        minusIslands(listOf(island))

    companion object {
        /**
         *  Constructs a [MoleculeComplex].
         *
         *  @param islands
         *      Molecules and atoms as islands of the complex.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            islands: List<Island<A>>
        ): MoleculeComplex<A> =
            MoleculeComplex(islands)
    }
}

/**
 *  Constructs a new instance of [MoleculeComplex].
 *
 *  See [MoleculeComplex.newInstance] for description.
 */
fun <A : Atom> MoleculeComplex(
    islands: List<Island<A>>
): MoleculeComplex<A> =
    MoleculeComplexImpl(islands)

/**
 *  Constructs a new molecule complex with the atoms of a given complex mapped
 *  from one type to another.
 *
 *  Connectivities and bond orders are the same as in the receiver.
 *
 *  Atoms in the output complex are not necessarily in the same order as in the
 *  input complex.
 *
 *  @param atomMapper
 *      Atom of a new type given an atom of the original type and the molecule
 *      complex. If it yields an atom that is referentially equal to an atom
 *      that has already been yielded, an exception is raised.
 *
 *  @return
 *      New molecule complex with atoms from the result of applying
 *      `atomMapper` to each atom.
 */
fun <A : Atom, B : Atom> MoleculeComplex<A>.mapAtoms(
    atomMapper: (A, MoleculeComplex<A>) -> B
): MoleculeComplex<B>
{
    // From wrapped input atom to output atom.
    val atomCorrespondence = atoms()
        .map { inputAtom ->
            Referential(inputAtom)
        }
        .associateWith { wrappedInputAtom ->
            atomMapper.invoke(wrappedInputAtom.value, this)
        }

    // Check that there are no two referentially equal output atoms.
    if (
        atomCorrespondence
            .entries
            .map { Referential(it.value) }
            .distinct()
            .count() != atomCorrespondence.count()
    ) {
        throw RuntimeException(
            "Atom mapper yielded referentially equal atoms."
        )
    }

    val outputIslands = mutableListOf<Island<B>>()

    // Convert each input island to output island.
    for (inputIsland in subspecies) {
        if (inputIsland.isSingleAtom) {
            val wrappedInputAtom = Referential(
                inputIsland.atoms().single()
            )

            // Convert input atom island to output atom island.
            outputIslands.add(
                atomCorrespondence[wrappedInputAtom]!!.getIsland<B>()
            )
        } else {
            val outputBonds = mutableListOf<Bond<B>>()

            // Connect output atoms into output bonds.
            for (inputBond in inputIsland.bonds()) {
                val wrappedInputAtoms = inputBond.atoms().map {
                    Referential(it)
                }

                val outputAtoms = wrappedInputAtoms.map {
                    atomCorrespondence[it]!!
                }

                // Add the output bond.
                outputBonds.add(
                    Bond(
                        outputAtoms[0],
                        outputAtoms[1],
                        inputBond.order
                    )
                )
            }

            // Add the output molecule.
            outputIslands.add(
                Molecule(outputBonds.toList())
            )
        }
    }

    // Convert output islands to output complex.
    return MoleculeComplex(outputIslands)
}
