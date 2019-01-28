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

import chemistry.species.Element
import chemistry.species.base.Atom as AtomIntf
import math.coordsys.Vector3D
import serialize.BinarySerializable

/**
 *  Skeletal implementation of [chemistry.species.base.Atom].
 *
 *  All properties and member functions are concrete.
 *
 *  @param A
 *      Type parameter that is to be instantiated by a subclass.
 */
abstract class AbstractAtom<A : AbstractAtom<A>> :
    AbstractBasicAtom<A>,
    AtomIntf<A>
{
    override var centroid: Vector3D

    override var formalCharge: Double

    @JvmOverloads
    constructor(
        element: Element,
        centroid: Vector3D,
        formalCharge: Double,
        name: String = uuid.Generator.inNCName()
    ): super(element, name)
    {
        this.centroid = centroid
        this.formalCharge = formalCharge
    }

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractAtom<A>): super(other) {
        this.centroid = other.centroid
        this.formalCharge = other.formalCharge
    }

    /**
     *  Copy constructor using a different atom name.
     */
    constructor(other: AbstractAtom<A>, name: String):
        this(other.element, other.centroid, other.formalCharge, name)

    /**
     *  Initializes from a MessagePack map.
     *
     *  @param unpackedMap
     *      Unpacked MessagePack map that is specific to this class.
     *
     *  @param msgpack
     *      MessagePack map for the entire inheritance tree.
     */
    private constructor(unpackedMap: Map<String, Value>, msgpack: ByteArray):
        super(msgpack)
    {
        this.centroid = Vector3D(
            unpackedMap["centroid"]!!.asBinaryValue().asByteArray()
        )

        this.formalCharge =
            unpackedMap["formal-charge"]!!.asFloatValue().toDouble()
    }

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

    /**
     *  Message serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(2)

        val centroidAsBytes = centroid.serialize()

        packer
            .packString("centroid")
            .packBinaryHeader(centroidAsBytes.count())

        packer.writePayload(centroidAsBytes)

        packer
            .packString("formal-charge")
            .packDouble(formalCharge)

        packer.close()

        return BinarySerializable.addKeyValueEntry(
            super<AbstractBasicAtom>.serialize(),
            this::class.qualifiedName!!,
            packer.toByteArray()
        )
    }
}
