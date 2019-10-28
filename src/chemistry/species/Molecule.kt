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

/**
 *  Default implementation of [AbstractMolecule].
 *
 *  @param A
 *      Type of atoms.
 */
class Molecule<A : Atom> : AbstractMolecule<A> {
    /**
     *  @param bonds
     *      Bonds of the molecule.
     */
    constructor(bonds: Collection<Bond<A>>): super(bonds)

    /**
     *  Copy constructor.
     */
    constructor(other: Molecule<A>): super(other)

    override fun clone(): Molecule<A> =
        Molecule(this)
}
