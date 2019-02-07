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
import parse.shiftreduce.ProductionMatcher
import parse.shiftreduce.Reducer

/**
 *  Production rules that are used as node types in a parse tree for the UCUM
 *  unit syntax.
 *
 *  Annotations are not supported. Therefore, curly brackets are not considered
 *  to be valid terminal symbols.
 *
 *  Except some additional productions, the meaning of the enum constant names
 *  follow the BNF as specified in the UCUM and are not documented here.
 *  Additional productions, on the other hand, are documented here.
 */
enum class Production : Reducer<Production> {
    /**
     *  Literal symbol that is not acting as an ID for something.
     */
    TERMINAL {
        override fun matchesLeftmost(token: String): Boolean =
            when (token) {
                // Digits.
                in (0..9).map { it.toString() } -> true

                // Signs.
                "+", "-" -> true

                // Operators.
                ".", "/" -> true

                // Round brackets.
                "(", ")" -> true

                else -> false
            }

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            val tokenSymbol = parseStack
                .last()
                .getUserData(userDataKey)
                as String

            return when {
                DIGIT.matchesLeftmost(tokenSymbol) ->
                    Pair(Node(DIGIT), 1)

                SIGN.matchesLeftmost(tokenSymbol) ->
                    Pair(Node(SIGN), 1)

                MULTIPLY.matchesLeftmost(tokenSymbol) ->
                    Pair(Node(MULTIPLY), 1)

                DIVIDE.matchesLeftmost(tokenSymbol) ->
                    Pair(Node(DIVIDE), 1)

                OPENING_ROUND_BRACKET.matchesLeftmost(tokenSymbol) ->
                    Pair(Node(OPENING_ROUND_BRACKET), 1)

                CLOSING_ROUND_BRACKET.matchesLeftmost(tokenSymbol) ->
                    Pair(Node(CLOSING_ROUND_BRACKET), 1)

                else -> throw RuntimeException(
                    "Not a valid terminal symbol: $tokenSymbol"
                )
            }
        }
    },

    /**
     *  Literal symbol that is acting as an ID for something.
     *
     *  Currently, an ID always gets reduced to [CS_SYMBOL]. If the token does
     *  not represent a known c/s symbol, an exception is raised.
     */
    ID {
        override fun matchesLeftmost(token: String): Boolean =
            Regex("^[a-zA-Z]+$") in token

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            val tokenSymbol = parseStack
                .last()
                .getUserData(userDataKey) as String

            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                PREFIX_SYMBOL.matchesLeftmost(tokenSymbol) &&
                metricUnitLookaheadMatcher.matches(
                    parseStack,
                    lookaheadSymbol
                ) ->
                    Pair(Node(PREFIX_SYMBOL), 1)

                ATOM_SYMBOL_METRIC.matchesLeftmost(tokenSymbol) ->
                    Pair(Node(ATOM_SYMBOL_METRIC), 1)

                ATOM_SYMBOL_NONMETRIC.matchesLeftmost(tokenSymbol) ->
                    Pair(Node(ATOM_SYMBOL_NONMETRIC), 1)

                else -> throw RuntimeException(
                    "Unknown UCUM c/s symbol: $tokenSymbol"
                )
            }
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }

        /**
         *  Production matcher for matching the lookahead as a metric unit.
         */
        private val metricUnitLookaheadMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(listOf(ID), ATOM_SYMBOL_METRIC)
        }
    },

    /**
     *  Opening round bracket.
     */
    OPENING_ROUND_BRACKET {
        override fun matchesLeftmost(token: String): Boolean =
            token == "("

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return null
        }
    },

    /**
     *  Closing round bracket.
     */
    CLOSING_ROUND_BRACKET {
        override fun matchesLeftmost(token: String): Boolean =
            token == ")"

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            if (!componentMatcher.matches(parseStack, null)) {
                throw RuntimeException(
                    "Invalid production for 'COMPONENT' " +
                    "at 'CLOSING_ROUND_BRACKET'."
                )
            }

            return Pair(Node(COMPONENT), 3)
        }

        /**
         *  Production matcher for [COMPONENT] in the context of
         *  [CLOSING_ROUND_BRACKET].
         */
        private val componentMatcher: ProductionMatcher<Production> by lazy {
            ProductionMatcher<Production>(
                listOf(OPENING_ROUND_BRACKET, TERM, CLOSING_ROUND_BRACKET),
                null
            )
        }
    },

    /**
     *  Multiplication operator.
     *
     *  '`.`' is the multiplication operator.
     */
    MULTIPLY {
        override fun matchesLeftmost(token: String): Boolean =
            token == "."

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            if (!termMatcher.matches(parseStack, lookaheadSymbol)) {
                throw RuntimeException(
                    "Multiplication operator is not preceded by a 'TERM' " +
                    "and is not followed by a 'COMPONENT'."
                )
            }

            return null
        }

        /**
         *  Production matcher for [TERM] in the context of [MULTIPLY].
         */
        private val termMatcher: ProductionMatcher<Production> by lazy {
            ProductionMatcher<Production>(listOf(TERM, MULTIPLY), COMPONENT)
        }
    },

    /**
     *  Division operator.
     *
     *  '`/`' is the division operator.
     */
    DIVIDE {
        override fun matchesLeftmost(token: String): Boolean =
            token == "/"

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            if (!termMatcher.matches(parseStack, lookaheadSymbol)) {
                throw RuntimeException(
                    "Division operator is not preceded by a 'TERM' " +
                    "and is not followed by a 'COMPONENT'."
                )
            }

            return null
        }

        /**
         *  Production matcher for [TERM] in the context of [DIVIDE].
         */
        private val termMatcher: ProductionMatcher<Production> by lazy {
            ProductionMatcher<Production>(listOf(TERM, DIVIDE), COMPONENT)
        }
    },

    SIGN {
        override fun matchesLeftmost(token: String): Boolean =
            token in listOf("+", "-")

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return null
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }
    },

    DIGIT {
        override fun matchesLeftmost(token: String): Boolean =
            token in (0..9).map { it.toString() }

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                digitsLookaheadMatcher.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(Node(DIGITS), 1)
            }
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }

        /**
         *  Production matcher for [DIGITS] in the context of [DIGIT] where
         *  the lookahead symbol can match as [DIGITS].
         */
        private val digitsLookaheadMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(listOf(DIGIT), DIGITS)
        }
    },

    DIGITS {
        override fun matchesLeftmost(token: String): Boolean =
            DIGIT.matchesLeftmost(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return when {
                digitsMatcher.matches(parseStack, null) ->
                    Pair(Node(DIGITS), 2)

                precededBySign.matches(parseStack, null) ->
                    Pair(Node(EXPONENT), 2)

                precededBySimpleUnit.matches(parseStack, null) ->
                    Pair(Node(EXPONENT), 1)

                else ->
                    Pair(Node(FACTOR), 1)
            }
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }

        private val digitsMatcher: ProductionMatcher<Production> by lazy {
            ProductionMatcher<Production>(listOf(DIGIT, DIGITS), null)
        }

        /**
         *  Production matcher for [EXPONENT] in the context of [DIGITS]
         *  with a preceding [SIGN].
         */
        private val precededBySign: ProductionMatcher<Production> by lazy {
            ProductionMatcher<Production>(listOf(SIGN, DIGITS), null)
        }

        /**
         *  Production matcher for [EXPONENT] in the context of [DIGITS]
         *  with a preceding [SIMPLE_UNIT].
         */
        private val precededBySimpleUnit: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(listOf(SIMPLE_UNIT, DIGITS), null)
        }
    },

    FACTOR {
        override fun matchesLeftmost(token: String): Boolean =
            DIGITS.matchesLeftmost(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return Pair(Node(COMPONENT), 1)
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }
    },

    EXPONENT {
        override fun matchesLeftmost(token: String): Boolean =
            SIGN.matchesLeftmost(token) ||
            DIGITS.matchesLeftmost(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            if (!annotatableMatcher.matches(parseStack, null)) {
                throw RuntimeException(
                    "Exponent is not preceded by 'SIMPLE_UNIT'."
                )
            }

            return Pair(Node(ANNOTATABLE), 2)
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }

        /**
         *  Production matcher for [ANNOTATABLE] in the context of
         *  [EXPONENT].
         */
        private val annotatableMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(
                listOf(SIMPLE_UNIT, EXPONENT),
                null
            )
        }
    },

    PREFIX_SYMBOL {
        override fun matchesLeftmost(token: String): Boolean =
            UnitPrefix.isPrefix(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return null
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }
    },

    /**
     *  Unit atom that is metric.
     */
    ATOM_SYMBOL_METRIC {
        override fun matchesLeftmost(token: String): Boolean =
            UnitOfMeasure.isMetric(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            val childCount =
                if (precededByPrefix.matches(parseStack, null)) {
                    2
                } else {
                    1
                }

            return Pair(Node(SIMPLE_UNIT), childCount)
        }

        /**
         *  Production matcher for determining whether the metric unit is
         *  preceded by a prefix.
         */
        private val precededByPrefix: ProductionMatcher<Production> by lazy {
            ProductionMatcher<Production>(
                listOf(PREFIX_SYMBOL, ATOM_SYMBOL_METRIC),
                null
            )
        }
    },

    /**
     *  Unit atom that is not metric.
     */
    ATOM_SYMBOL_NONMETRIC {
        override fun matchesLeftmost(token: String): Boolean =
            UnitOfMeasure.isUnit(token) &&
            !UnitOfMeasure.isMetric(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return Pair(Node(SIMPLE_UNIT), 1)
        }
    },

    SIMPLE_UNIT {
        override fun matchesLeftmost(token: String): Boolean =
            PREFIX_SYMBOL.matchesLeftmost(token) ||
            ATOM_SYMBOL_METRIC.matchesLeftmost(token) ||
            ATOM_SYMBOL_NONMETRIC.matchesLeftmost(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                annotatableMatcher.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(Node(ANNOTATABLE), 1)
            }
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }

        /**
         *  Production matcher for [ANNOTATABLE] in the context of
         *  [SIMPLE_UNIT] where the lookahead symbol can match as
         *  [EXPONENT].
         */
        private val annotatableMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(listOf(SIMPLE_UNIT), EXPONENT)
        }
    },

    ANNOTATABLE {
        override fun matchesLeftmost(token: String): Boolean =
            SIMPLE_UNIT.matchesLeftmost(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return Pair(Node(COMPONENT), 1)
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }
    },

    COMPONENT {
        override fun matchesLeftmost(token: String): Boolean =
            ANNOTATABLE.matchesLeftmost(token) ||
            FACTOR.matchesLeftmost(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return when {
                termMultiplyMatcher.matches(parseStack, null) ->
                    Pair(Node(TERM), 3)

                termDivideMatcher.matches(parseStack, null) ->
                    Pair(Node(TERM), 3)

                else ->
                    Pair(Node(TERM), 1)
            }
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }

        /**
         *  Production matcher for [TERM] in the context of [COMPONENT]
         *  where the operation is multiplication.
         */
        private val termMultiplyMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(
                listOf(TERM, MULTIPLY, COMPONENT),
                null
            )
        }

        /**
         *  Production matcher for [TERM] in the context of [COMPONENT]
         *  where the operation is division.
         */
        private val termDivideMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(
                listOf(TERM, DIVIDE, COMPONENT),
                null
            )
        }
    },

    TERM {
        override fun matchesLeftmost(token: String): Boolean =
            COMPONENT.matchesLeftmost(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                precededByOpeningParen.matches(parseStack, null) ->
                    null

                precededByDivide.matches(parseStack, null) ->
                    Pair(Node(MAIN_TERM), 2)

                followedByMultiply.matches(parseStack, lookaheadSymbol) ->
                    null

                followedByDivide.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(Node(MAIN_TERM), 1)
            }
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }

        /**
         *  Production matcher for [COMPONENT] in the context of [TERM].
         */
        private val precededByOpeningParen: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(
                listOf(OPENING_ROUND_BRACKET, TERM),
                null
            )
        }

        /**
         *  Production matcher for [MAIN_TERM] in the context of [TERM]
         *  that is preceded by a [DIVIDE].
         */
        private val precededByDivide: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(listOf(DIVIDE, TERM), null)
        }

        /**
         *  Production matcher for [TERM] that is followed by a [MULTIPLY].
         */
        private val followedByMultiply: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(listOf(TERM), MULTIPLY)
        }

        /**
         *  Production matcher for [TERM] that is followed by a [DIVIDE].
         */
        private val followedByDivide: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher<Production>(listOf(TERM), DIVIDE)
        }
    },

    MAIN_TERM {
        override val isAccepting: Boolean = true

        override fun matchesLeftmost(token: String): Boolean =
            DIVIDE.matchesLeftmost(token) ||
            TERM.matchesLeftmost(token)

        override fun reduce(
            parseStack: List<Node<Production>>,
            lookahead: Node<Production>?
        ): Pair<Node<Production>, Int>?
        {
            return null
        }

        override fun colorFill(node: Node<Production>) {
            super.colorFill(node)
        }
    };

    /**
     *  Colors a given node in a parse tree through [Node.setUserData].
     *
     *  @param node
     *      Node to color. Node type must match the enum constant that is being
     *      called.
     */
    open fun colorFill(node: Node<Production>) {
        if (node.type != this) {
            throw IllegalArgumentException(
                "Node type does not match the enum constant."
            )
        }
    }

    /**
     *  Only [MAIN_TERM] overrides this to be `true`.
     */
    override val isAccepting: Boolean = false

    companion object {
        /**
         *  Key used by the color filler for associating with the production
         *  value stored as user data in a node of the parse tree.
         */
        @JvmField
        val userDataKey: String = "production-value"
    }
}
