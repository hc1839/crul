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

package io.github.hc1839.crul.chemistry.species

import io.github.hc1839.crul.distinct.Referential

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
open class Molecule<A : Atom>(bonds: List<Bond<A>>) :
    AbstractMolecule<A>(bonds)
{
    override fun <R : Atom> map(transform: (A) -> R): Molecule<R> {
        val outputAtoms = atoms.map { transform.invoke(it) }

        // Check that there are no two referentially equal output atoms.
        if (outputAtoms.distinctBy(::Referential).count() !=
            outputAtoms.count()
        ) {
            throw RuntimeException(
                "Atom mapper yielded referentially equal atoms."
            )
        }

        // From wrapped input atom to output atom.
        val atomCorrespondence = atoms.zip(outputAtoms) {
            inputAtom, outputAtom ->

            Pair(Referential(inputAtom), outputAtom)
        }.toMap()

        // Recreate bonds with output atoms.
        val outputBonds = bonds.map { inputBond ->
            val (atom1, atom2) = inputBond.toAtomPair()

            Bond(
                atomCorrespondence[Referential(atom1)]!!,
                atomCorrespondence[Referential(atom2)]!!,
                inputBond.bondType
            )
        }

        return Molecule(outputBonds)
    }
}
