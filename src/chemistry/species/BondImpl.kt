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

package chemistry.species

/**
 *  Default implementation of [Bond].
 */
internal class BondImpl<A : Atom> : Bond<A> {
    /**
     *  Pair of the atoms in the bond.
     */
    private val atomPair: Pair<A, A>

    /**
     *  Bond order as specified to the constructor.
     */
    override val order: String

    /**
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
    constructor(
        atom1: A,
        atom2: A,
        order: String
    ) {
        if (atom1 == atom2 || atom1.name == atom2.name) {
            throw IllegalArgumentException(
                "Atoms are equal or have the same name."
            )
        }

        this.atomPair = Pair(atom1, atom2)
        this.order = order
    }

    /**
     *  Copy constructor.
     */
    @Suppress("UNCHECKED_CAST")
    private constructor(other: BondImpl<A>): this(
        other.toAtomPair().first.clone() as A,
        other.toAtomPair().second.clone() as A,
        other.order
    )

    override fun atoms(): Iterator<A> =
        atomPair.toList().iterator()

    override fun containsAtom(atom: A): Boolean =
        atomPair.toList().contains(atom)

    override fun toAtomPair(): Pair<A, A> =
        atomPair

    override fun clone(): Bond<A> =
        BondImpl(this)
}
