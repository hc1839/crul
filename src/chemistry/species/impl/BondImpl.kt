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

import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.chemistry.species.Atom
import crul.chemistry.species.AtomBuilder
import crul.chemistry.species.Bond
import crul.serialize.BinarySerializable

/**
 *  Default implementation of [Bond].
 */
internal class BondImpl<A : Atom> :
    Bond<A>,
    BinarySerializable
{
    /**
     *  Pair of the atoms in the bond.
     */
    private val atomPair: Pair<A, A>

    /**
     *  Bond order as specified to the constructor.
     */
    override val order: String

    /**
     *  See [crul.chemistry.species.BondFactory.create] for the description.
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

    /**
     *  Initializes from a MessagePack returned by [serialize].
     */
    private constructor(
        unpackedMap: Map<String, Value>,
        atomDeserializer: (ByteArray) -> A,
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray
    ): this(
        atomDeserializer(
            unpackedMap["atom1"]!!.asBinaryValue().asByteArray()
        ),
        atomDeserializer(
            unpackedMap["atom2"]!!.asBinaryValue().asByteArray()
        ),
        unpackedMap["order"]!!.asStringValue().toString()
    )

    /**
     *  Deserialization constructor.
     *
     *  @param atomDeserializer
     *      Deserializer for atoms.
     */
    constructor(
        msgpack: ByteArray,
        atomDeserializer: (ByteArray) -> A
    ): this(
        BinarySerializable.getInnerMap(
            msgpack,
            BondImpl::class.qualifiedName!!
        ),
        atomDeserializer,
        msgpack
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

    /**
     *  MessagePack serialization.
     *
     *  Atoms must implement [BinarySerializable].
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(3)

        val (atom1, atom2) = toAtomPair()

        atom1 as BinarySerializable
        atom2 as BinarySerializable

        val atom1AsBytes = atom1.serialize()

        packer
            .packString("atom1")
            .packBinaryHeader(atom1AsBytes.count())

        packer.writePayload(atom1AsBytes)

        val atom2AsBytes = atom2.serialize()

        packer
            .packString("atom2")
            .packBinaryHeader(atom2AsBytes.count())

        packer.writePayload(atom2AsBytes)

        packer
            .packString("order")
            .packString(order)

        packer.close()

        return packer.toByteArray()
    }
}
