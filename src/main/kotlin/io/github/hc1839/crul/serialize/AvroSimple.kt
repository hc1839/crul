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

package io.github.hc1839.crul.serialize

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
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
     *  Serializes Avro data to a byte buffer.
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
     *      Avro data serialized to a byte buffer.
     */
    @JvmStatic
    fun <D> serializeData(schema: Schema, datumList: List<D>): ByteBuffer {
        val datumWriter = GenericDatumWriter<D>(schema)
        val dataFileWriter = DataFileWriter(datumWriter)
        val outputStream = ByteArrayOutputStream()

        dataFileWriter.create(schema, outputStream)

        for (datum in datumList) {
            dataFileWriter.append(datum)
        }

        dataFileWriter.close()

        return ByteBuffer.wrap(outputStream.toByteArray())
    }

    /**
     *  Deserializes Avro data from a byte buffer.
     *
     *  @param D
     *      Type of each Avro datum (e.g., `GenericRecord`).
     *
     *  @param schema
     *      Avro schema.
     *
     *  @param avroData
     *      Byte buffer of the Avro data to deserialize.
     *
     *  @return
     *      Deserialized Avro data as a list of `D`.
     */
    @JvmStatic
    fun <D> deserializeData(schema: Schema, avroData: ByteBuffer): List<D> {
        val datumReader = GenericDatumReader<D>(schema)
        val inputStream = ByteArrayInputStream(
            ByteArray(avroData.limit() - avroData.position()) {
                avroData.get()
            }
        )
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
