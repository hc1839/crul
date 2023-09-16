package io.github.hc1839.crul.chemistry.tinker.force.vdw

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.serialize.AvroSimple
import io.github.hc1839.crul.chemistry.tinker.AtomClass

private object VdwParamGroupAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/io/github/hc1839/crul/chemistry/tinker/force/vdw/VdwParamGroup.avsc"

    /**
     *  Avro schema for the serialization of [VdwParamGroup].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  VdW-parameter group of an atom class.
 *
 *  [rmin], [epsilon], and [beta] are allowed to store arbitrary values, since
 *  their associated parameters may be validated or unvalidated depending on
 *  the application.
 *
 *  @property atomClass
 *      Atom class of the vdW parameters.
 *
 *  @property rmin
 *      Value of (or associated with) the Rmin parameter.
 *
 *  @property epsilon
 *      Value of (or associated with) the epsilon parameter.
 *
 *  @property beta
 *      Value of (or associated with) the beta parameter, or `null` if not
 *      applicable.
 *
 *  @constructor
 */
data class VdwParamGroup(
    val atomClass: AtomClass,
    val rmin: Double,
    val epsilon: Double,
    val beta: Double?
) {
    companion object {
        /**
         *  Serializes a [VdwParamGroup] in Apache Avro.
         *
         *  @param obj
         *      [VdwParamGroup] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: VdwParamGroup): ByteBuffer {
            val avroRecord = GenericData.Record(
                VdwParamGroupAvsc.schema
            )

            avroRecord.put(
                "atom_class",
                AtomClass.serialize(obj.atomClass)
            )

            avroRecord.put("rmin", obj.rmin)
            avroRecord.put("epsilon", obj.epsilon)
            avroRecord.put("beta", obj.beta)

            return AvroSimple.serializeData<GenericRecord>(
                VdwParamGroupAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [VdwParamGroup] in Apache Avro.
         *
         *  @param obj
         *      Serialized [VdwParamGroup] as returned by [serialize].
         *
         *  @return
         *      Deserialized [VdwParamGroup].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): VdwParamGroup {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                VdwParamGroupAvsc.schema,
                avroData
            ).first()

            val atomClass = AtomClass.deserialize(
                avroRecord.get("atom_class") as ByteBuffer
            )

            val rmin = avroRecord.get("rmin") as Double
            val epsilon = avroRecord.get("epsilon") as Double
            val beta = avroRecord.get("beta") as Double?

            return VdwParamGroup(
                atomClass = atomClass,
                rmin = rmin,
                epsilon = epsilon,
                beta = beta
            )
        }
    }
}
