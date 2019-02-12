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

package chemistry.species.sandbox

/**
 *  Default implementation of [Bond].
 */
internal class BondImpl<A : Atom> : Bond<A> {
    /**
     *  Pair of the atoms in the bond.
     */
    private val atomPair: Pair<A, A>

    /**
     *  Bond order as an aribtrary string at the time of retrieval from the
     *  parent complex.
     *
     *  If the bond order is changed in the parent complex, it is not reflected
     *  here.
     */
    override val order: String

    /**
     *  @param atom1
     *      First atom.
     *
     *  @param atom2
     *      Second atom.
     *
     *  @param order
     *      Bond order as an arbitrary string.
     */
    constructor(
        atom1: A,
        atom2: A,
        order: String
    ) {
        this.atomPair = Pair(atom1, atom2)
        this.order = order
    }

    /**
     *  Copy constructor.
     */
    @Suppress("UNCHECKED_CAST")
    private constructor(other: BondImpl<A>): this(
        other.component1().clone() as A,
        other.component2().clone() as A,
        other.order
    )

    override fun clone(): Fragment<A> =
        BondImpl(this)

    override fun atoms(): Iterator<A> =
        atomPair.toList().iterator()

    override fun component1(): A =
        atomPair.first

    override fun component2(): A =
        atomPair.second
}
