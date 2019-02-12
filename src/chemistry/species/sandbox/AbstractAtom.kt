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

import chemistry.species.Element
import math.coordsys.Vector3D
import serialize.BinarySerializable

/**
 *  Skeletal implementation of [chemistry.species.Atom].
 *
 *  Only [Atom.clone] needs to be implemented.
 */
abstract class AbstractAtom :
    Atom,
    BinarySerializable
{
    override val element: Element

    override val name: String

    override var position: Vector3D

    override var formalCharge: Double

    /**
     *  @param element
     *      Element of the atom.
     *
     *  @param position
     *      Position of the center of the atom.
     *
     *  @param formalCharge
     *      Formal charge of the atom.
     *
     *  @param name
     *      Name of the atom. It must conform to XML NCName production.
     */
    @JvmOverloads
    constructor(
        element: Element,
        position: Vector3D,
        formalCharge: Double,
        name: String = uuid.Generator.inNCName()
    ) {
        if (!xml.Datatype.isNCName(name)) {
            throw IllegalArgumentException(
                "Name does not conform to XML NCName production: $name"
            )
        }

        this.element = element
        this.position = position
        this.formalCharge = formalCharge
        this.name = name
    }

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractAtom) {
        this.element = other.element
        this.position = other.position
        this.formalCharge = other.formalCharge
        this.name = other.name
    }

    /**
     *  Copy constructor using a different atom name.
     */
    constructor(other: AbstractAtom, name: String):
        this(other.element, other.position, other.formalCharge, name)

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
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray
    ): this(
        Element(
            unpackedMap["element"]!!.asBinaryValue().asByteArray()
        ),
        Vector3D(
            unpackedMap["position"]!!.asBinaryValue().asByteArray()
        ),
        unpackedMap["formal-charge"]!!.asFloatValue().toDouble(),
        unpackedMap["name"]!!.asStringValue().toString()
    )

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(
        BinarySerializable.getInnerMap(
            msgpack,
            AbstractAtom::class.qualifiedName!!
        ),
        msgpack
    )

    override fun hashCode() =
        listOf(element.hashCode(), name.hashCode()).hashCode()

    override fun equals(other: Any?) =
        other is AbstractAtom &&
        this::class == other::class &&
        (
            element == other.element &&
            name == other.name
        )

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(4)

        val elementAsBytes = element.serialize()

        packer
            .packString("element")
            .packBinaryHeader(elementAsBytes.count())

        packer.writePayload(elementAsBytes)

        packer
            .packString("name")
            .packString(name)

        val positionAsBytes = position.serialize()

        packer
            .packString("position")
            .packBinaryHeader(positionAsBytes.count())

        packer.writePayload(positionAsBytes)

        packer
            .packString("formal-charge")
            .packDouble(formalCharge)

        packer.close()

        return packer.toByteArray()
    }
}