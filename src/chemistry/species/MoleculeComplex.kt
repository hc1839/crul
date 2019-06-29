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
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/MoleculeComplex.avsc"

    /**
     *  Avro schema for the serialization of [MoleculeComplex].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
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
     *  Gets the subspecies that contains a given atom by referential equality,
     *  or `null` if there is no such subspecies.
     */
    fun getSubspeciesWithAtom(atom: A): Species?

    abstract override fun clone(deep: Boolean): MoleculeComplex<A>

    companion object {
        /**
         *  Constructs a [MoleculeComplex].
         *
         *  @param subspecies
         *      Molecules and atoms of the complex.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            subspecies: Collection<Species>
        ): MoleculeComplex<A> =
            MoleculeComplexImpl(subspecies)

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

            return newInstance(moleculeSubspecies + atomSubspecies)
        }
    }
}
