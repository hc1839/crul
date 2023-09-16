package io.github.hc1839.crul.chemistry.tinker

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.serialize.AvroSimple

private object AtomClassPairAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/io/github/hc1839/crul/chemistry/tinker/AtomClassPair.avsc"

    /**
     *  Avro schema for the serialization of [AtomClassPair].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Pair of [AtomClass] instances.
 *
 *  @property first
 *      First atom class.
 *
 *  @property second
 *      Second atom class.
 *
 *  @property orderSignificant
 *      Whether the order of the atom classes is significant when calculating
 *      hash code and comparing for equality.
 *
 *  @constructor
 */
open class AtomClassPair(
    val first: AtomClass,
    val second: AtomClass,
    val orderSignificant: Boolean
) {
    override fun hashCode(): Int =
        if (orderSignificant) {
            listOf(first, second).hashCode()
        } else {
            setOf(first, second).hashCode()
        }

    override fun equals(other: Any?): Boolean =
        other is AtomClassPair &&
        this::class == other::class &&
        if (orderSignificant) {
            toList() == other.toList()
        } else {
            toList().toSet() == other.toList().toSet()
        }

    /**
     *  Whether the two atom classes are the same.
     */
    val isLike: Boolean = first == second

    /**
     *  Returns the atom class if the atom classes are equal, or `null` if they
     *  are not.
     */
    fun singleOrNull(): AtomClass? =
        if (isLike) {
            first
        } else {
            null
        }

    /**
     *  Returns the atom class if the atom classes are equal, or throws an
     *  exception if they are not.
     */
    fun single(): AtomClass =
        singleOrNull() ?: throw RuntimeException(
            "Atom classes are not equal."
        )

    /**
     *  Converts this pair to a list.
     *
     *  Atom classes maintain their order in the returned list regardless of
     *  [orderSignificant].
     */
    fun toList(): List<AtomClass> =
        listOf(first, second)

    companion object {
        /**
         *  Serializes an [AtomClassPair] in Apache Avro.
         *
         *  @param obj
         *      [AtomClassPair] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: AtomClassPair): ByteBuffer {
            val avroRecord = GenericData.Record(
                AtomClassPairAvsc.schema
            )

            avroRecord.put(
                "atom_class_1",
                AtomClass.serialize(obj.first)
            )

            avroRecord.put(
                "atom_class_2",
                AtomClass.serialize(obj.second)
            )

            avroRecord.put(
                "order_significant",
                obj.orderSignificant
            )

            return AvroSimple.serializeData<GenericRecord>(
                AtomClassPairAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes an [AtomClassPair] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [AtomClassPair] as returned by [serialize].
         *
         *  @return
         *      Deserialized [AtomClassPair].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): AtomClassPair {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                AtomClassPairAvsc.schema,
                avroData
            ).first()

            val atomClass1 = AtomClass.deserialize(
                avroRecord.get("atom_class_1") as ByteBuffer
            )

            val atomClass2 = AtomClass.deserialize(
                avroRecord.get("atom_class_2") as ByteBuffer
            )

            val orderSignificant =
                avroRecord.get("order_significant") as Boolean

            return AtomClassPair(atomClass1, atomClass2, orderSignificant)
        }
    }
}
