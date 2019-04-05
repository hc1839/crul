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

import org.msgpack.core.MessagePack
import org.msgpack.value.impl.ImmutableArrayValueImpl
import org.msgpack.value.impl.ImmutableStringValueImpl

/**
 *  Convenience functions for working with serialization of objects in
 *  MessagePack.
 */
object MessagePackConv {
    /**
     *  Gets the map associated with a key in a MessagePack map.
     *
     *  @param msgpackMap
     *      MessagePack map of string to another map, which in turn is an
     *      association of string with a MessagePack value. In essence, it is a
     *      map that contains the serializations of the objects in an
     *      inheritance tree.
     *
     *  @param key
     *      Key that is associated with the map within `msgpackMap` to
     *      retrieve. If it does not exist, an exception is raised.
     *
     *  @return
     *      Map within `msgpackMap` that is associated by `key`.
     */
    @JvmStatic
    fun getInnerMap(msgpackMap: ByteArray, key: String):
        Map<String, org.msgpack.value.Value>
    {
        val unpacker = MessagePack.newDefaultUnpacker(msgpackMap)

        val outerMap = unpacker
            .unpackValue()
            .asMapValue()
            .map()
            .mapKeys { (keyValue, _) ->
                keyValue.asStringValue().toString()
            }

        unpacker.close()

        val innerMap = outerMap[key]!!
            .asMapValue()
            .map()
            .mapKeys { (keyValue, _) ->
                keyValue.asStringValue().toString()
            }

        return innerMap
    }

    /**
     *  Creates a new MessagePack map encoded as a byte array by adding a
     *  key-value entry.
     *
     *  @param msgpackMap
     *      MessagePack map that will have `key` and `addendum` added. It is
     *      not modified.
     *
     *  @param key
     *      Key that is associated with `addendum`.
     *
     *  @param addendum
     *      MessagePack value that is associated by `key`.
     */
    @JvmStatic
    fun addKeyValueEntry(
        msgpackMap: ByteArray,
        key: String,
        addendum: ByteArray
    ): ByteArray
    {
        val mapUnpacker = MessagePack.newDefaultUnpacker(msgpackMap)
        val addendumUnpacker = MessagePack.newDefaultUnpacker(addendum)

        var mapAsList = mapUnpacker
            .unpackValue()
            .asMapValue()
            .keyValueArray
            .toList()

        mapAsList += listOf(
            ImmutableStringValueImpl(key),
            addendumUnpacker.unpackValue()
        )

        mapUnpacker.close()
        addendumUnpacker.close()

        val packer = MessagePack.newDefaultBufferPacker()

        packer.packValue(
            ImmutableArrayValueImpl(
                Array(mapAsList.count()) { mapAsList[it] }
            )
        )

        packer.close()

        return packer.toByteArray()
    }
}
