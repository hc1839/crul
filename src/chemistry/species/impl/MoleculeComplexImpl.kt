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

package crul.chemistry.species.impl

import crul.chemistry.species.AbstractMoleculeComplex
import crul.chemistry.species.Atom
import crul.chemistry.species.Molecule
import crul.chemistry.species.MoleculeComplex

/**
 *  Default implementation of [MoleculeComplex].
 *
 *  Only [MoleculeComplexBuilder] should be instantiating this class.
 *
 *  @param A
 *      Type of atoms in the molecules.
 */
internal class MoleculeComplexImpl<A : Atom> : AbstractMoleculeComplex<A> {
    /**
     *  @param molecules
     *      Molecules of the complex.
     */
    constructor(molecules: Iterable<Molecule<A>>): super(molecules)

    /**
     *  Copy constructor.
     */
    constructor(other: MoleculeComplexImpl<A>): super(other)

    override fun clone(): MoleculeComplexImpl<A> =
        MoleculeComplexImpl(this)
}
