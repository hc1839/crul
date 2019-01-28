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
import serialize.BinarySerializable

/**
 *  Skeletal implementation of [chemistry.species.base.BasicAtom].
 *
 *  All properties and member functions are concrete.
 *
 *  @param A
 *      Type parameter that is to be instantiated by a subclass.
 */
abstract class AbstractBasicAtom<A : AbstractBasicAtom<A>> :
    BasicAtomIntf<A>,
    BinarySerializable,
    Cloneable
{
    override val element: Element

    override val name: String

    /**
     *  @param element
     *      Symbol of the element of the atom.
     *
     *  @param name
     *      Name of the atom. It must conform to XML NCName production.
     */
    @JvmOverloads
    constructor(element: Element, name: String = uuid.Generator.inNCName()) {
        if (!xml.Datatype.isNCName(name)) {
            throw IllegalArgumentException(
                "Name does not conform to XML NCName production: $name"
            )
        }

        this.element = element
        this.name = name
    }

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractBasicAtom<A>):
        this(other.element, other.name)

    /**
     *  Copy constructor using a different atom name.
     */
    constructor(other: AbstractBasicAtom<A>, name: String):
        this(other.element, name)

    /**
     *  Initializes from a MessagePack map.
     *
     *  @param unpackedMap
     *      Unpacked MessagePack map that is specific to this class.
     *
     *  @param msgpack
     *      MessagePack map for the entire inheritance tree.
     */
    private constructor(
        unpackedMap: Map<String, Value>,
        @Suppress("UNUSED_PARAMETER") msgpack: ByteArray
    ): this(
        Element(
            unpackedMap["element"]!!.asBinaryValue().asByteArray()
        ),
        unpackedMap["name"]!!.asStringValue().toString()
    )

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        BinarySerializable.getInnerMap(
            msgpack,
            AbstractBasicAtom::class.qualifiedName!!
        ),
        msgpack
    )

    override fun hashCode() =
        listOf(name, element.hashCode()).hashCode()

    override fun equals(other: Any?) =
        other is AbstractBasicAtom<*> &&
        this::class == other::class &&
        (
            name == other.name &&
            element == other.element
        )

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(2)

        val elementAsBytes = element.serialize()

        packer
            .packString("element")
            .packBinaryHeader(elementAsBytes.count())

        packer.writePayload(elementAsBytes)

        packer
            .packString("name")
            .packString(name)

        packer.close()

        return packer.toByteArray()
    }

    /**
     *  Clones this atom.
     */
    public override abstract fun clone(): A
}
