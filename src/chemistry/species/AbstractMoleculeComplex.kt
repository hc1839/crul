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

private object AbstractMoleculeComplexAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/AbstractMoleculeComplex.avsc"

    /**
     *  Avro schema for the serialization of [AbstractMoleculeComplex].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Skeletal implementation of [MoleculeComplex].
 *
 *  @param A
 *      Type of atoms.
 */
abstract class AbstractMoleculeComplex<A : Atom> :
    AbstractComplex<Island<A>>,
    MoleculeComplex<A>
{
    /**
     *  @param subspecies
     *      Molecules and atoms of the complex.
     */
    constructor(islands: Collection<Island<A>>): super(islands) {
        // Referentially distinct atoms from all islands.
        val wrappedAtomSets = islands.map { island ->
            island.atoms().map { atom ->
                SpeciesSetElement(atom)
            }.toSet()
        }

        // Number of distinct atoms across all islands.
        val numDistinctAtoms = wrappedAtomSets
            .flatten()
            .distinct()
            .count()

        if (
            numDistinctAtoms !=
                wrappedAtomSets.fold(0) { acc, item ->
                    acc + item.count()
                }
        ) {
            throw IllegalArgumentException(
                "At least one atom exists in more than one island."
            )
        }
    }

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractMoleculeComplex<A>): super(other)

    /**
     *  Delegated deserialization constructor.
     */
    @Suppress("UNCHECKED_CAST")
    private constructor(
        avroRecord: GenericRecord,
        atomDeserializer: (ByteBuffer) -> A
    ): this(
        (avroRecord.get("islands") as List<ByteBuffer>).map {
            Island.deserialize(it, atomDeserializer)
        }
    )

    /**
     *  Deserialization constructor.
     */
    protected constructor(
        avroData: ByteBuffer,
        atomDeserializer: (ByteBuffer) -> A
    ): this(
        AvroSimple.deserializeData<GenericRecord>(
            AbstractMoleculeComplexAvsc.schema,
            avroData
        ).first(),
        atomDeserializer
    )

    override fun getIslandWithAtom(atom: A): Island<A>? =
        toList()
            .filter { island -> island.containsAtom(atom) }
            .singleOrNull()

    companion object {
        /**
         *  Serializes a [AbstractMoleculeComplex] in Apache Avro.
         *
         *  @param obj
         *      [AbstractMoleculeComplex] to serialize.
         *
         *  @param atomSerializer
         *      [Atom] serializer.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun <A : Atom> serialize(
            obj: AbstractMoleculeComplex<A>,
            atomSerializer: (A) -> ByteBuffer
        ): ByteBuffer
        {
            val avroRecord = GenericData.Record(
                AbstractMoleculeComplexAvsc.schema
            )

            avroRecord.put(
                "islands",
                obj.map {
                    Island.serialize(it, atomSerializer)
                }
            )

            return AvroSimple.serializeData<GenericRecord>(
                AbstractMoleculeComplexAvsc.schema,
                listOf(avroRecord)
            )
        }
    }
}
