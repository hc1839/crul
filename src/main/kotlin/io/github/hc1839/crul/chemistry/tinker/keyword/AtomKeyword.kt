package io.github.hc1839.crul.chemistry.tinker.keyword

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.chemistry.species.Element
import io.github.hc1839.crul.serialize.AvroSimple
import io.github.hc1839.crul.chemistry.tinker.AtomClass

/**
 *  Avro-schema information for an `atom` keyword.
 */
private object AtomKeywordAvsc {
    /**
     *  Absolute path to the Avro schema file with respect to the JAR.
     */
    val path: String =
        "/io/github/hc1839/crul/chemistry/tinker/keyword/AtomKeyword.avsc"

    /**
     *  Avro schema for the serialization of [AtomKeyword].
     */
    val schema: Schema = Schema.Parser().parse(
        this::class.java.getResourceAsStream(path)
    )
}

/**
 *  Definition of an atom type for a parameter set.
 *
 *  It corresponds to the Tinker keyword, `atom`.
 *
 *  @property atomTypeCode
 *      Integer code of the atom type.
 *
 *  @property atomClass
 *      Atom class.
 *
 *  @property typeName
 *      Name of the atom type. It is arbitrary, but if the atom type is defined
 *      by Tinker, it is by convention the same as specified in the parameter
 *      file provided by Tinker. It must have a non-zero length.
 *
 *  @property description
 *      Description of the atom type for a human. It must have a non-zero
 *      length.
 *
 *  @property atomicNumber
 *      Atomic number of the atom represented by the atom type.
 *
 *  @property bondCount
 *      Number of bonds that the atom represented by the atom type participates
 *      in. It must be non-negative.
 *
 *  @constructor
 */
data class AtomKeyword(
    val atomTypeCode: Int,
    val atomClass: AtomClass,
    val typeName: String,
    val description: String,
    val atomicNumber: Int,
    val bondCount: Int
) : Keyword
{
    init {
        if (typeName.length == 0) {
            throw IllegalArgumentException(
                "Name has zero length."
            )
        }

        if (description.length == 0) {
            throw IllegalArgumentException(
                "Description has zero length."
            )
        }

        // Trigger an exception if the atomic number is invalid.
        Element.getSymbolByNumber(atomicNumber)

        if (bondCount < 0) {
            throw IllegalArgumentException(
                "Bond count is negative."
            )
        }
    }

    /**
     *  @param args
     *      Ignored.
     */
    override fun toInputText(vararg args: Any?, separator: String): String =
        listOf(
            "atom",
            atomTypeCode.toString(),
            atomClass.code.toString(),
            typeName,
            "\"$description\"",
            atomicNumber.toString(),
            Element(atomicNumber).weight.toString(),
            bondCount
        )
        .joinToString(separator)

    companion object {
        /**
         *  Serializes an [AtomKeyword] in Apache Avro.
         *
         *  @param obj
         *      [AtomKeyword] to serialize.
         *
         *  @return
         *      Avro serialization of `obj`.
         */
        @JvmStatic
        fun serialize(obj: AtomKeyword): ByteBuffer {
            val avroRecord = GenericData.Record(
                AtomKeywordAvsc.schema
            )

            avroRecord.put("atom_type_code", obj.atomTypeCode)

            avroRecord.put(
                "atom_class",
                AtomClass.serialize(obj.atomClass)
            )

            avroRecord.put("type_name", obj.typeName)
            avroRecord.put("description", obj.description)
            avroRecord.put("atomic_number", obj.atomicNumber)
            avroRecord.put("bond_count", obj.bondCount)

            return AvroSimple.serializeData<GenericRecord>(
                AtomKeywordAvsc.schema,
                listOf(avroRecord)
            )
        }

        /**
         *  Deserializes an [AtomKeyword] in Apache Avro.
         *
         *  @param avroData
         *      Serialized [AtomKeyword] as returned by [serialize].
         *
         *  @return
         *      Deserialized [AtomKeyword].
         */
        @JvmStatic
        fun deserialize(avroData: ByteBuffer): AtomKeyword {
            val avroRecord = AvroSimple.deserializeData<GenericRecord>(
                AtomKeywordAvsc.schema,
                avroData
            ).first()

            val atomTypeCode = avroRecord.get("atom_type_code") as Int

            val atomClass = AtomClass.deserialize(
                avroRecord.get("atom_class") as ByteBuffer
            )

            val typeName = avroRecord.get("type_name").toString()
            val description = avroRecord.get("description").toString()
            val atomicNumber = avroRecord.get("atomic_number") as Int
            val bondCount = avroRecord.get("bond_count") as Int

            return AtomKeyword(
                atomTypeCode = atomTypeCode,
                atomClass = atomClass,
                typeName = typeName,
                description = description,
                atomicNumber = atomicNumber,
                bondCount = bondCount
            )
        }
    }
}
