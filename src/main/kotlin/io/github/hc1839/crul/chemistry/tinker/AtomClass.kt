package io.github.hc1839.crul.chemistry.tinker

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.serialize.AvroSimple

private object AtomClassAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/io/github/hc1839/crul/chemistry/tinker/AtomClass.avsc"

    /**
     *  Avro schema for the serialization of [AtomClass].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Atom class as in Tinker.
 *
 *  @property code
 *      Code of the atom class as a non-negative integer.
 *
 *  @constructor
 */
data class AtomClass(
    val code: Int
) {
    init {
        if (code < 0) {
            throw IllegalArgumentException(
                "Integer code is negative."
            )
        }
    }

    companion object {
        /**
         *  Serializes an [AtomClass] in Apache Avro.
         *
         *  @param obj
         *      [AtomClass] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: AtomClass): ByteBuffer {
            val avroRecord = GenericData.Record(
                AtomClassAvsc.schema
            )

            avroRecord.put("code", obj.code)

            return AvroSimple.serializeData<GenericRecord>(
                AtomClassAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes an [AtomClass] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [AtomClass] as returned by [serialize].
         *
         *  @return
         *      Deserialized [AtomClass].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): AtomClass {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                AtomClassAvsc.schema,
                avroData
            ).first()

            return AtomClass(
                code = avroRecord.get("code") as Int
            )
        }
    }
}
