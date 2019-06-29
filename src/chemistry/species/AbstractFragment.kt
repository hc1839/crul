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

private object AbstractFragmentAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/crul/chemistry/species/AbstractFragment.avsc"

    /**
     *  Avro schema for the serialization of [AbstractFragment].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Skeletal implementation of [Fragment].
 */
abstract class AbstractFragment<A : Atom> :
    AbstractComplex<A>,
    Fragment<A>
{
    /**
     *  @param atoms
     *      Atoms of the fragment.
     */
    constructor(atoms: Collection<A>): super(atoms) {
        if (atoms.isEmpty()) {
            throw IllegalArgumentException(
                "Fragment must have at least one atom."
            )
        }
    }

    /**
     *  Copy constructor.
     *
     *  @param other
     *      Fragment to copy.
     *
     *  @param deep
     *      Whether atoms are copied.
     */
    constructor(
        other: AbstractFragment<A>,
        deep: Boolean
    ): super(other, deep)

    /**
     *  Delegated deserialization constructor.
     */
    private constructor(
        avroRecord: GenericRecord,
        atomDeserializer: (ByteBuffer) -> A
    ): this(
        @Suppress("UNCHECKED_CAST") (
            avroRecord.get("atoms") as List<ByteBuffer>
        ).map {
            atomDeserializer.invoke(it)
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
            AbstractFragmentAvsc.schema,
            avroData
        ).first(),
        atomDeserializer
    )

    override fun containsAtom(atom: A): Boolean =
        atoms().contains(atom)

    companion object {
        /**
         *  Serializes an [AbstractFragment] in Apache Avro.
         *
         *  @param obj
         *      [AbstractFragment] to serialize.
         *
         *  @param atomSerializer
         *      [Atom] serializer.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun <A : Atom> serialize(
            obj: AbstractFragment<A>,
            atomSerializer: (A) -> ByteBuffer
        ): ByteBuffer
        {
            val avroRecord = GenericData.Record(
                AbstractFragmentAvsc.schema
            )

            avroRecord.put(
                "atoms",
                obj.atoms().map {
                    atomSerializer.invoke(it)
                }
            )

            return AvroSimple.serializeData<GenericRecord>(
                AbstractFragmentAvsc.schema,
                listOf(avroRecord)
            )
        }
    }
}
