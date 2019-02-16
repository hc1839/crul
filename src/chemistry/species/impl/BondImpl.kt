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

package chemistry.species.impl

import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import chemistry.species.Atom
import chemistry.species.AtomBuilder
import chemistry.species.Bond
import serialize.BinarySerializable

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
    constructor(other: BondImpl<A>): this(
        other.toAtomPair().first.clone() as A,
        other.toAtomPair().second.clone() as A,
        other.order
    )

    /**
     *  Initializes from a MessagePack returned by [serialize].
     */
    @Suppress("UNCHECKED_CAST")
    private constructor(
        unpackedMap: Map<String, Value>,
        atomBuilder: AtomBuilder<*>,
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray
    ): this(
        atomBuilder.deserialize(
            unpackedMap["atom1"]!!.asBinaryValue().asByteArray()
        ) as A,
        atomBuilder.deserialize(
            unpackedMap["atom2"]!!.asBinaryValue().asByteArray()
        ) as A,
        unpackedMap["order"]!!.asStringValue().toString()
    )

    /**
     *  Deserialization constructor.
     *
     *  @param atomBuilder
     *      Builder for deserializing atoms.
     */
    constructor(msgpack: ByteArray, atomBuilder: AtomBuilder<*>): this(
        BinarySerializable.getInnerMap(
            msgpack,
            BondImpl::class.qualifiedName!!
        ),
        atomBuilder,
        msgpack
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
