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
    val schema: Schema = Schema.Parser().parse(
        """
       |{
       |    "type": "record",
       |    "namespace": "crul.chemistry.species",
       |    "name": "AbstractMoleculeComplex",
       |    "fields": [
       |        {
       |            "type": { "type": "array", "items": "bytes" },
       |            "name": "molecule_subspecies"
       |        },
       |        {
       |            "type": { "type": "array", "items": "bytes" },
       |            "name": "atom_subspecies"
       |        },
       |        { "type": "string", "name": "id" }
       |    ]
       |}
        """.trimMargin()
    )
}

/**
 *  Skeletal implementation of [MoleculeComplex].
 *
 *  @param A
 *      Type of atoms.
 */
abstract class AbstractMoleculeComplex<A : Atom> :
    AbstractComplex<Species>,
    MoleculeComplex<A>
{
    override val id: String

    /**
     *  @param subspecies
     *      Molecules and atoms of the complex.
     *
     *  @param id
     *      Identifier for this complex.
     */
    @JvmOverloads
    constructor(
        subspecies: Collection<Species>,
        id: String = crul.uuid.Generator.inNCName()
    ): super(subspecies)
    {
        if (!subspecies.all { it is Molecule<*> || it is Atom }) {
            throw IllegalArgumentException(
                "Subspecies are not molecules or atoms."
            )
        }

        if (id.isEmpty()) {
            throw IllegalArgumentException(
                "Complex identifier is an empty string."
            )
        }

        // Atom identifiers from all subspecies without removing redundancies.
        val atomIds = subspecies.flatMap { species ->
            species.atoms().map { atom ->
                atom.id
            }
        }

        for (atomId in atomIds.toSet()) {
            if (atomIds.filter { it == atomId }.count() > 1) {
                throw IllegalArgumentException(
                    "Atom identifier exists in more than one subspecies: " +
                    "$atomId"
                )
            }
        }

        this.id = id
    }

    /**
     *  Copy constructor.
     *
     *  @param other
     *      Molecule complex to copy.
     *
     *  @param deep
     *      Whether molecules and atoms are copied.
     *
     *  @param id
     *      Identifier to use for the copied complex.
     */
    @JvmOverloads
    constructor(
        other: AbstractMoleculeComplex<A>,
        deep: Boolean = false,
        id: String = other.id
    ): super(other, deep)
    {
        if (id.isEmpty()) {
            throw IllegalArgumentException(
                "Identifier for the copied complex is an empty string."
            )
        }

        this.id = id
    }

    /**
     *  Delegated deserialization constructor.
     */
    @Suppress("UNCHECKED_CAST")
    private constructor(
        avroRecord: GenericRecord,
        atomDeserializer: (ByteBuffer) -> A
    ): this(
        (avroRecord.get("molecules_subspecies") as List<ByteBuffer>).map {
            Molecule.deserialize(it, atomDeserializer)
        } +
        (avroRecord.get("atom_subspecies") as List<ByteBuffer>).map {
            atomDeserializer.invoke(it)
        },
        avroRecord.get("id").toString()
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

    override fun getSubspeciesWithAtom(atom: A): Species? =
        toList()
            .filter {
                when (it) {
                    is Molecule<*> -> {
                        @Suppress("UNCHECKED_CAST") (
                            (it as Molecule<A>).containsAtom(atom)
                        )
                    }

                    is Atom -> it == atom

                    else -> throw RuntimeException(
                        "[Internal Error] Unexpected type: " +
                        "${it::class.qualifiedName}"
                    )
                }
            }
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

            avroRecord.put("id", obj.id)

            return AvroSimple.serializeData<GenericRecord>(
                AbstractMoleculeComplexAvsc.schema,
                listOf(avroRecord)
            )
        }
    }
}
