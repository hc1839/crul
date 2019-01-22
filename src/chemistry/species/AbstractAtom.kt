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
    override var formalCharge: Double

    override var centroid: Vector3D

    @JvmOverloads
    constructor(
        element: Element,
        formalCharge: Double,
        centroid: Vector3D,
        name: String = uuid.Generator.inNCName()
    ): super(element, name)
    {
        this.formalCharge = formalCharge
        this.centroid = centroid
    }

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractAtom<A>): super(other) {
        this.formalCharge = other.formalCharge
        this.centroid = other.centroid
    }

    /**
     *  Copy constructor using a different atom name.
     */
    constructor(other: AbstractAtom<A>, name: String):
        this(other.element, other.formalCharge, other.centroid, name)

    /**
     *  Data-based constructor.
     */
    private constructor(ctorArgs: CtorArgs): super(ctorArgs.restTreeLine) {
        this.formalCharge = ctorArgs.formalCharge
        this.centroid = ctorArgs.centroid
    }

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): this(getCtorArgs(msgpack))

    /**
     *  Message serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(2)

        packer
            .packString("formal-charge")
            .packDouble(formalCharge)

        val centroidAsBytes = centroid.serialize()

        packer
            .packString("centroid")
            .packBinaryHeader(centroidAsBytes.count())

        packer.writePayload(centroidAsBytes)

        packer.close()

        return BinarySerializable.addKeyValueEntry(
            super<AbstractBasicAtom>.serialize(),
            this::class.qualifiedName!!,
            packer.toByteArray()
        )
    }

    companion object {
        /**
         *  Constructor arguments.
         */
        private data class CtorArgs(
            val formalCharge: Double,
            val centroid: Vector3D,
            val restTreeLine: ByteArray
        )

        /**
         *  Gets the constructor arguments from [serialize].
         */
        private fun getCtorArgs(msgpack: ByteArray): CtorArgs {
            val (unpackedMap, restTreeLine) = BinarySerializable
                .getMapRestPair(
                    msgpack,
                    AbstractAtom::class.qualifiedName!!
                )

            return CtorArgs(
                unpackedMap["formal-charge"]!!.asFloatValue().toDouble(),
                Vector3D(
                    unpackedMap["centroid"]!!.asBinaryValue().asByteArray()
                ),
                restTreeLine
            )
        }
    }
}
