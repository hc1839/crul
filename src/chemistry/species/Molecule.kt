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

private object MoleculeAvsc {
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.chemistry.species",
       |    "name": "Molecule",
       |    "fields": [
       |        {
       |            "type": { "type": "map", "values": "bytes" },
       |            "name": "atoms_by_id"
       |        },
       |        {
       |            "type": { "type": "array", "items": "bytes" },
       |            "name": "bonds",
       |            "doc": "crul.chemistry.species.Bond"
       |        }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Interface for a molecule, which is a non-empty [Fragment] of bonds with
 *  unique atom identifiers and has every pair of atoms connected by bonds,
 *  directly or indirectly.
 *
 *  Equality operator, `==`, is used for comparing atoms. Within the same
 *  molecule, two equal atoms must have the same identifier and vice versa.
 *
 *  @param A
 *      Type of atoms in this molecule.
 */
interface Molecule<A : Atom> : Fragment<A> {
    /**
     *  Bonds in this molecule.
     *
     *  Bonds are unique. Bonds are not necessarily in the same order between
     *  calls and are not guaranteed to be in any particular order. A
     *  subinterface or an implementation, however, is allowed to make
     *  specified guarantees.
     */
    fun bonds(): Collection<Bond<A>>

    /**
     *  Gets the bonds that an atom is participating in.
     *
     *  By default, [bonds] is used. An implementation may override this for a
     *  more efficient method.
     *
     *  If a given atom does not exist in this molecule, an exception is
     *  raised.
     *
     *  @return
     *      Bonds that the given atom is participating in.
     */
    fun getBondsByAtom(atom: A): Set<Bond<A>> =
        bonds()
            .filter { it.containsAtom(atom) }
            .toSet()

    /**
     *  Gets the bond between two atoms.
     *
     *  By default, [bonds] is used. An implementation may override this for a
     *  more efficient method.
     *
     *  If a given atom does not exist in this molecule, an exception is
     *  raised.
     *
     *  @param atom1
     *      First atom.
     *
     *  @param atom2
     *      Second atom.
     *
     *  @return
     *      Bond that the given atoms are participating in, or `null` if there
     *      is no bond between the two atoms.
     */
    fun getBond(atom1: A, atom2: A): Bond<A>? =
        bonds()
            .filter {
                it.containsAtom(atom1) &&
                it.containsAtom(atom2)
            }
            .singleOrNull()

    override fun clone(): Molecule<A> =
        @Suppress("UNCHECKED_CAST")
        super.clone() as Molecule<A>

    abstract override fun clone(deep: Boolean): Molecule<A>

    companion object {
        /**
         *  Constructs a [Molecule].
         *
         *  @param bonds
         *      Bonds of the molecule.
         */
        @JvmStatic
        fun <A : Atom> newInstance(bonds: Set<Bond<A>>): Molecule<A> =
            MoleculeImpl(bonds)

        /**
         *  Serializes a [Molecule] in Apache Avro.
         *
         *  @param obj
         *      [Molecule] to serialize.
         *
         *  @param atomSerializer
         *      [Atom] serializer.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun <A : Atom> serialize(
            obj: Molecule<A>,
            atomSerializer: (A) -> ByteBuffer
        ): ByteBuffer
        {
            val avroRecord = GenericData.Record(
                MoleculeAvsc.schema
            )

            avroRecord.put(
                "atoms_by_id",
                obj
                    .atoms()
                    .associateBy { it.id }
                    .mapValues { (_, atom) ->
                        atomSerializer.invoke(atom)
                    }
            )

            avroRecord.put(
                "bonds",
                obj.bonds().map {
                    Bond.serialize(it)
                }
            )

            return AvroSimple.serializeData<GenericRecord>(
                MoleculeAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [Molecule] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Molecule] as returned by [serialize].
         *
         *  @param atomDeserializer
         *      [Atom] deserializer.
         *
         *  @return
         *      Deserialized [Molecule].
         */
        @JvmStatic
        fun <A : Atom> deserialize(
            avroData: ByteBuffer,
            atomDeserializer: (ByteBuffer) -> A
        ): Molecule<A>
        {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                MoleculeAvsc.schema,
                avroData
            ).first()

            val atomsById =
                @Suppress("UNCHECKED_CAST") (
                    avroRecord.get("atoms_by_id")
                    as Map<*, ByteBuffer>
                )
                .map { (atomId, atomBuf) ->
                    Pair(
                        atomId.toString(),
                        atomDeserializer.invoke(atomBuf)
                    )
                }
                .toMap()

            val bonds =
                @Suppress("UNCHECKED_CAST") (
                    avroRecord.get("bonds")
                    as List<ByteBuffer>
                )
                .map { bondBuf ->
                    Bond.deserialize(bondBuf) { atomId ->
                        atomsById[atomId]!!
                    }
                }

            return newInstance(bonds.toSet())
        }
    }
}
