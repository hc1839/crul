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
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/Atom.avsc"

    /**
     *  Avro schema for the serialization of [Atom].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
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

    override var centroid: Vector3D

    /**
     *  Charge associated with this atom.
     *
     *  Interpretation depends on the context that the atom is in.
     */
    override var charge: Double

    /**
     *  Clones this atom with a new tag.
     */
    fun clone(newTag: Int): Atom

    /**
     *  Arbitrary integer tag.
     */
    var tag: Int

    abstract override fun clone(deep: Boolean): Atom

    companion object {
        /**
         *  Constructs an [Atom].
         *
         *  @param element
         *      Element of the atom.
         *
         *  @param centroid
         *      Centroid of the atom.
         *
         *  @param charge
         *      Charge associated with the atom.
         *
         *  @param tag
         *      Arbitrary integer tag.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            centroid: Vector3D,
            charge: Double,
            tag: Int
        ): Atom = AtomImpl(
            element,
            centroid,
            charge,
            tag
        )

        /**
         *  Constructs an [Atom] with a tag value of `0`.
         *
         *  @param element
         *      Element of the atom.
         *
         *  @param centroid
         *      Centroid of the atom.
         *
         *  @param charge
         *      Charge associated with the atom.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            centroid: Vector3D,
            charge: Double
        ): Atom = newInstance(
            element,
            centroid,
            charge,
            0
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
            avroRecord.put("centroid", Vector3D.serialize(obj.centroid))
            avroRecord.put("charge", obj.charge)
            avroRecord.put("tag", obj.tag)

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

            val centroid = Vector3D.deserialize(
                avroRecord.get("centroid") as ByteBuffer
            )

            val charge = avroRecord.get("charge") as Double
            val tag = avroRecord.get("tag") as Int

            return newInstance(
                element,
                centroid,
                charge,
                tag
            )
        }
    }
}
