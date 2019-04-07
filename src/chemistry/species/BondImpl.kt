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

import java.nio.ByteBuffer
import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.serialize.MessagePackSimple

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
     *  See [crul.chemistry.species.Bond.newInstance] for the description.
     */
    constructor(
        atom1: A,
        atom2: A,
        order: String
    ) {
        if (atom1 == atom2 || atom1.id == atom2.id) {
            throw IllegalArgumentException(
                "Atoms are equal or have the same ID."
            )
        }

        this.atomPair = Pair(atom1, atom2)
        this.order = order
    }

    /**
     *  Copy constructor.
     */
    @Suppress("UNCHECKED_CAST")
    constructor(other: BondImpl<A>): this(
        other.toAtomPair().first.clone() as A,
        other.toAtomPair().second.clone() as A,
        other.order
    )

    override fun hashCode(): Int =
        atomPair.toList().toSet().hashCode()

    override fun equals(other: Any?): Boolean =
        other is BondImpl<*> &&
        this::class == other::class &&
        (
            atomPair.toList().toSet() == other.atomPair.toList().toSet() &&
            order == other.order
        )

    override fun iterator(): Iterator<A> =
        atomPair.toList().iterator()

    override fun containsAtom(atom: A): Boolean =
        atomPair.toList().contains(atom)

    override fun toAtomPair(): Pair<A, A> =
        atomPair

    override fun clone(): Bond<A> =
        BondImpl(this)
}
