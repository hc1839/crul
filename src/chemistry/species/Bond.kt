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

private object BondAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.chemistry.species",
       |    "name": "Bond",
       |    "fields": [
       |        { "type": "string", "name": "atom1_id" },
       |        { "type": "string", "name": "atom2_id" },
       |        { "type": "string", "name": "order" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Interface for a bond in a molecule.
 *
 *  Atoms in a bond are not equal to each other and have different identifiers.
 *
 *  To construct an instance of this class, use [newInstance].
 *
 *  @param A
 *      Type of atoms.
 */
interface Bond<A : Atom> : Fragment<A> {
    /**
     *  Bond order as an aribtrary string.
     */
    val order: String

    /**
     *  Atoms as a pair in the given order.
     */
    fun toAtomPair(): Pair<A, A>

    abstract override fun hashCode(): Int

    /**
     *  Bonds are equal if and only if the atoms (without regards to their
     *  order) and bond order are equal.
     */
    abstract override fun equals(other: Any?): Boolean

    override fun clone(): Bond<A> =
        @Suppress("UNCHECKED_CAST") (
            super.clone() as Bond<A>
        )

    abstract override fun clone(deep: Boolean): Bond<A>

    companion object {
        /**
         *  Constructs a [Bond].
         *
         *  If the given atoms are equal or have the same identifier, an
         *  exception is raised.
         *
         *  @param atom1
         *      First atom.
         *
         *  @param atom2
         *      Second atom.
         *
         *  @param order
         *      Bond order as an arbitrary string.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            atom1: A,
            atom2: A,
            order: String
        ): Bond<A> =
            BondImpl(atom1, atom2, order)

        /**
         *  Serializes a [Bond] in Apache Avro.
         *
         *  @param obj
         *      [Bond] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun <A : Atom> serialize(obj: Bond<A>): ByteBuffer {
            val avroRecord = GenericData.Record(
                BondAvsc.schema
            )

            val (atom1, atom2) = obj.toAtomPair()

            avroRecord.put("atom1_id", atom1.id)
            avroRecord.put("atom2_id", atom2.id)
            avroRecord.put("order", obj.order)

            return AvroSimple.serializeData<GenericRecord>(
                BondAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [Bond] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Bond] as returned by [serialize].
         *
         *  @param atomMapper
         *      Function that maps an atom identifier to an [Atom].
         *
         *  @return
         *      Deserialized [Bond].
         */
        @JvmStatic
        fun <A : Atom> deserialize(
            avroData: ByteBuffer,
            atomMapper: (String) -> A
        ): Bond<A>
        {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                BondAvsc.schema,
                avroData
            ).first()

            val atom1 = atomMapper.invoke(
                avroRecord.get("atom1_id").toString()
            )

            val atom2 = atomMapper.invoke(
                avroRecord.get("atom2_id").toString()
            )

            val order = avroRecord.get("order").toString()

            return newInstance(atom1, atom2, order)
        }
    }
}
