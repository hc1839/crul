package io.github.hc1839.crul.chemistry.tinker

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.serialize.AvroSimple

/**
 *  Pair of [AtomClass] instances where the order of the atom classes is not
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
class AtomClassUnorderedPair(
    first: AtomClass,
    second: AtomClass
) : AtomClassPair(first, second, false)
{
    companion object {
        /**
         *  Serializes an [AtomClassUnorderedPair] in Apache Avro.
         *
         *  @param obj
         *      [AtomClassUnorderedPair] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: AtomClassUnorderedPair): ByteBuffer =
            AtomClassPair.serialize(obj)

        /**
         *  Deserializes an [AtomClassUnorderedPair] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [AtomClassUnorderedPair] as returned by [serialize].
         *
         *  @return
         *      Deserialized [AtomClassUnorderedPair].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): AtomClassUnorderedPair {
            val atomClassPair = AtomClassPair.deserialize(avroData)

            if (atomClassPair.orderSignificant) {
                throw RuntimeException(
                    "Serialized atom-class pair is order significant."
                )
            }

            return AtomClassUnorderedPair(
                atomClassPair.first,
                atomClassPair.second
            )
        }
    }
}
