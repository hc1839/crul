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
 *  Factory for [Bond].
 *
 *  @param A
 *      Type of atoms participating in the bond being constructed.
 */
open class BondFactory<A : Atom> {
    /**
     *  Builder for construction.
     */
    protected val builder: BondBuilder<*> by lazy {
        BondBuilder.newInstance()
    }

    constructor()

    /**
     *  Constructs a [Bond].
     *
     *  If the given atoms are equal or have the same name, an exception is
     *  raised.
     *
     *  @param atom1
     *      First atom.
     *
     *  @param atom2
     *      Second atom.
     *
     *  @param order
     *      Bond order as an arbitrary string.
     */
    open fun create(atom1: A, atom2: A, order: String): Bond<A> =
        builder
            .setAtom1(atom1)
            .setAtom2(atom2)
            .setOrder(order)
            .build<A>()
}
