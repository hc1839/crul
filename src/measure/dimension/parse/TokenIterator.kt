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

package measure.dimension.parse

import parse.AbstractTokenIterator
import parse.ParseNode

/**
 *  Iterator of tokens of an ISQ dimension.
 *
 *  The possible node types produced by this iterator are
 *      - [Production.ID]
 *      - [Production.TERMINAL]
 *
 *  All tokens have user data as `String` associated by
 *  [Production.userDataKey].
 */
class TokenIterator : AbstractTokenIterator<Production> {
    /**
     *  Complete input string.
     */
    private val input: String

    /**
     *  @param input
     *      Input string representing a UCUM unit. It cannot contain
     *      intervening newlines.
     */
    constructor(input: String): super(listOf(input.trim()).iterator()) {
        this.input = input.trim()

        if (Regex("\\n") in this.input) {
            throw IllegalArgumentException(
                "Input string cannot contain intervening newlines."
            )
        }
    }

    override fun computeNext() {
        val currString = shift()

        if (currString == null) {
            // Text is completely scanned.
            done()
            return
        }

        // String that will be unshifted if not empty.
        val nextString: String

        // If a base dimension, distinguish thermodynamic temperature from the
        // others by substring length.
        val baseDimLength = listOf(1, 2)
            .filter { substrLength ->
                if (substrLength <= currString.length) {
                    Production
                        .BASE_DIMENSION
                        .matchesFirst(
                            currString.substring(0, substrLength)
                        )
                } else {
                    false
                }
            }
            .singleOrNull()

        // Construct the next token as a node for the parse tree.
        val nextToken = when (baseDimLength) {
            null -> {
                val node = ParseNode(Production.TERMINAL)

                node.setUserData(
                    Production.userDataKey,
                    currString.first().toString(),
                    null
                )

                nextString = currString.drop(1).trim()

                node
            }

            else -> {
                val node = ParseNode(Production.ID)

                node.setUserData(
                    Production.userDataKey,
                    currString.substring(0, baseDimLength),
                    null
                )

                nextString = currString.drop(baseDimLength).trim()

                node
            }
        }

        if (nextString != "") {
            unshift(nextString)
        }

        setNext(nextToken)

        return
    }
}
