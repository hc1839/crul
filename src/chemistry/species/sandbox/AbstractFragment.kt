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

import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import serialize.BinarySerializable

/**
 *  Skeletal implementation of [chemistry.species.Fragment].
 *
 *  Only [Fragment.clone] needs to be implemented.
 */
abstract class AbstractFragment<A : AbstractAtom> :
    Fragment<A>,
    BinarySerializable
{
    protected val _atoms: MutableList<A>

    override fun atoms(): Iterator<A> =
        _atoms.iterator()

    /**
     *  @param atoms
     *      Atoms of the fragment.
     */
    constructor(atoms: Iterator<A>) {
        this._atoms = atoms.asSequence().toMutableList()

        if (this._atoms.isEmpty()) {
            throw IllegalArgumentException(
                "Fragment must have at least one atom."
            )
        }
    }

    /**
     *  Copy constructor.
     *
     *  Atoms are cloned.
     */
    constructor(other: AbstractFragment<A>): this(
        @Suppress("UNCHECKED_CAST")
        other
            .atoms()
            .asSequence()
            .map { it.clone() as A }
            .iterator()
    )

    /**
     *  Initializes from a MessagePack map.
     *
     *  @param unpackedMap
     *      Unpacked MessagePack map that is specific to this class.
     *
     *  @param atomFactory
     *      Factory function for the atoms.
     *
     *  @param msgpack
     *      MessagePack map for the entire inheritance tree.
     */
    private constructor(
        unpackedMap: Map<String, Value>,
        atomFactory: (ByteArray) -> A,
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray
    ): this(
        unpackedMap["atoms"]!!
            .asArrayValue()
            .list()
            .map {
                atomFactory(it.asBinaryValue().asByteArray())
            }
            .iterator()
    )

    /**
     *  Deserialization constructor.
     */
    constructor(
        msgpack: ByteArray,
        atomFactory: (ByteArray) -> A
    ): this(
        BinarySerializable.getInnerMap(
            msgpack,
            AbstractFragment::class.qualifiedName!!
        ),
        atomFactory,
        msgpack
    )

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(1)

        packer
            .packString("atoms")
            .packArrayHeader(_atoms.count())

        for (atom in _atoms) {
            val atomAsBytes = atom.serialize()

            packer
                .packBinaryHeader(atomAsBytes.count())
                .writePayload(atomAsBytes)
        }

        packer.close()

        return packer.toByteArray()
    }
}
