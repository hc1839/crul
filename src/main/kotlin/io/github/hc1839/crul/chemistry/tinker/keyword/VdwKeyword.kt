package io.github.hc1839.crul.chemistry.tinker.keyword

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.measure.Quantity
import io.github.hc1839.crul.measure.dimension.Dimension
import io.github.hc1839.crul.measure.unit.SiUnitSystem
import io.github.hc1839.crul.measure.unit.UnitOfMeasure
import io.github.hc1839.crul.measure.unit.UnitSystem
import io.github.hc1839.crul.serialize.AvroSimple
import io.github.hc1839.crul.chemistry.tinker.AtomClass

/**
 *  Avro-schema information for a `vdw` keyword.
 */
private object VdwKeywordAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/io/github/hc1839/crul/chemistry/tinker/keyword/VdwKeyword.avsc"

    /**
     *  Avro schema for the serialization of [VdwKeyword].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Units and dimensions for the vdW keyword.
 */
internal object VdwKeywordUnits {
    /**
     *  Angstrom.
     */
    val angstromUnit: UnitOfMeasure =
        UnitOfMeasure.parse("Ao")

    /**
     *  kcal/mol.
     */
    val kcalPerMolUnit: UnitOfMeasure =
        UnitOfMeasure.parse("kcal/mol")

    /**
     *  Dimension of Rmin.
     */
    val rminDim: Dimension =
        Dimension.parse("L")

    /**
     *  Dimension of potential well depth.
     */
    val epsilonDim: Dimension =
        Dimension.parse("M.L2.T-2.N-1")
}

/**
 *  Definition of vdW parameters for an atom class.
 *
 *  It corresponds to the Tinker keyword, `vdw`.
 *
 *  @property atomClass
 *      Atom class.
 *
 *  @property rmin
 *      Rmin of the atom class (in dimension of `L`). It must be positive.
 *
 *  @property epsilon
 *      Potential well depth of the atom class (in dimension of
 *      `M.L2.T-2.N-1`). It must be positive.
 *
 *  @property beta
 *      Reduction factor (beta) of the atom class (dimensionless), or `null` if
 *      the atom class is not univalent. If not `null`, it must be between
 *      `0.0` and `1.0`.
 *
 *  @constructor
 */
data class VdwKeyword(
    val atomClass: AtomClass,
    val rmin: Double,
    val epsilon: Double,
    val beta: Double?
) : Keyword
{
    init {
        if (!(rmin > 0.0)) {
            throw IllegalArgumentException(
                "Rmin is not positive: $rmin"
            )
        }

        if (!(epsilon > 0.0)) {
            throw IllegalArgumentException(
                "Epsilon is not positive: $epsilon"
            )
        }

        if (beta != null && (beta < 0.0 || beta > 1.0)) {
            throw IllegalArgumentException(
                "Beta is not between 0.0 and 1.0: $beta"
            )
        }
    }

    /**
     *  @param args
     *      1) [UnitSystem]. Unit system that the values are in.
     */
    override fun toInputText(vararg args: Any?, separator: String): String {
        val srcUnitSystem = args[0] as UnitSystem

        return listOf(
            "vdw",
            atomClass.code.toString(),
            Quantity.convertUnit(
                rmin,
                VdwKeywordUnits.rminDim,
                srcUnitSystem,
                VdwKeywordUnits.angstromUnit
            ).toString(),
            Quantity.convertUnit(
                epsilon,
                VdwKeywordUnits.epsilonDim,
                srcUnitSystem,
                VdwKeywordUnits.kcalPerMolUnit
            ).toString(),
            beta?.toString()
        )
        .filterNotNull()
        .joinToString(separator)
    }

    companion object {
        /**
         *  Serializes a [VdwKeyword] in Apache Avro.
         *
         *  @param obj
         *      [VdwKeyword] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: VdwKeyword): ByteBuffer {
            val avroRecord = GenericData.Record(
                VdwKeywordAvsc.schema
            )

            avroRecord.put(
                "atom_class",
                AtomClass.serialize(obj.atomClass)
            )

            avroRecord.put("rmin", obj.rmin)
            avroRecord.put("epsilon", obj.epsilon)
            avroRecord.put("beta", obj.beta)

            return AvroSimple.serializeData<GenericRecord>(
                VdwKeywordAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes a [VdwKeyword] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [VdwKeyword] as returned by [serialize].
         *
         *  @return
         *      Deserialized [VdwKeyword].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): VdwKeyword {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                VdwKeywordAvsc.schema,
                avroData
            ).first()

            val atomClass = AtomClass.deserialize(
                avroRecord.get("atom_class") as ByteBuffer
            )

            val rmin = avroRecord.get("rmin") as Double
            val epsilon = avroRecord.get("epsilon") as Double
            val beta = avroRecord.get("beta") as Double?

            return VdwKeyword(
                atomClass = atomClass,
                rmin = rmin,
                epsilon = epsilon,
                beta = beta
            )
        }
    }
}
