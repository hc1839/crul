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

import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import chemistry.species.base.BasicAtom as BasicAtomIntf
import chemistry.species.base.BasicFragment as BasicFragmentIntf
import serialize.BinarySerializable

/**
 *  Skeletal implementation of [chemistry.species.base.BasicFragment].
 *
 *  All properties and member functions are concrete.
 *
 *  @param A
 *      Type parameter that is to be instantiated by a subclass.
 */
abstract class AbstractBasicFragment<A, F> :
    BasicFragmentIntf<A, F>,
    BinarySerializable,
    Cloneable
    where A : AbstractBasicAtom<A>,
          F : AbstractBasicFragment<A, F>
{
    /**
     *  Atoms associated by their names.
     */
    private val atomsByName: MutableMap<String, A> = mutableMapOf()

    override val name: String

    /**
     *  @param atoms
     *      Atoms to include in the fragment.
     *
     *  @param name
     *      Name of the fragment.
     */
    @JvmOverloads
    constructor(
        atoms: Iterable<A>,
        name: String = uuid.Generator.inNCName()
    ) {
        for (atom in atoms) {
            if (atomsByName.containsKey(atom.name)) {
                throw IllegalArgumentException(
                    "Atom with the same name exists: ${atom.name}"
                )
            }

            atomsByName.put(atom.name, atom)
        }

        this.name = name
    }

    /**
     *  Copy constructor.
     *
     *  Atoms are cloned.
     */
    constructor(other: AbstractBasicFragment<A, F>):
        this(other.atoms.map { it.clone() }, other.name)

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
            },
        unpackedMap["name"]!!.asStringValue().toString()
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
            AbstractBasicFragment::class.qualifiedName!!
        ),
        atomFactory,
        msgpack
    )

    override val atoms: List<A>
        get() = atomsByName.values.sortedBy { it.name }

    override fun containsAtom(atomName: String) =
        atomsByName.containsKey(atomName)

    override fun getAtomByName(atomName: String) =
        atomsByName[atomName]

    /**
     *  Clones this fragment and the atoms that it contains.
     */
    public override abstract fun clone(): AbstractBasicFragment<A, F>

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(2)

        val atomsBuf = atoms

        packer
            .packString("atoms")
            .packArrayHeader(atomsBuf.count())

        for (atom in atomsBuf) {
            val atomAsBytes = atom.serialize()

            packer
                .packBinaryHeader(atomAsBytes.count())
                .writePayload(atomAsBytes)
        }

        packer
            .packString("name")
            .packString(name)

        packer.close()

        return packer.toByteArray()
    }
}
