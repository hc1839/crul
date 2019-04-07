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

import crul.chemistry.species.Element
import crul.math.coordsys.Vector3D
import crul.serialize.MessagePackSimple

/**
 *  Skeletal implementation of [Atom].
 */
abstract class AbstractAtom : Atom {
    override val element: Element

    override var position: Vector3D

    override var formalCharge: Double

    override val id: String

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
     *  @param id
     *      Identifier for this atom. It must conform to XML NCName production.
     */
    @JvmOverloads
    constructor(
        element: Element,
        position: Vector3D,
        formalCharge: Double,
        id: String = crul.uuid.Generator.inNCName()
    ) {
        if (!crul.xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "ID does not conform to XML NCName production: $id"
            )
        }

        this.element = element
        this.position = position
        this.formalCharge = formalCharge
        this.id = id
    }

    /**
     *  Copy constructor.
     */
    @JvmOverloads
    constructor(other: AbstractAtom, id: String = other.id) {
        this.element = other.element
        this.position = other.position
        this.formalCharge = other.formalCharge
        this.id = id
    }

    /**
     *  Initializes from a MessagePack map.
     *
     *  @param msgpack
     *      MessagePack map for the entire inheritance tree.
     *
     *  @param unpackedMap
     *      Unpacked MessagePack map that is specific to this class.
     *
     *  @param vector3DDeserializer
     *      [Vector3D] deserializer.
     */
    private constructor(
        @Suppress("UNUSED_PARAMETER")
        msgpack: ByteArray,
        unpackedMap: Map<String, Value>,
        vector3DDeserializer: (ByteBuffer) -> Vector3D
    ): this(
        Element.deserialize(
            ByteBuffer.wrap(
                unpackedMap["element"]!!.asBinaryValue().asByteArray()
            )
        ),
        vector3DDeserializer.invoke(
            ByteBuffer.wrap(
                unpackedMap["position"]!!.asBinaryValue().asByteArray()
            )
        ),
        unpackedMap["formal-charge"]!!.asFloatValue().toDouble(),
        unpackedMap["id"]!!.asStringValue().toString()
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(
        msgpack: ByteArray,
        vector3DDeserializer: (ByteBuffer) -> Vector3D
    ): this(
        msgpack,
        MessagePackSimple.getInnerMap(
            msgpack,
            AbstractAtom::class.qualifiedName!!
        ),
        vector3DDeserializer
    )

    override fun hashCode(): Int =
        listOf(element.hashCode(), id.hashCode()).hashCode()

    override fun equals(other: Any?): Boolean =
        other is AbstractAtom &&
        this::class == other::class &&
        (
            element == other.element &&
            id == other.id
        )

    companion object {
        /**
         *  Serializes an [AbstractAtom] in MessagePack.
         *
         *  @param obj
         *      [AbstractAtom] to serialize.
         *
         *  @param vector3DSerializer
         *      [Vector3D] serializer.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(
            obj: AbstractAtom,
            vector3DSerializer: (Vector3D) -> ByteBuffer
        ): ByteBuffer
        {
            val packer = MessagePack.newDefaultBufferPacker()

            packer.packMapHeader(1)

            packer
                .packString(obj::class.qualifiedName)
                .packMapHeader(4)

            val elementAsByteBuffer = Element.serialize(obj.element)
            val elementAsBytes = ByteArray(
                elementAsByteBuffer.limit() -
                elementAsByteBuffer.position()
            ) {
                elementAsByteBuffer.get()
            }

            packer
                .packString("element")
                .packBinaryHeader(elementAsBytes.count())

            packer.writePayload(elementAsBytes)

            val positionAsByteBuffer = vector3DSerializer
                .invoke(obj.position)

            val positionAsBytes = ByteArray(
                positionAsByteBuffer.limit() -
                positionAsByteBuffer.position()
            ) {
                positionAsByteBuffer.get()
            }

            packer
                .packString("position")
                .packBinaryHeader(positionAsBytes.count())

            packer.writePayload(positionAsBytes)

            packer
                .packString("formal-charge")
                .packDouble(obj.formalCharge)

            packer
                .packString("id")
                .packString(obj.id)

            packer.close()

            return ByteBuffer.wrap(packer.toByteArray())
        }

        /**
         *  Serializes an [AbstractAtom] in MessagePack using the default
         *  [Vector3D] serializer.
         *
         *  @param obj
         *      [AbstractAtom] to serialize.
         *
         *  @return
         *      MessagePack serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: AbstractAtom): ByteBuffer =
            serialize(obj, Vector3D.Companion::serialize)
    }
}
