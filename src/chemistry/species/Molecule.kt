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
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/Molecule.avsc"

    /**
     *  Avro schema for the serialization of [Molecule].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Interface for a molecule, which is a [Fragment] of at least two atoms and
 *  has every pair of atoms connected by bonds, directly or indirectly.
 *
 *  Atoms are referentially distinct.
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
    fun getBondsByAtom(atom: A): List<Bond<A>> =
        bonds().filter { it.containsAtom(atom) }

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
         *  Avro schema of a record that is the serialization of a bond.
         */
        private val bondRecordSchema: Schema = Schema.Parser().parse(
            """
           |{
           |    "type": "record",
           |    "name": "bond",
           |    "fields": [
           |        { "type": "int", "name": "atom1_index" },
           |        { "type": "int", "name": "atom2_index" },
           |        { "type": "string", "name": "bond_order" }
           |    ]
           |}
            """.trimMargin()
        )

        /**
         *  Constructs a [Molecule].
         *
         *  @param bonds
         *      Bonds of the molecule.
         */
        @JvmStatic
        fun <A : Atom> newInstance(bonds: Collection<Bond<A>>): Molecule<A> =
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

            val atoms = obj.atoms()

            avroRecord.put(
                "atoms",
                atoms.map {
                    atomSerializer.invoke(it)
                }
            )

            // Serialize the bonds to Avro records.
            val bondsArray = obj.bonds().map { bond ->
                val bondRecord = GenericData.Record(
                    bondRecordSchema
                )

                val atomIndices = bond.toAtomPair().toList().map { bondAtom ->
                    atoms.indexOfFirst { objAtom ->
                        objAtom === bondAtom
                    }
                }

                bondRecord.put("atom1_index", atomIndices[0])
                bondRecord.put("atom2_index", atomIndices[1])
                bondRecord.put("bond_order", bond.order)

                bondRecord
            }

            avroRecord.put("bonds", bondsArray)

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

            val atoms =
                @Suppress("UNCHECKED_CAST") (
                    avroRecord.get("atoms") as List<ByteBuffer>
                )
                .map {
                    atomDeserializer.invoke(it)
                }

            // Deserialize the bonds from Avro records.
            val bonds =
                @Suppress("UNCHECKED_CAST") (
                    avroRecord.get("bonds") as List<GenericRecord>
                )
                .map { bondRecord ->
                    val atom1Index = bondRecord.get("atom1_index") as Int
                    val atom2Index = bondRecord.get("atom2_index") as Int
                    val bondOrder = bondRecord.get("bond_order").toString()

                    Bond.newInstance(
                        atoms[atom1Index],
                        atoms[atom2Index],
                        bondOrder
                    )
                }

            return newInstance(bonds)
        }
    }
}
