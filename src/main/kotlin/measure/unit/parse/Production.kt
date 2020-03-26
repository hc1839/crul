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

package crul.measure.unit.parse

import crul.measure.unit.UnitOfMeasure
import crul.measure.unit.UnitPrefix
import crul.parse.ColorFiller
import crul.parse.ParseNode
import crul.parse.shiftreduce.ProductionMatcher
import crul.parse.shiftreduce.Reducer

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
enum class Production :
    Reducer<Production>,
    ColorFiller<Production>
{
    /**
     *  Literal symbol that is not acting as an ID for something.
     */
    TERMINAL {
        override fun matchesFirst(token: String): Boolean =
            DIGIT.matchesFirst(token) ||
            SIGN.matchesFirst(token) ||
            MULTIPLY.matchesFirst(token) ||
            DIVIDE.matchesFirst(token) ||
            OPENING_ROUND_BRACKET.matchesFirst(token) ||
            CLOSING_ROUND_BRACKET.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val tokenSymbol = parseStack
                .last()
                .getUserData(userDataKey) as String

            return when {
                DIGIT.matchesFirst(tokenSymbol) ->
                    Pair(DIGIT, 1)

                SIGN.matchesFirst(tokenSymbol) ->
                    Pair(SIGN, 1)

                MULTIPLY.matchesFirst(tokenSymbol) ->
                    Pair(MULTIPLY, 1)

                DIVIDE.matchesFirst(tokenSymbol) ->
                    Pair(DIVIDE, 1)

                OPENING_ROUND_BRACKET.matchesFirst(tokenSymbol) ->
                    Pair(OPENING_ROUND_BRACKET, 1)

                CLOSING_ROUND_BRACKET.matchesFirst(tokenSymbol) ->
                    Pair(CLOSING_ROUND_BRACKET, 1)

                else -> throw RuntimeException(
                    "Not a valid terminal symbol: $tokenSymbol"
                )
            }
        }
    },

    /**
     *  Literal symbol that is acting as an ID for something.
     *
     *  Currently, an ID can get reduced to one of the following.
     *      - [ATOM_SYMBOL_METRIC]
     *      - [ATOM_SYMBOL_NONMETRIC]
     *      - [PREFIX_SYMBOL]
     *
     *  If the token does not represent a known c/s symbol, an exception is
     *  raised.
     */
    ID {
        override fun matchesFirst(token: String): Boolean =
            Regex(
                """^[!#-'*,0-z|~]*[!#-'*,:-Z\]-z|~]$"""
            ) in token

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val tokenSymbol = parseStack
                .last()
                .getUserData(userDataKey) as String

            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                PREFIX_SYMBOL.matchesFirst(tokenSymbol) &&
                metricUnitLookaheadMatcher.matches(
                    parseStack,
                    lookaheadSymbol
                ) ->
                    Pair(PREFIX_SYMBOL, 1)

                ATOM_SYMBOL_METRIC.matchesFirst(tokenSymbol) ->
                    Pair(ATOM_SYMBOL_METRIC, 1)

                ATOM_SYMBOL_NONMETRIC.matchesFirst(tokenSymbol) ->
                    Pair(ATOM_SYMBOL_NONMETRIC, 1)

                else -> throw RuntimeException(
                    "Unknown UCUM c/s symbol: $tokenSymbol"
                )
            }
        }

        /**
         *  Production matcher for matching the lookahead as a metric unit.
         */
        private val metricUnitLookaheadMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(ID), ATOM_SYMBOL_METRIC)
        }
    },

    /**
     *  Opening round bracket.
     */
    OPENING_ROUND_BRACKET {
        override fun matchesFirst(token: String): Boolean =
            token == "("

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return null
        }
    },

    /**
     *  Closing round bracket.
     */
    CLOSING_ROUND_BRACKET {
        override fun matchesFirst(token: String): Boolean =
            token == ")"

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            if (!componentMatcher.matches(parseStack, null)) {
                throw RuntimeException(
                    "Invalid production for 'COMPONENT' " +
                    "at 'CLOSING_ROUND_BRACKET'."
                )
            }

            return Pair(COMPONENT, 3)
        }

        /**
         *  Production matcher for [COMPONENT] in the context of
         *  [CLOSING_ROUND_BRACKET].
         */
        private val componentMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(
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
        override fun matchesFirst(token: String): Boolean =
            token == "."

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
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
        private val termMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(TERM, MULTIPLY), COMPONENT)
        }
    },

    /**
     *  Division operator.
     *
     *  '`/`' is the division operator.
     */
    DIVIDE {
        override fun matchesFirst(token: String): Boolean =
            token == "/"

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            if (!(
                termMatcher.matches(parseStack, lookaheadSymbol) ||
                mainTermMatcher.matches(parseStack, lookaheadSymbol)
            )) {
                throw RuntimeException(
                    "Division operator is not preceded or followed " +
                    "by the expected token type."
                )
            }

            return null
        }

        /**
         *  Production matcher for [TERM] in the context of [DIVIDE].
         */
        private val termMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(TERM, DIVIDE), COMPONENT)
        }

        /**
         *  Production matcher for [MAIN_TERM] in the context of [DIVIDE].
         */
        private val mainTermMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(DIVIDE), TERM)
        }
    },

    SIGN {
        override fun matchesFirst(token: String): Boolean =
            token in listOf("+", "-")

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return null
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            node.setUserData(
                userDataKey,
                node.firstChild!!.getUserData(userDataKey),
                null
            )
        }
    },

    DIGIT {
        override fun matchesFirst(token: String): Boolean =
            Regex("^\\d$") in token

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                digitsLookaheadMatcher.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(DIGITS, 1)
            }
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            node.setUserData(
                userDataKey,
                node.firstChild!!.getUserData(userDataKey),
                null
            )
        }

        /**
         *  Production matcher for [DIGITS] in the context of [DIGIT] where
         *  the lookahead symbol can match as [DIGITS].
         */
        private val digitsLookaheadMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(DIGIT), DIGITS)
        }
    },

    DIGITS {
        override fun matchesFirst(token: String): Boolean =
            DIGIT.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return when {
                digitsMatcher.matches(parseStack, null) ->
                    Pair(DIGITS, 2)

                precededBySign.matches(parseStack, null) ->
                    Pair(EXPONENT, 2)

                precededBySimpleUnit.matches(parseStack, null) ->
                    Pair(EXPONENT, 1)

                else ->
                    Pair(FACTOR, 1)
            }
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            node.setUserData(
                userDataKey,
                node
                    .childNodes
                    .map {
                        it.getUserData(userDataKey) as String
                    }
                    .joinToString(""),
                null
            )
        }

        private val digitsMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(DIGIT, DIGITS), null)
        }

        /**
         *  Production matcher for [EXPONENT] in the context of [DIGITS]
         *  with a preceding [SIGN].
         */
        private val precededBySign: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(SIGN, DIGITS), null)
        }

        /**
         *  Production matcher for [EXPONENT] in the context of [DIGITS]
         *  with a preceding [SIMPLE_UNIT].
         */
        private val precededBySimpleUnit: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(SIMPLE_UNIT, DIGITS), null)
        }
    },

    FACTOR {
        override fun matchesFirst(token: String): Boolean =
            DIGITS.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return Pair(COMPONENT, 1)
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val digitsAsInt =
                (
                    node
                        .firstChild!!
                        .getUserData(userDataKey) as String
                )
                .toInt()

            node.setUserData(
                userDataKey,
                UnitOfMeasure() * digitsAsInt.toDouble(),
                null
            )
        }
    },

    EXPONENT {
        override fun matchesFirst(token: String): Boolean =
            SIGN.matchesFirst(token) ||
            DIGITS.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            if (!annotatableMatcher.matches(parseStack, null)) {
                throw RuntimeException(
                    "Exponent is not preceded by 'SIMPLE_UNIT'."
                )
            }

            return Pair(ANNOTATABLE, 2)
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            var exponent =
                (node.lastChild!!.getUserData(userDataKey) as String)
                .toInt()

            if (
                node.childNodes.count() == 2 &&
                node.firstChild!!.getUserData(userDataKey) as String == "-"
            ) {
                exponent = -exponent
            }

            node.setUserData(userDataKey, exponent, null)
        }

        /**
         *  Production matcher for [ANNOTATABLE] in the context of
         *  [EXPONENT].
         */
        private val annotatableMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(
                listOf(SIMPLE_UNIT, EXPONENT),
                null
            )
        }
    },

    PREFIX_SYMBOL {
        override fun matchesFirst(token: String): Boolean =
            UnitPrefix.isPrefix(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return null
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val prefixCs = node
                .firstChild!!
                .getUserData(userDataKey) as String

            node.setUserData(
                userDataKey,
                UnitPrefix.getValue(prefixCs),
                null
            )
        }
    },

    /**
     *  Unit atom that is metric.
     */
    ATOM_SYMBOL_METRIC {
        override fun matchesFirst(token: String): Boolean =
            UnitOfMeasure.isMetric(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val childCount =
                if (precededByPrefix.matches(parseStack, null)) {
                    2
                } else {
                    1
                }

            return Pair(SIMPLE_UNIT, childCount)
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val unitAtomCs = node
                .firstChild!!
                .getUserData(userDataKey) as String

            val unitAtom = UnitOfMeasure.parse(unitAtomCs)

            node.setUserData(userDataKey, unitAtom, null)
        }

        /**
         *  Production matcher for determining whether the metric unit is
         *  preceded by a prefix.
         */
        private val precededByPrefix: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(
                listOf(PREFIX_SYMBOL, ATOM_SYMBOL_METRIC),
                null
            )
        }
    },

    /**
     *  Unit atom that is not metric.
     */
    ATOM_SYMBOL_NONMETRIC {
        override fun matchesFirst(token: String): Boolean =
            UnitOfMeasure.isUnitAtom(token) &&
            !UnitOfMeasure.isMetric(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return Pair(SIMPLE_UNIT, 1)
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val unitAtomCs = node
                .firstChild!!
                .getUserData(userDataKey) as String

            val unitAtom = UnitOfMeasure.parse(unitAtomCs)

            node.setUserData(userDataKey, unitAtom, null)
        }
    },

    SIMPLE_UNIT {
        override fun matchesFirst(token: String): Boolean =
            PREFIX_SYMBOL.matchesFirst(token) ||
            ATOM_SYMBOL_METRIC.matchesFirst(token) ||
            ATOM_SYMBOL_NONMETRIC.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                annotatableMatcher.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(ANNOTATABLE, 1)
            }
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val unitAtom = node
                .lastChild!!
                .getUserData(userDataKey) as UnitOfMeasure

            val simpleUnit = when (node.childNodes.count()) {
                1 -> unitAtom

                2 -> {
                    val prefix = node
                        .firstChild!!
                        .getUserData(userDataKey) as Double

                    unitAtom * prefix
                }

                else -> throw RuntimeException(
                    "[Internal Error] " +
                    "'SIMPLE_UNIT' has ${node.childNodes.count()} " +
                    "child nodes."
                )
            }

            node.setUserData(userDataKey, simpleUnit, null)
        }

        /**
         *  Production matcher for [ANNOTATABLE] in the context of
         *  [SIMPLE_UNIT] where the lookahead symbol can match as
         *  [EXPONENT].
         */
        private val annotatableMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(SIMPLE_UNIT), EXPONENT)
        }
    },

    ANNOTATABLE {
        override fun matchesFirst(token: String): Boolean =
            SIMPLE_UNIT.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return Pair(COMPONENT, 1)
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val simpleUnit = node
                .firstChild!!
                .getUserData(userDataKey) as UnitOfMeasure

            val annotatable = when (node.childNodes.count()) {
                1 -> simpleUnit

                2 -> {
                    val exponent = node
                        .lastChild!!
                        .getUserData(userDataKey) as Int

                    simpleUnit.pow(exponent)
                }

                else -> throw RuntimeException(
                    "[Internal Error] " +
                    "'ANNOTATABLE' has ${node.childNodes.count()} " +
                    "child nodes."
                )
            }

            node.setUserData(userDataKey, annotatable, null)
        }
    },

    COMPONENT {
        override fun matchesFirst(token: String): Boolean =
            ANNOTATABLE.matchesFirst(token) ||
            FACTOR.matchesFirst(token) ||
            OPENING_ROUND_BRACKET.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return when {
                termMultiplyMatcher.matches(parseStack, null) ->
                    Pair(TERM, 3)

                termDivideMatcher.matches(parseStack, null) ->
                    Pair(TERM, 3)

                else ->
                    Pair(TERM, 1)
            }
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val component = when ((node.firstChild!! as ParseNode<*>).type) {
                ANNOTATABLE, FACTOR ->
                    node.firstChild!!.getUserData(userDataKey)

                OPENING_ROUND_BRACKET ->
                    node.childNodes[1].getUserData(userDataKey)

                else -> throw RuntimeException(
                    "[Internal Error] " +
                    "'COMPONENT' does not have the expected types " +
                    "of child nodes."
                )
            }

            node.setUserData(userDataKey, component, null)
        }

        /**
         *  Production matcher for [TERM] in the context of [COMPONENT]
         *  where the operation is multiplication.
         */
        private val termMultiplyMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(
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
            ProductionMatcher(
                listOf(TERM, DIVIDE, COMPONENT),
                null
            )
        }
    },

    TERM {
        override fun matchesFirst(token: String): Boolean =
            COMPONENT.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                precededByOpeningParen.matches(parseStack, null) ->
                    null

                precededByDivide.matches(parseStack, null) ->
                    Pair(MAIN_TERM, 2)

                followedByMultiply.matches(parseStack, lookaheadSymbol) ->
                    null

                followedByDivide.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(MAIN_TERM, 1)
            }
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val result = if (node.childNodes.count() == 1) {
                node.firstChild!!.getUserData(userDataKey)
            } else {
                val firstOperand = node
                    .firstChild!!
                    .getUserData(userDataKey) as UnitOfMeasure

                val secondOperand = node
                    .lastChild!!
                    .getUserData(userDataKey) as UnitOfMeasure

                when ((node.childNodes[1] as ParseNode<*>).type) {
                    MULTIPLY -> firstOperand * secondOperand

                    DIVIDE -> firstOperand / secondOperand

                    else -> throw RuntimeException(
                        "[Internal Error] " +
                        "'TERM' does not have the expected types " +
                        "of child nodes."
                    )
                }
            }

            node.setUserData(userDataKey, result, null)
        }

        /**
         *  Production matcher for [COMPONENT] in the context of [TERM].
         */
        private val precededByOpeningParen: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(
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
            ProductionMatcher(listOf(DIVIDE, TERM), null)
        }

        /**
         *  Production matcher for [TERM] that is followed by a [MULTIPLY].
         */
        private val followedByMultiply: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(TERM), MULTIPLY)
        }

        /**
         *  Production matcher for [TERM] that is followed by a [DIVIDE].
         */
        private val followedByDivide: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(TERM), DIVIDE)
        }
    },

    MAIN_TERM {
        override val isAccepting: Boolean = true

        override fun matchesFirst(token: String): Boolean =
            DIVIDE.matchesFirst(token) ||
            TERM.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            return null
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val operand = node
                .lastChild!!
                .getUserData(userDataKey) as UnitOfMeasure

            val result = when (node.childNodes.count()) {
                1 -> operand

                2 -> {
                    if ((node.firstChild!! as ParseNode<*>).type != DIVIDE) {
                        throw RuntimeException(
                            "[Internal Error] " +
                            "'MAIN_TERM' has two child nodes, " +
                            "but the first child is not 'DIVIDE'"
                        )
                    }

                    UnitOfMeasure() / operand
                }

                else -> throw RuntimeException(
                    "[Internal Error] " +
                    "'MAIN_TERM' does not have the expected number " +
                    "of child nodes."
                )
            }

            node.setUserData(userDataKey, result, null)
        }
    };

    /**
     *  The following enum constants overrides this to be `true`:
     *      - [MAIN_TERM]
     */
    override val isAccepting: Boolean = false

    /**
     *  Colors a given node in a parse tree through [Node.setUserData].
     *
     *  @param node
     *      Node to color. Node type must match the enum constant that is being
     *      called.
     */
    override fun fill(node: ParseNode<Production>) {
        if (node.type != this) {
            throw IllegalArgumentException(
                "Node type does not match the enum constant."
            )
        }
    }

    companion object {
        /**
         *  Key used by the color filler for associating with the production
         *  value stored as user data in a node of the parse tree.
         */
        @JvmField
        val userDataKey: String = "production-value"
    }
}
