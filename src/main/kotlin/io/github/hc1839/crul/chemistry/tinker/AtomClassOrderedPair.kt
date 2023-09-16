package io.github.hc1839.crul.chemistry.tinker

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.serialize.AvroSimple

/**
 *  Pair of [AtomClass] instances where the order of the atom classes is
 *  significant when calculating hash code and comparing for equality.
 *
 *  @property first
 *      First atom class.
 *
 *  @property second
 *      Second atom class.
 *
 *  @constructor
 */
class AtomClassOrderedPair(
    first: AtomClass,
    second: AtomClass
) : AtomClassPair(first, second, true)
{
    companion object {
        /**
         *  Serializes an [AtomClassOrderedPair] in Apache Avro.
         *
         *  @param obj
         *      [AtomClassOrderedPair] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: AtomClassOrderedPair): ByteBuffer =
            AtomClassPair.serialize(obj)

        /**
         *  Deserializes an [AtomClassOrderedPair] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [AtomClassOrderedPair] as returned by [serialize].
         *
         *  @return
         *      Deserialized [AtomClassOrderedPair].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): AtomClassOrderedPair {
            val atomClassPair = AtomClassPair.deserialize(avroData)

            if (!atomClassPair.orderSignificant) {
                throw RuntimeException(
                    "Serialized atom-class pair is not order significant."
                )
            }

            return AtomClassOrderedPair(
                atomClassPair.first,
                atomClassPair.second
            )
        }
    }
}
