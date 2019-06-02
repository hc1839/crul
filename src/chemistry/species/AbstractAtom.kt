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
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.chemistry.species",
       |    "name": "AbstractAtom",
       |    "fields": [
       |        { "type": "bytes", "name": "element" },
       |        { "type": "bytes", "name": "position" },
       |        { "type": "double", "name": "formal_charge" },
       |        { "type": "string", "name": "id" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Skeletal implementation of [Atom].
 */
abstract class AbstractAtom : Atom {
    override val element: Element

    override var centroid: Vector3D

    override var formalCharge: Double

    override val id: String

    /**
     *  @param element
     *      Element of the atom.
     *
     *  @param centroid
     *      Centroid of the atom.
     *
     *  @param formalCharge
     *      Formal charge of the atom.
     *
     *  @param id
     *      Identifier for this atom.
     */
    @JvmOverloads
    constructor(
        element: Element,
        centroid: Vector3D,
        formalCharge: Double,
        id: String = crul.uuid.Generator.inNCName()
    ) {
        if (id.isEmpty()) {
            throw IllegalArgumentException(
                "Atom identifier is an empty string."
            )
        }

        this.element = element
        this.centroid = centroid
        this.formalCharge = formalCharge
        this.id = id
    }

    /**
     *  Copy constructor.
     */
    @JvmOverloads
    constructor(other: AbstractAtom, id: String = other.id) {
        this.element = other.element
        this.centroid = other.centroid
        this.formalCharge = other.formalCharge
        this.id = id
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
        avroRecord.get("formal-charge") as Double,
        avroRecord.get("id").toString()
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
            avroRecord.put("position", Vector3D.serialize(obj.centroid))
            avroRecord.put("formal_charge", obj.formalCharge)
            avroRecord.put("id", obj.id)

            return AvroSimple.serializeData<GenericRecord>(
                AbstractAtomAvsc.schema,
                listOf(avroRecord)
            )
        }
    }
}
