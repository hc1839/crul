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
 *  Complex of molecules and atoms as islands.
 *
 *  @param A
 *      Type of atoms.
 */
interface MoleculeComplex<A : Atom> : Complex<Island<A>> {
    override fun atoms(): Collection<A> =
        super.atoms().map {
            @Suppress("UNCHECKED_CAST")
            it as A
        }

    abstract override fun clone(deep: Boolean): MoleculeComplex<A>

    /**
     *  Charge, which is the sum of the charges of the islands.
     */
    val charge: Int
        get() = fold(0) { acc, island ->
            acc + island.charge
        }

    /**
     *  Gets the island that contains a given atom by referential equality, or
     *  `null` if there is no such island.
     */
    fun getIslandWithAtom(atom: A): Island<A>?

    companion object {
        /**
         *  Constructs a [MoleculeComplex].
         *
         *  @param islands
         *      Molecules and atoms as islands of the complex.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            islands: Collection<Island<A>>
        ): MoleculeComplex<A> =
            MoleculeComplexImpl(islands)

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
                "islands",
                obj.map {
                    Island.serialize(it, atomSerializer)
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

            val islands = @Suppress("UNCHECKED_CAST") (
                avroRecord.get("islands") as List<ByteBuffer>
            ).map {
                Island.deserialize(it, atomDeserializer)
            }

            return newInstance(islands)
        }
    }
}
