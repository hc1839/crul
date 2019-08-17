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
import org.apache.avro.Schema
import org.apache.avro.generic.*

import crul.chemistry.species.Element
import crul.math.coordsys.Vector3D
import crul.serialize.AvroSimple

private object AbstractAtomAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/AbstractAtom.avsc"

    /**
     *  Avro schema for the serialization of [AbstractAtom].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Skeletal implementation of [Atom].
 */
abstract class AbstractAtom : Atom {
    override val element: Element

    override var position: Vector3D

    override var charge: Double?

    override var tag: Int

    private var _island: Island<Atom>? = null

    /**
     *  @param element
     *      Element of the atom.
     *
     *  @param position
     *      Position of the atom.
     *
     *  @param charge
     *      Charge associated with the atom.
     *
     *  @param tag
     *      Arbitrary integer tag.
     */
    @JvmOverloads
    constructor(
        element: Element,
        position: Vector3D,
        charge: Double?,
        tag: Int = 0
    ) {
        this.element = element
        this.position = position
        this.charge = charge
        this.tag = tag
    }

    /**
     *  Copy constructor.
     */
    @JvmOverloads
    constructor(other: AbstractAtom, tag: Int = other.tag) {
        this.element = other.element
        this.position = other.position
        this.charge = other.charge
        this.tag = tag
    }

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(avroRecord: GenericRecord): this(
        Element.deserialize(
            avroRecord.get("element") as ByteBuffer
        ),
        Vector3D.deserialize(
            avroRecord.get("position") as ByteBuffer
        ),
        avroRecord.get("charge") as Double?,
        avroRecord.get("tag") as Int
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(avroData: ByteBuffer): this(
        AvroSimple.deserializeData<GenericRecord>(
            AbstractAtomAvsc.schema,
            avroData
        ).first()
    )

    override fun getIsland(islandCharge: Int): Island<Atom> {
        if (_island == null) {
            _island = object :
                AbstractFragment<Atom>(listOf(this@AbstractAtom)),
                Island<Atom>
            {
                override var charge: Int = islandCharge

                override fun bonds(): Collection<Bond<Atom>> =
                    listOf()

                override fun clone(): Island<Atom> {
                    return atoms().single().clone().getIsland(charge)
                }
            }
        }

        _island!!.charge = islandCharge

        return _island!!
    }

    companion object {
        /**
         *  Serializes an [AbstractAtom] in Apache Avro.
         *
         *  @param obj
         *      [AbstractAtom] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: AbstractAtom): ByteBuffer {
            val avroRecord = GenericData.Record(
                AbstractAtomAvsc.schema
            )

            avroRecord.put("element", Element.serialize(obj.element))
            avroRecord.put("position", Vector3D.serialize(obj.position))
            avroRecord.put("charge", obj.charge)
            avroRecord.put("tag", obj.tag)

            return AvroSimple.serializeData<GenericRecord>(
                AbstractAtomAvsc.schema,
                listOf(avroRecord)
            )
        }
    }
}
