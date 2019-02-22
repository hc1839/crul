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

@file:JvmMultifileClass

package crul.uuid

/**
 *  Generator for UUIDs.
 */
object Generator {
    /**
     *  Generates a Version 4 UUID that conforms to the XML NCName production.
     */
    @JvmStatic
    fun inNCName(): String {
        var id: String

        do {
            id = java.util.UUID.randomUUID().toString()
        } while (!crul.xml.Datatype.isNCName(id))

        return id
    }
}
