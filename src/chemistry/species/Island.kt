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

private object IslandAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/Island.avsc"

    /**
     *  Avro schema for the serialization of [Island].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Group containing atoms of a molecule or a single atom.
 *
 *  To construct an island representing a molecule, instantiate a [Molecule].
 *  For a single atom, use [Atom.island].
 *
 *  @param A
 *      Type of atoms in this island.
 */
interface Island<A : Atom> : Fragment<A> {
    abstract override fun clone(): Island<A>

    /**
     *  Whether this island contains a single atom.
     *
     *  Implementation may override this to be a constant property.
     */
    val isSingleAtom: Boolean
        get() = atoms().count() == 1

    /**
     *  Charge of the molecule or atom represented by this island.
     *
     *  It is independent of the charges associated with the individual atoms.
     */
    var charge: Int

    /**
     *  Bonds in this island, or an empty collection if the island contains a
     *  single atom.
     *
     *  Bonds are unique. Bonds are not necessarily in the same order between
     *  calls and are not guaranteed to be in any particular order.
     */
    fun bonds(): Collection<Bond<A>>

    /**
     *  Gets the bonds that an atom is participating in.
     *
     *  By default, [bonds] is used. An implementation may override this for a
     *  more efficient method.
     *
     *  @param atom
     *      Atom whose bonds is to be retrieved.
     *
     *  @return
     *      Bonds that `atom` is participating in. If `atom` does not exist in
     *      this island, an empty list is returned.
     */
    fun getBondsByAtom(atom: A): List<Bond<A>> =
        bonds().filter { it.containsAtom(atom) }

    /**
     *  Gets the atoms that are bonded to a given atom.
     *
     *  @param atom
     *      Atom whose bonded atoms are to be retrieved.
     *
     *  @return
     *      Atoms bonded to `atom`. If `atom` does not exist in this island, an
     *      empty list is returned.
     */
    fun getAtomsBondedTo(atom: A): List<A> =
        getBondsByAtom(atom)
            .flatMap { bond ->
                bond.toAtomPair().toList()
            }
            .filter {
                it !== atom
            }

    /**
     *  Gets the bond between two atoms.
     *
     *  By default, [bonds] is used. An implementation may override this for a
     *  more efficient method.
     *
     *  @param atom1
     *      First atom.
     *
     *  @param atom2
     *      Second atom.
     *
     *  @return
     *      Bond that the given atoms are participating in, or `null` if there
     *      is no bond between the two atoms. If a given atom does not exist in
     *      this island, `null` is returned.
     */
    fun getBond(atom1: A, atom2: A): Bond<A>? =
        bonds()
            .filter {
                it.containsAtom(atom1) &&
                it.containsAtom(atom2)
            }
            .singleOrNull()

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
         *  Serializes an [Island] in Apache Avro.
         *
         *  @param obj
         *      [Island] to serialize.
         *
         *  @param atomSerializer
         *      [Atom] serializer.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun <A : Atom> serialize(
            obj: Island<A>,
            atomSerializer: (A) -> ByteBuffer
        ): ByteBuffer
        {
            val avroRecord = GenericData.Record(
                IslandAvsc.schema
            )

            avroRecord.put("charge", obj.charge)

            val atoms = obj.atoms()

            avroRecord.put(
                "atoms",
                atoms.map {
                    atomSerializer.invoke(it)
                }
            )

            // Serialize the bonds to Avro records.
            val bondRecords = obj.bonds().map { bond ->
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

            avroRecord.put("bonds", bondRecords)

            return AvroSimple.serializeData<GenericRecord>(
                IslandAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes an [Island] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [Island] as returned by [serialize].
         *
         *  @param atomDeserializer
         *      [Atom] deserializer.
         *
         *  @return
         *      Deserialized [Island].
         */
        @JvmStatic
        fun <A : Atom> deserialize(
            avroData: ByteBuffer,
            atomDeserializer: (ByteBuffer) -> A
        ): Island<A>
        {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                IslandAvsc.schema,
                avroData
            ).first()

            val charge = avroRecord.get("charge") as Int

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

                    Bond(
                        atoms[atom1Index],
                        atoms[atom2Index],
                        bondOrder
                    )
                }

            return if (bonds.isEmpty()) {
                atoms.single().getIsland<A>(charge)
            } else {
                Molecule(charge, bonds)
            }
        }
    }
}
