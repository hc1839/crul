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

package crul.hypergraph.indexer

import crul.hypergraph.base.*

/**
 *  Redirection for merged constructs.
 *
 *  This class is mainly used for constructs that have been merged to refer to
 *  the same terminal (underlying) construct.
 *
 *  @constructor
 */
class ConstructRedirector<T : Construct>() {
    /**
     *  Constructs associated by their IDs.
     */
    private val constructsById: MutableMap<String, T> = HashMap()

    /**
     *  Construct instances associated with their terminal constructs.
     */
    private val redirections: MutableMap<String, String> = HashMap()

    /**
     *  IDs of terminal constructs.
     */
    fun terminalIds(): List<String> =
        constructsById.keys.toList()

    /**
     *  Gets a terminal construct by a construct ID.
     *
     *  The construct ID can be terminal or non-terminal.
     */
    operator fun get(constructId: String): T? {
        val terminalId = getTerminalId(constructId)

        if (terminalId == "") {
            return null
        } else {
            return constructsById[terminalId]
        }
    }

    /**
     *  Puts a construct to the redirector.
     */
    fun put(constructId: String, construct: T) {
        if (!crul.xml.Datatype.isNCName(constructId)) {
            throw IllegalArgumentException(
                "Construct ID does not conform to XML NCName production: " +
                constructId
            )
        }

        constructsById.put(constructId, construct)
        redirections.put(constructId, "")
    }

    /**
     *  Gets the terminal construct ID given a construct ID.
     */
    fun getTerminalId(constructId: String) =
        if (redirections.containsKey(constructId)) {
            var id = constructId

            while (true) {
                val constructIdRedirect = redirections[id]!!

                if (constructIdRedirect == "") {
                    break
                } else {
                    id = constructIdRedirect
                }
            }

            id
        } else {
            ""
        }

    /**
     *  Redirects a non-terminal construct ID to a terminal construct ID.
     */
    fun redirect(fromId: String, terminalId: String) {
        if (!redirections.containsKey(fromId)) {
            throw IllegalArgumentException(
                "From ID does not exist: $fromId"
            )
        }

        if (!constructsById.containsKey(terminalId)) {
            throw IllegalArgumentException(
                "Terminal ID does not directly refer a construct: $terminalId"
            )
        }

        if (terminalId == fromId) {
            throw IllegalArgumentException(
                "From and terminal IDs are the same: $fromId"
            )
        }

        redirections.put(fromId, terminalId)
        constructsById.remove(fromId)
    }

    /**
     *  Removes a construct from the redirector.
     *
     *  The construct ID can be terminal or non-terminal.
     */
    fun remove(constructId: String) {
        val terminalId = getTerminalId(constructId)

        if (terminalId == "") {
            return
        }

        var idsToRemove = listOf(terminalId)

        while (!idsToRemove.isEmpty()) {
            for (id in idsToRemove) {
                redirections.remove(id)
                constructsById.remove(id)
            }

            idsToRemove = redirections.map { (fromId, toId) ->
                if (toId in idsToRemove) {
                    fromId
                } else {
                    null
                }
            }.filterNotNull()
        }
    }

    /**
     *  Removes all constructs from the redirector.
     */
    fun clear() {
        constructsById.clear()
        redirections.clear()
    }
}
