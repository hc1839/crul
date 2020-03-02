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
 *  Default implementation of [AbstractMolecule].
 *
 *  @param A
 *      Type of atoms.
 *
 *  @constructor
 *
 *  @param bonds
 *      Bonds of the molecule.
 */
class Molecule<A : Atom>(bonds: List<Bond<A>>) :
    AbstractMolecule<A>(bonds)
{
    override fun <R : Atom> map(transform: (A) -> R): Molecule<R> {
        // From wrapped input atom to output atom.
        val atomCorrespondence = atoms
            .map { inputAtom ->
                Referential(inputAtom)
            }
            .associateWith { wrappedInputAtom ->
                transform.invoke(wrappedInputAtom.value)
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

        val outputBonds = mutableListOf<Bond<R>>()

        // Connect output atoms into output bonds.
        for (inputBond in bonds) {
            val wrappedInputAtoms = atoms.map {
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

        return Molecule(outputBonds.toList())
    }
}
