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
     *  Tag is cloned.
     */
    abstract override fun clone(): Atom

    /**
     *  Singleton collection containing itself.
     */
    override fun atoms(): Collection<Atom> =
        listOf(this)

    var position: Vector3D

    /**
     *  Element.
     */
    val element: Element

    /**
     *  Charge associated with this atom.
     *
     *  Interpretation depends on the context that the atom is in.
     */
    var charge: Double?

    /**
     *  Arbitrary integer tag.
     */
    var tag: Int

    /**
     *  Island that represents this atom.
     *
     *  Two islands are referentially equal if and only if the two atoms are
     *  referentially equal.
     *
     *  @param islandCharge
     *      Charge to assign to the island. It is independent of the charge
     *      associated with this atom.
     *
     *  @return
     *      Island containing this atom.
     */
    fun island(islandCharge: Int): Island<Atom>

    companion object {
        /**
         *  Constructs an [Atom].
         *
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
        @JvmStatic
        fun newInstance(
            element: Element,
            position: Vector3D,
            charge: Double?,
            tag: Int
        ): Atom = AtomImpl(
            element,
            position,
            charge,
            tag
        )

        /**
         *  Constructs an [Atom] with a tag value of `0`.
         *
         *  @param element
         *      Element of the atom.
         *
         *  @param position
         *      Position of the atom.
         *
         *  @param charge
         *      Charge associated with the atom.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            position: Vector3D,
            charge: Double?
        ): Atom = newInstance(
            element,
            position,
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
            avroRecord.put("position", Vector3D.serialize(obj.position))
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

            val position = Vector3D.deserialize(
                avroRecord.get("position") as ByteBuffer
            )

            val charge = avroRecord.get("charge") as Double?
            val tag = avroRecord.get("tag") as Int

            return newInstance(
                element,
                position,
                charge,
                tag
            )
        }
    }
}
