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

import crul.distinct.Referential
import crul.serialize.AvroSimple

private object AbstractMoleculeAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/AbstractMolecule.avsc"

    /**
     *  Avro schema for the serialization of [AbstractMolecule].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Skeletal implementation of a molecule, which is an [Island] with at least
 *  two atoms that has every pair of atoms connected by bonds, directly or
 *  indirectly.
 *
 *  A molecule is taken in a more general sense to include polyatomic ions.
 *
 *  Atoms are referentially distinct.
 *
 *  @param A
 *      Type of atoms.
 */
abstract class AbstractMolecule<A : Atom> :
    AbstractFragment<A>,
    Island<A>
{
    override var charge: Int

    /**
     *  Lists of bonds associated by the participating atom.
     */
    private val bondListsByAtom: Map<Referential<A>, List<Bond<A>>>

    /**
     *  @param charge
     *      Charge of the molecule.
     *
     *  @param bonds
     *      Non-empty collection of bonds of the molecule. Order is not
     *      important, and referentially equivalent bonds are removed.
     *      Exception is raised if (1) two bonds have referentially equal atoms
     *      but unequal orders or (2) collection of bonds represents more than
     *      one molecule.
     */
    constructor(charge: Int, bonds: Collection<Bond<A>>): super(
        if (!bonds.isEmpty()) {
            bonds
                .flatMap { it.atoms() }
                .distinctBy { Referential(it) }
        } else {
            throw IllegalArgumentException("Collection of bonds is empty.")
        }
    ) {
        this.charge = charge
        this.bondListsByAtom = bondIndexing(bonds)
    }

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractMolecule<A>): this(
        other.charge,
        {
            val clonedAtomsByOtherAtom = other
                .atoms()
                .map { Referential(it) }
                .associateWith {
                    @Suppress("UNCHECKED_CAST")
                    it.value.clone() as A
                }

            // Bonds cannot be directly cloned, since the same atom
            // participating in more than one bond would be cloned.
            other
                .bonds()
                .map { otherBond ->
                    val (otherAtom1, otherAtom2) = otherBond.toAtomPair()

                    Bond.newInstance(
                        clonedAtomsByOtherAtom[
                            Referential(otherAtom1)
                        ]!!,
                        clonedAtomsByOtherAtom[
                            Referential(otherAtom2)
                        ]!!,
                        otherBond.order
                    )
                }
        }.invoke()
    )

    /**
     *  Always `false`.
     */
    final override fun isSingleAtom(): Boolean =
        false

    override fun bonds(): Collection<Bond<A>> =
        bondListsByAtom.values.flatten().distinctBy {
            Referential(it)
        }

    override fun getBondsByAtom(atom: A): List<Bond<A>> {
        val wrappedAtom = Referential(atom)

        if (!bondListsByAtom.containsKey(wrappedAtom)) {
            throw IllegalArgumentException(
                "No such atom."
            )
        }

        val bondList = bondListsByAtom[wrappedAtom]!!

        return bondList.toList()
    }

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(
        avroRecord: GenericRecord,
        atomDeserializer: (ByteBuffer) -> A
    ): super(
        @Suppress("UNCHECKED_CAST") (
            avroRecord.get("atoms") as List<ByteBuffer>
        )
        .map { atomDeserializer.invoke(it) }
    ) {
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
                    subspecies[atom1Index],
                    subspecies[atom2Index],
                    bondOrder
                )
            }

        this.charge = avroRecord.get("charge") as Int
        this.bondListsByAtom = bondIndexing(bonds)
    }

    /**
     *  Deserialization constructor.
     */
    protected constructor(
        avroData: ByteBuffer,
        atomDeserializer: (ByteBuffer) -> A
    ): this(
        AvroSimple.deserializeData<GenericRecord>(
            AbstractMoleculeAvsc.schema,
            avroData
        ).first(),
        atomDeserializer
    )

    override fun getBond(atom1: A, atom2: A): Bond<A>? {
        val wrappedAtom1 = Referential(atom1)
        val wrappedAtom2 = Referential(atom2)

        if (
            !bondListsByAtom.contains(wrappedAtom1) ||
            !bondListsByAtom.contains(wrappedAtom2)
        ) {
            return null
        }

        val bond = bondListsByAtom[wrappedAtom1]!!.intersect(
            bondListsByAtom[wrappedAtom2]!!
        ).singleOrNull()

        return if (bond == null) {
            null
        } else {
            val wrappedBondAtoms = bond
                .atoms()
                .map { Referential(it) }
                .toSet()

            if (wrappedBondAtoms == setOf(wrappedAtom1, wrappedAtom2)) {
                bond
            } else {
                null
            }
        }
    }

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
         *  Indexing of the bonds from a collection of bonds.
         *
         *  @param bonds
         *      Non-empty collection of bonds of the molecule. Order is not
         *      important, and referentially equivalent bonds are removed.
         *      Exception is raised if (1) two bonds have referentially equal
         *      atoms but unequal orders or (2) collection of bonds represents
         *      more than one molecule.
         *
         *  @return
         *      Map of wrapped atom to list of bonds that the atom is
         *      participating.
         */
        private fun <A : Atom> bondIndexing(
            bonds: Collection<Bond<A>>
        ): Map<Referential<A>, List<Bond<A>>>
        {
            if (bonds.isEmpty()) {
                throw IllegalArgumentException(
                    "Collection of bonds is empty."
                )
            }

            val bondAggregates = BondAggregator.aggregate(bonds)

            if (bondAggregates.count() != 1) {
                throw IllegalArgumentException(
                    "Collection of bonds represents more than one molecule."
                )
            }

            val bondAggregate = bondAggregates.single()

            val bondListsByAtom =
                mutableMapOf<Referential<A>, MutableList<Bond<A>>>()

            for (bond in bondAggregate) {
                for (atom in bond.atoms()) {
                    val wrappedAtom = Referential(atom)

                    if (!bondListsByAtom.containsKey(wrappedAtom)) {
                        bondListsByAtom[wrappedAtom] = mutableListOf()
                    }

                    bondListsByAtom[wrappedAtom]!!.add(bond)
                }
            }

            return bondListsByAtom.mapValues { (_, bondList) ->
                bondList.toList()
            }
        }

        /**
         *  Serializes an [AbstractMolecule] in Apache Avro.
         *
         *  @param obj
         *      [AbstractMolecule] to serialize.
         *
         *  @param atomSerializer
         *      [Atom] serializer.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun <A : Atom> serialize(
            obj: AbstractMolecule<A>,
            atomSerializer: (A) -> ByteBuffer
        ): ByteBuffer
        {
            val avroRecord = GenericData.Record(
                AbstractMoleculeAvsc.schema
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
                AbstractMoleculeAvsc.schema,
                listOf(avroRecord)
            )
        }
    }
}
