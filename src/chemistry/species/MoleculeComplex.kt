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

import crul.serialize.AvroSimple

private object MoleculeComplexAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.chemistry.species",
       |    "name": "MoleculeComplex",
       |    "fields": [
       |        {
       |            "type": { "type": "array", "items": "bytes" },
       |            "name": "molecule_subspecies"
       |        },
       |        {
       |            "type": { "type": "array", "items": "bytes" },
       |            "name": "atom_subspecies"
       |        },
       |        { "type": "string", "name": "id" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Interface for a complex of molecules and atoms.
 *
 *  A subspecies is either a [Molecule] or an [Atom]. An atom identifier exists
 *  in exactly one subspecies.
 *
 *  @param A
 *      Type of atoms.
 */
interface MoleculeComplex<A : Atom> : Complex<Species> {
    override fun atoms(): Collection<A> =
        super.atoms().map {
            @Suppress("UNCHECKED_CAST")
            it as A
        }

    /**
     *  Identifier for this complex.
     *
     *  It conforms to XML NCName production.
     */
    val id: String

    /**
     *  Gets the subspecies that contains a given atom, or `null` if there is
     *  no such subspecies.
     */
    fun getSubspeciesWithAtom(atom: A): Species?

    override fun clone(): MoleculeComplex<A> =
        @Suppress("UNCHECKED_CAST") (
            super.clone() as MoleculeComplex<A>
        )

    abstract override fun clone(deep: Boolean): MoleculeComplex<A>

    /**
     *  Clones this molecule complex using a given identifier.
     *
     *  @param deep
     *      Whether molecules and atoms are cloned.
     *
     *  @param newId
     *      New identifier to use for the cloned molecule complex.
     *
     *  @return
     *      Cloned molecule complex.
     */
    fun clone(deep: Boolean, newId: String): MoleculeComplex<A>

    companion object {
        /**
         *  Constructs a [MoleculeComplex].
         *
         *  @param subspecies
         *      Molecules and atoms of the complex.
         *
         *  @param id
         *      Identifier for this complex. It must conform to XML NCName
         *      production.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            subspecies: Collection<Species>,
            id: String
        ): MoleculeComplex<A> =
            MoleculeComplexImpl(subspecies, id)

        /**
         *  Constructs a [MoleculeComplex] using a UUID Version 4 as its
         *  identifier.
         *
         *  @param subspecies
         *      Molecules and atoms of the complex.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            subspecies: Collection<Species>
        ): MoleculeComplex<A> =
            MoleculeComplexImpl(
                subspecies,
                crul.uuid.Generator.inNCName()
            )

        /**
         *  Serializes a [MoleculeComplex] in Apache Avro.
         *
         *  @param obj
         *      [MoleculeComplex] to serialize.
         *
         *  @param atomSerializer
         *      [Atom] serializer.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun <A : Atom> serialize(
            obj: MoleculeComplex<A>,
            atomSerializer: (A) -> ByteBuffer
        ): ByteBuffer
        {
            val avroRecord = GenericData.Record(
                MoleculeComplexAvsc.schema
            )

            avroRecord.put(
                "molecule_subspecies",
                obj.mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    if (it as? Molecule<A> != null) {
                        Molecule.serialize(it, atomSerializer)
                    } else {
                        null
                    }
                }
            )

            avroRecord.put(
                "atom_subspecies",
                obj.mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    if (it as? A != null) {
                        atomSerializer.invoke(it)
                    } else {
                        null
                    }
                }
            )

            avroRecord.put("id", obj.id)

            return AvroSimple.serializeData<GenericRecord>(
                MoleculeComplexAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [MoleculeComplex] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [MoleculeComplex] as returned by [serialize].
         *
         *  @param atomDeserializer
         *      [Atom] deserializer.
         *
         *  @return
         *      Deserialized [MoleculeComplex].
         */
        @JvmStatic
        fun <A : Atom> deserialize(
            avroData: ByteBuffer,
            atomDeserializer: (ByteBuffer) -> A
        ): MoleculeComplex<A>
        {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                MoleculeComplexAvsc.schema,
                avroData
            ).first()

            val moleculeSubspecies = @Suppress("UNCHECKED_CAST") (
                avroRecord.get("molecule_subspecies") as List<ByteBuffer>
            ).map {
                Molecule.deserialize(it, atomDeserializer)
            }

            val atomSubspecies = @Suppress("UNCHECKED_CAST") (
                avroRecord.get("atom_subspecies") as List<ByteBuffer>
            ).map {
                atomDeserializer.invoke(it)
            }

            val id = avroRecord.get("id").toString()

            return newInstance(moleculeSubspecies + atomSubspecies, id)
        }
    }
}
