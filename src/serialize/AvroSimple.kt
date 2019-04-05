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

package crul.serialize

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.apache.avro.Schema
import org.apache.avro.file.DataFileStream
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter

/**
 *  Convenience functions for working with serialization of objects in Apache
 *  Avro.
 */
object AvroSimple {
    /**
     *  Parses an Avro schema file stored in the JAR file.
     *
     *  @param path
     *      Absolute path with respect to the JAR of the schema file.
     *
     *  @return
     *      Avro schema.
     */
    @JvmStatic
    fun getSchema(path: String): Schema =
        Schema.Parser().parse(
            this::class.java.getResourceAsStream(path)
        )

    /**
     *  Serializes Avro data in binary format.
     *
     *  @param D
     *      Type of each Avro datum (e.g., `GenericRecord`).
     *
     *  @param schema
     *      Avro schema.
     *
     *  @param datumList
     *      Avro data to serialize.
     *
     *  @return
     *      Serialized Avro data as a byte array.
     */
    @JvmStatic
    fun <D> serializeData(schema: Schema, datumList: List<D>): ByteArray {
        val datumWriter = GenericDatumWriter<D>(schema)
        val dataFileWriter = DataFileWriter(datumWriter)
        val outputStream = ByteArrayOutputStream()

        dataFileWriter.create(schema, outputStream)

        for (datum in datumList) {
            dataFileWriter.append(datum)
        }

        dataFileWriter.close()

        return outputStream.toByteArray()
    }

    /**
     *  Deserializes Avro data in binary format.
     *
     *  @param D
     *      Type of each Avro datum (e.g., `GenericRecord`).
     *
     *  @param schema
     *      Avro schema.
     *
     *  @param avroData
     *      Avro data to deserialize.
     *
     *  @return
     *      Deserialized Avro data as a list of `D`.
     */
    @JvmStatic
    fun <D> deserializeData(schema: Schema, avroData: ByteArray): List<D> {
        val datumReader = GenericDatumReader<D>(schema)
        val inputStream = ByteArrayInputStream(avroData)
        val dataStream = DataFileStream(inputStream, datumReader)

        val datumList: MutableList<D> = mutableListOf()

        while (dataStream.hasNext()) {
            datumList.add(
                dataStream.next(datumList.lastOrNull())
            )
        }

        return datumList.toList()
    }
}
