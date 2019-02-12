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

package measure.unit.parse

import measure.unit.UnitOfMeasure
import measure.unit.UnitPrefix
import parse.AbstractTokenIterator
import parse.ParseNode

/**
 *  Iterator of tokens of a UCUM unit.
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

        // Construct the next token as a node for the parse tree.
        val nextToken = when {
            idRegex in currString -> {
                val matchResult = idRegex.find(currString)!!
                val node = ParseNode(Production.ID)

                // Determine whether it is a prefix preceding a metric unit.
                if (
                    UnitPrefix.isPrefix(
                        matchResult.value.first().toString()
                    ) &&
                    UnitOfMeasure.isMetric(matchResult.value.drop(1))
                ) {
                    node.setUserData(
                        Production.userDataKey,
                        matchResult.value.first().toString(),
                        null
                    )
                    nextString = currString.drop(1).trim()
                } else {
                    node.setUserData(
                        Production.userDataKey,
                        matchResult.value,
                        null
                    )
                    nextString = currString
                        .drop(matchResult.value.length)
                        .trim()
                }

                node
            }

            terminalRegex in currString -> {
                val matchResult = terminalRegex.find(currString)!!

                val node = ParseNode(Production.TERMINAL)
                node.setUserData(
                    Production.userDataKey,
                    matchResult.value,
                    null
                )

                nextString = currString
                    .drop(matchResult.value.length)
                    .trim()

                node
            }

            else -> {
                // Construct a string with a pointer to the location of the
                // error.

                val errorPosition =  input.length - currString.length

                val errorPointerString = (0 until input.length)
                    .map {
                        if (it != errorPosition) {
                            " "
                        } else {
                            "^"
                        }
                    }
                    .joinToString("")

                throw RuntimeException(
                    "Error in tokenization at position $errorPosition:\n\n" +
                    input + "\n" +
                    errorPointerString + "\n\n"
                )
            }
        }

        if (nextString != "") {
            unshift(nextString)
        }

        setNext(nextToken)

        return
    }

    companion object {
        /**
         *  Regular expression for matching an ID.
         */
        private val idRegex: Regex =
            Regex("^[!#-'\\*,0-<>-Z\\\\^-z|~]*[!#-'\\*,:-<>-Z\\\\^-z|~]")

        /**
         *  Regular expression for matching a terminal symbol at the beginning
         *  of the unscanned text.
         */
        private val terminalRegex: Regex =
            Regex("^(\\+|\\-|\\d|\\(|\\)|\\.|/|\\{|\\})")
    }
}
