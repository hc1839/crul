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

package parse.shiftreduce

import hierarchy.tree.Node

/**
 *  Matcher for the rightmost node(s) in a parse stack and the lookahead symbol
 *  against a production pattern.
 *
 *  @param T
 *      Enum type of the node types in the parse tree.
 */
class ProductionMatcher<T>
    where T : Enum<T>,
          T : Reducer<T>
{
    /**
     *  Non-empty list of node types of the rightmost nodes of a parse stack
     *  that is being matched in the same order.
     */
    val rightmostMatchTypes: List<T>

    /**
     *  Node type that a lookahead symbol is being matched; `null` if any
     *  lookahead symbol (including a `null` one) can be matched.
     */
    val lookaheadMatchType: T?

    /**
     *  @param rightmostMatchTypes
     *      See [rightmostMatchTypes].
     *
     *  @param lookaheadMatchType
     *      See [lookaheadMatchType].
     */
    constructor(rightmostMatchTypes: Iterable<T>, lookaheadMatchType: T?) {
        this.rightmostMatchTypes = rightmostMatchTypes.toList()
        this.lookaheadMatchType = lookaheadMatchType

        if (this.rightmostMatchTypes.isEmpty()) {
            throw IllegalArgumentException(
                "Node types of the rightmost nodes " +
                "of a parse stack to match is empty."
            )
        }
    }

    /**
     *  Whether a combination of a parse stack and a lookahead symbol matches
     *  the production pattern.
     *
     *  @param parseStack
     *      Parse stack to match. The number of rightmost elements that are
     *      being match is the number of elements in [rightmostMatchTypes].
     *
     *  @param lookaheadSymbol
     *      Lookahead symbol to match; `null` if there is none.
     *
     *  @return
     *      `true` if the node types of the rightmost elements of `parseStack`
     *      is equal to the node types specified in [rightmostMatchTypes] in
     *      the same order and `lookaheadSymbol` matches [lookaheadMatchType]
     *      (using [Reducer.matches] if both are not `null`); `false` if
     *      otherwise.  `lookaheadSymbol` always matches if
     *      [lookaheadMatchType] is `null`.  `lookaheadSymbol` never matches if
     *      it is `null` and [lookaheadMatchType] is non-`null`.
     */
    fun matches(parseStack: List<Node<T>>, lookaheadSymbol: String?): Boolean {
        if (parseStack.count() < rightmostMatchTypes.count()) {
            return false
        }

        val parseStackRightmostTypes = parseStack
            .takeLast(rightmostMatchTypes.count())
            .map { it.type }

        if (parseStackRightmostTypes != rightmostMatchTypes) {
            return false
        }

        return lookaheadMatchType == null || (
            lookaheadSymbol != null &&
            lookaheadMatchType.matchesFirst(lookaheadSymbol)
        )
    }
}
