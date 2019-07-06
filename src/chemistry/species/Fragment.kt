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

import crul.math.coordsys.Vector3D
import crul.serialize.AvroSimple

private object FragmentAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/Fragment.avsc"

    /**
     *  Avro schema for the serialization of [Fragment].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Interface for a fragment, which is a non-empty [Complex] of [Atom]
 *  instances.
 *
 *  Atoms are compared by referentially equality.
 *
 *  @param A
 *      Type of atoms in this fragment.
 */
interface Fragment<A : Atom> : Complex<A> {
    /**
     *  Whether an atom exists in this fragment.
     */
    fun containsAtom(atom: A): Boolean =
        atoms().any { it === atom }

    override fun atoms(): Collection<A> =
        @Suppress("UNCHECKED_CAST") (
            super.atoms() as Collection<A>
        )

    override fun getAtomsByTag(tag: Int): List<A> =
        @Suppress("UNCHECKED_CAST") (
            super.getAtomsByTag(tag) as List<A>
        )

    abstract override fun clone(): Fragment<A>

    companion object {
        /**
         *  Constructs a [Fragment].
         *
         *  @param atoms
         *      Atoms of the fragment.
         */
        @JvmStatic
        fun <A : Atom> newInstance(atoms: Collection<A>): Fragment<A> =
            FragmentImpl(atoms)

        /**
         *  Serializes a [Fragment] in Apache Avro.
         *
         *  @param obj
         *      [Fragment] to serialize.
         *
         *  @param atomSerializer
         *      [Atom] serializer.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun <A : Atom> serialize(
            obj: Fragment<A>,
            atomSerializer: (A) -> ByteBuffer
        ): ByteBuffer
        {
            val avroRecord = GenericData.Record(
                FragmentAvsc.schema
            )

            avroRecord.put(
                "atoms",
                obj.atoms().map {
                    atomSerializer.invoke(it)
                }
            )

            return AvroSimple.serializeData<GenericRecord>(
                FragmentAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [Fragment] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Fragment] as returned by [serialize].
         *
         *  @param atomDeserializer
         *      [Atom] deserializer
         *
         *  @return
         *      Deserialized [Fragment].
         */
        @JvmStatic
        fun <A : Atom> deserialize(
            avroData: ByteBuffer,
            atomDeserializer: (ByteBuffer) -> A
        ): Fragment<A>
        {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                FragmentAvsc.schema,
                avroData
            ).first()

            val atoms = @Suppress("UNCHECKED_CAST") (
                avroRecord.get("atoms") as List<ByteBuffer>
            ).map {
                atomDeserializer.invoke(it)
            }

            return newInstance(atoms)
        }
    }
}
