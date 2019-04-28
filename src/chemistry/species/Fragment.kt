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

private object FragmentAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.chemistry.species",
       |    "name": "Fragment",
       |    "fields": [
       |        {
       |            "type": { "type": "array", "items": "bytes" },
       |            "name": "atoms"
       |        }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Interface for a fragment, which is a non-empty [Complex] of [Atom].
 *
 *  @param A
 *      Type of atoms in this fragment.
 */
interface Fragment<A : Atom> : Complex<A> {
    /**
     *  Formal charge of this fragment, which is the sum of the formal charges
     *  of the atoms.
     *
     *  If [atoms] is empty, an exception is raised.
     */
    val formalCharge: Double
        get() = atoms()
            .map { it.formalCharge }
            .reduce { acc, item -> acc + item }

    /**
     *  Whether an atom exists in this fragment.
     */
    fun containsAtom(atom: A): Boolean

    /**
     *  Gets an atom by its ID, or `null` if there is no such atom.
     *
     *  If this fragment has more than one atom with the same given ID, the
     *  first one encountered is returned.
     */
    fun getAtomById(atomId: String): A? =
        @Suppress("UNCHECKED_CAST")
        atoms().firstOrNull { it.id == atomId }

    override fun atoms(): Collection<A> =
        @Suppress("UNCHECKED_CAST") (
            super.atoms() as Collection<A>
        )

    override fun clone(): Fragment<A> =
        @Suppress("UNCHECKED_CAST")
        super.clone() as Fragment<A>

    abstract override fun clone(deep: Boolean): Fragment<A>

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
