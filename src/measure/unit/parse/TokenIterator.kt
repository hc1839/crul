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

import hierarchy.tree.Node
import measure.unit.UnitOfMeasure
import measure.unit.UnitPrefix

/**
 *  Iterator of tokens as nodes of a UCUM unit.
 *
 *  The possible node types produced by this iterator are
 *      - [Production.ID]
 *      - [Production.TERMINAL]
 *
 *  All tokens have user data as `String` associated by
 *  [Production.userDataKey].
 */
class TokenIterator : AbstractIterator<Node<Production>> {
    /**
     *  Complete input string representing a UCUM unit.
     */
    val input: String

    /**
     *  Unscanned text of the UCUM unit.
     */
    private var stream: String

    /**
     *  @param input
     *      Input string representing a UCUM unit. It cannot contain
     *      intervening newlines.
     */
    constructor(input: String) {
        this.input = input.trim()

        if (Regex("\\n") in this.input) {
            throw IllegalArgumentException(
                "Input string cannot contain intervening newlines."
            )
        }

        this.stream = this.input
    }

    override fun computeNext() {
        if (stream == "") {
            // Text is completely scanned.
            done()
            return
        }

        // Construct the next token as a node for the parse tree.
        val nextToken = when {
            idRegex in stream -> {
                val matchResult = idRegex.find(stream)!!

                val node = Node(Production.ID)

                // Determine whether it is a prefix preceding a metric unit.
                if (
                    UnitPrefix.isPrefix(
                        matchResult.value.first().toString()
                    ) &&
                    UnitOfMeasure.isMetric(matchResult.value.drop(1))
                ) {
                    node.setUserData(
                        Production.userDataKey,
                        matchResult.value.first().toString()
                    )
                    stream = stream.drop(1)
                } else {
                    node.setUserData(
                        Production.userDataKey,
                        matchResult.value
                    )
                    stream = stream.drop(matchResult.value.length)
                }

                node
            }

            terminalRegex in stream -> {
                val matchResult = terminalRegex.find(stream)!!

                val node = Node(Production.TERMINAL)
                node.setUserData(Production.userDataKey, matchResult.value)

                stream = stream.drop(matchResult.value.length)

                node
            }

            else -> {
                // Construct a string with a pointer to the location of the
                // error.

                val errorPosition =  input.length - stream.length

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

        stream = stream.trim()

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
