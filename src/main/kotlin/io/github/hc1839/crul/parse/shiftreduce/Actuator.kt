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

package io.github.hc1839.crul.parse.shiftreduce

import io.github.hc1839.crul.parse.ColorFiller
import io.github.hc1839.crul.parse.ParseNode

/**
 *  Actuator that executes a sequence of shift-reduce steps.
 *
 *  The actuator is constrained to run only once.
 *
 *  @param T
 *      Enum type to use as node type in the parse tree and as the reducer.
 */
class Actuator<T>
    where T : Enum<T>,
          T : Reducer<T>,
          T : ColorFiller<T>
{
    /**
     *  Tokens in the stream as an iterator.
     */
    private val tokens: Iterator<ParseNode<T>>

    /**
     *  Whether this actuator has previously run.
     */
    var hasRun: Boolean = false
        private set

    /**
     *  @param tokens
     *      Tokens in the input stream. For each shift step, the next token is
     *      retrieved and cloned along with the user data. Any descendants are
     *      ignored. The sequence must not be empty.
     */
    constructor(tokens: Iterator<ParseNode<T>>) {
        this.tokens = tokens
    }

    /**
     *  Executes the sequence of shift-reduce steps, and returns the root node
     *  of the parse tree.
     *
     *  For each reduce step, [Reducer.reduce] of the node type of the
     *  rightmost node in the parse stack is called. The returned node is
     *  cloned (along with the user data without descendant nodes) and becomes
     *  the parent of the rightmost node(s) (where the number depends on the
     *  returned integer) in the parse stack, removing the said rightmost
     *  node(s) from parse stack and appending the new parent node.  The new
     *  parent node is sent to the color filler associated with the node type.
     *  The reduction repeats with the successively rightmost node until `null`
     *  is returned.
     *
     *  The parse stack at the end of the sequence of shift-reduce steps must
     *  contain exactly one node, which is the root node of the completed parse
     *  tree. The node type of the root node must also be an accepting state.
     *
     *  If the actuator has previously run, an exception is raised.
     *
     *  @return
     *      Root node of the completed parse tree. It must be a node type that
     *      represents an accepting state.
     */
    fun actuate(): ParseNode<T> {
        if (hasRun) {
            throw RuntimeException(
                "Actuator has previously run."
            )
        } else {
            hasRun = true
        }

        var parseStack: List<ParseNode<T>> = listOf()

        // There must be at least one lookahead symbol.
        if (!tokens.hasNext()) {
            throw IllegalArgumentException(
                "Input stream is empty."
            )
        }

        var lookahead: ParseNode<T>? = tokens.next().cloneNode(false, true)

        // Actuate the sequence of shift-reduce steps.
        do {
            parseStack += lookahead!!

            lookahead = if (tokens.hasNext()) {
                tokens.next().cloneNode(false, true)
            } else {
                null
            }

            // Keep reducing until there are no more reduce steps.
            while (true) {
                val parentChildCountPair = parseStack
                    .last()
                    .type
                    .reduce(parseStack, lookahead)

                if (parentChildCountPair == null) {
                    break
                }

                val parentNode = ParseNode(parentChildCountPair.first)
                val childCount = parentChildCountPair.second

                if (childCount < 1) {
                    throw RuntimeException(
                        "Number of tokens to reduce is not positive."
                    )
                }

                for (childNode in parseStack.takeLast(childCount)) {
                    parentNode.appendChild(childNode)
                }

                parseStack = parseStack.dropLast(childCount) + parentNode
                parentNode.type.fill(parentNode)
            }
        } while (lookahead != null)

        // Completion of parsing must result in one parse tree.
        if (parseStack.count() != 1) {
            throw RuntimeException(
                "Parsing is done, but the parse stack " +
                "does not contain exactly one node."
            )
        }

        // Completion of parsing must result in an accepting state.
        if (!parseStack.single().type.isAccepting) {
            throw RuntimeException(
                "Parsing does not result in an accepting state."
            )
        }

        return parseStack.single()
    }
}
