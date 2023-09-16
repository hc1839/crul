package io.github.hc1839.crul.chemistry.tinker.force.vdw

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.serialize.AvroSimple
import io.github.hc1839.crul.chemistry.tinker.AtomClass

private object VdwParamMapperComboAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/io/github/hc1839/crul/chemistry/tinker/force/vdw/VdwParamMapperCombo.avsc"

    /**
     *  Avro schema for the serialization of [VdwParamMapperCombo].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  VdW-parameter mapper that uses combination rules.
 *
 *  @param R
 *      Type of combination rule for Rmin.
 *
 *  @param E
 *      Type of combination rule for epsilon.
 */
interface VdwParamMapperCombo<out R, out E> : VdwParamMapper
    where R : ComboRule,
          E : ComboRule
{
    /**
     *  Combination rule for Rmin.
     */
    val rminComboRule: R

    /**
     *  Combination rule for epsilon.
     */
    val epsilonComboRule: E

    companion object {
        /**
         *  Constructs a [VdwParamMapperCombo].
         *
         *  @param rminComboRule
         *      Combination rule for Rmin.
         *
         *  @param epsilonComboRule
         *      Combination rule for epsilon.
         *
         *  @param vdwParamGroups
         *      Like-pair vdW parameters. If an atom class occurs more than
         *      once, an exception is raised.
         *
         *  @return
         *      New instance of [VdwParamMapperCombo].
         */
        @JvmStatic
        fun <R, E> newInstance(
            rminComboRule: R,
            epsilonComboRule: E,
            vdwParamGroups: Collection<VdwParamGroup>
        ): VdwParamMapperCombo<R, E>
            where R : ComboRule,
                  E : ComboRule
        {
            return VdwParamMapperCombo(
                rminComboRule,
                epsilonComboRule,
                vdwParamGroups
            )
        }

        /**
         *  Serializes a [VdwParamMapperCombo] in Apache Avro.
         *
         *  Combination rules are not part of the serialization.
         *
         *  @param obj
         *      [VdwParamMapperCombo] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: VdwParamMapperCombo<*, *>): ByteBuffer {
            val avroRecord = GenericData.Record(
                VdwParamMapperComboAvsc.schema
            )

            avroRecord.put(
                "vdw_param_groups",
                obj.atomClasses.map { atomClass ->
                    VdwParamGroup.serialize(
                        obj.getParamGroup(atomClass)
                    )
                }
            )

            return AvroSimple.serializeData<GenericRecord>(
                VdwParamMapperComboAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [VdwParamMapperCombo] in Apache Avro.
         *
         *  Since combination rules are not part of the serialization, they
         *  must be supplied as part of the deserialization.
         *
         *  @param avroData
         *      Serialized [VdwParamMapperCombo] as returned by [serialize].
         *
         *  @param rminComboRule
         *      Combination rule for Rmin.
         *
         *  @param epsilonComboRule
         *      Combination rule for epsilon.
         *
         *  @return
         *      Deserialized [VdwParamMapperCombo].
         */
        @JvmStatic
        fun <R, E> deserialize(
            avroData: ByteBuffer,
            rminComboRule: R,
            epsilonComboRule: E
        ): VdwParamMapperCombo<R, E>
            where R : ComboRule,
                  E : ComboRule
        {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                VdwParamMapperComboAvsc.schema,
                avroData
            ).first()

            val vdwParamGroups =
                @Suppress("UNCHECKED_CAST") (
                    avroRecord.get("vdw_param_groups") as
                        List<ByteBuffer>
                )
                .map { VdwParamGroup.deserialize(it) }
                .toSet()

            return VdwParamMapperCombo(
                rminComboRule,
                epsilonComboRule,
                vdwParamGroups
            )
        }
    }
}

/**
 *  Constructs a new instance of [VdwParamMapperCombo].
 *
 *  See [VdwParamMapperCombo.newInstance] for description.
 */
fun <R, E> VdwParamMapperCombo(
    rminComboRule: R,
    epsilonComboRule: E,
    vdwParamGroups: Collection<VdwParamGroup>
): VdwParamMapperCombo<R, E>
    where R : ComboRule,
          E : ComboRule
{
    return VdwParamMapperComboImpl(
        rminComboRule,
        epsilonComboRule,
        vdwParamGroups
    )
}
