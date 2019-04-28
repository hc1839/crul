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

private object AtomAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.chemistry.species",
       |    "name": "Atom",
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
 *  Interface for an atom.
 *
 *  An atom is a singleton [Species].
 *
 *  To construct an instance of this class, use [newInstance].
 */
interface Atom : Species {
    /**
     *  Singleton collection containing itself.
     */
    override fun atoms(): Collection<Atom> =
        listOf(this)

    /**
     *  Element.
     */
    val element: Element

    /**
     *  Position of the center.
     */
    var position: Vector3D

    /**
     *  Formal charge.
     */
    var formalCharge: Double

    /**
     *  Identifier for this atom.
     *
     *  It must conform to XML NCName production.
     */
    val id: String

    override fun clone(): Atom =
        super.clone() as Atom

    abstract override fun clone(deep: Boolean): Atom

    /**
     *  Clones this atom using a given identifier.
     *
     *  @param id
     *      Identifier to use for the cloned atom.
     *
     *  @return
     *      Cloned atom.
     */
    fun clone(id: String): Atom

    companion object {
        /**
         *  Constructs an [Atom].
         *
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
         *      Identifier for this atom. It must conform to XML NCName
         *      production.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            position: Vector3D,
            formalCharge: Double,
            id: String
        ): Atom = AtomImpl(
            element,
            position,
            formalCharge,
            id
        )

        /**
         *  Constructs an [Atom] using an automatically generated UUID as the
         *  identifer.
         *
         *  @param element
         *      Element of the atom.
         *
         *  @param position
         *      Position of the center of the atom.
         *
         *  @param formalCharge
         *      Formal charge of the atom.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            position: Vector3D,
            formalCharge: Double
        ): Atom = newInstance(
            element,
            position,
            formalCharge,
            crul.uuid.Generator.inNCName()
        )

        /**
         *  Serializes an [Atom] in Apache Avro.
         *
         *  @param obj
         *      [Atom] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: Atom): ByteBuffer {
            val avroRecord = GenericData.Record(
                AtomAvsc.schema
            )

            avroRecord.put("element", Element.serialize(obj.element))
            avroRecord.put("position", Vector3D.serialize(obj.position))
            avroRecord.put("formal_charge", obj.formalCharge)
            avroRecord.put("id", obj.id)

            return AvroSimple.serializeData<GenericRecord>(
                AtomAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes an [Atom] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Atom] as returned by [serialize].
         *
         *  @return
         *      Deserialized [Atom].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): Atom {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                AtomAvsc.schema,
                avroData
            ).first()

            val element = Element.deserialize(
                avroRecord.get("element") as ByteBuffer
            )

            val position = Vector3D.deserialize(
                avroRecord.get("position") as ByteBuffer
            )

            val formalCharge = avroRecord.get("formal_charge") as Double
            val id = avroRecord.get("id").toString()

            return newInstance(
                element,
                position,
                formalCharge,
                id
            )
        }
    }
}
