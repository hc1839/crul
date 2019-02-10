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

import measure.dimension.BaseDimension
import measure.dimension.Dimension
import parse.ColorFiller
import parse.ParseNode
import parse.shiftreduce.ProductionMatcher
import parse.shiftreduce.Reducer

/**
 *  Production rules that are used as node types in a parse tree for an ISQ
 *  dimension.
 *
 *  Productions are based on the UCUM unit syntax but are adapted for ISQ
 *  dimensions.
 *
 *  Valid literal symbols are the digits (from '`0`' to '`9`'), plus sign
 *  ('`+`'), minus sign ('`-`'), period ('`.`'), and the uppercase letters
 *  representing the base dimensions according to ISQ with the exception of
 *  thermodynamic temperature, which is represented by '`Th`'. Note that
 *  solidus ('`/`') is not a valid symbol.
 *
 *  Digits and sign are used only as exponents. The plus sign is optional. If
 *  the exponent is `1`, it can be ommitted.
 *
 *  As in the UCUM unit syntax, the period is required for the multiplication
 *  of terms. For example, the dimension for velocity would be `L.T-1` (not
 *  `LT-1`).
 */
enum class Production :
    Reducer<Production>,
    ColorFiller<Production>
{
    /**
     *  Terminal symbol (not including base dimension).
     */
    TERMINAL {
        override fun matchesFirst(token: String): Boolean =
            DIGIT.matchesFirst(token) ||
            SIGN.matchesFirst(token) ||
            MULTIPLY.matchesFirst(token)

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

                else -> throw RuntimeException(
                    "Not a valid terminal symbol: $tokenSymbol"
                )
            }
        }
    },

    /**
     *  Literal symbol that is acting as an ID for something.
     *
     *  Currently, an ID always gets reduced to [BASE_DIMENSION]. If the token
     *  does not represent a known base dimension according to ISQ, an
     *  exception is raised.
     */
    ID {
        override fun matchesFirst(token: String): Boolean =
            BASE_DIMENSION.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val tokenSymbol = parseStack
                .last()
                .getUserData(userDataKey) as String

            if (!ID.matchesFirst(tokenSymbol)) {
                throw RuntimeException(
                    "Unknown ISQ base dimension: $tokenSymbol"
                )
            }

            return Pair(BASE_DIMENSION, 1)
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
            return null
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
                precededByDigit.matches(parseStack, null) ->
                    Pair(DIGITS, 2)

                precededByBaseDimension.matches(parseStack, null) ->
                    Pair(EXPONENT, 1)

                precededBySign.matches(parseStack, null) ->
                    Pair(EXPONENT, 2)

                else -> throw RuntimeException(
                    "Invalid 'DIGITS' specification."
                )
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

        /**
         *  Production matcher that matches a preceding [DIGIT].
         */
        private val precededByDigit: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(DIGIT, DIGITS), null)
        }

        /**
         *  Production matcher that matches a preceding [BASE_DIMENSION].
         */
        private val precededByBaseDimension: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(BASE_DIMENSION, DIGITS), null)
        }

        /**
         *  Production matcher for [EXPONENT] in the context of [DIGITS]
         *  with a preceding [SIGN].
         */
        private val precededBySign: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(SIGN, DIGITS), null)
        }
    },

    BASE_DIMENSION {
        override fun matchesFirst(token: String): Boolean =
            token in listOf("L", "M", "T", "I", "Th", "N", "J")

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            if (followedByExponent.matches(parseStack, lookaheadSymbol)) {
                return null
            } else {
                return Pair(TERM, 1)
            }
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val baseDimSymbol = node
                .firstChild!!.getUserData(userDataKey) as String

            val baseDim = BaseDimension.getBySymbol(baseDimSymbol)

            if (baseDim == null) {
                throw RuntimeException(
                    "Not a valid base dimension: $baseDimSymbol"
                )
            }

            node.setUserData(
                userDataKey,
                Dimension(baseDim),
                null
            )
        }

        /**
         *  Production matcher for a following exponent.
         */
        private val followedByExponent: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(BASE_DIMENSION), EXPONENT)
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
            if (!termMatcher.matches(parseStack, null)) {
                throw RuntimeException(
                    "Exponent is not preceded by 'BASE_DIMENSION'."
                )
            }

            return Pair(TERM, 2)
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
         *  Production matcher for [TERM] in the context of [EXPONENT].
         */
        private val termMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(
                listOf(BASE_DIMENSION, EXPONENT),
                null
            )
        }
    },

    TERM {
        override fun matchesFirst(token: String): Boolean =
            BASE_DIMENSION.matchesFirst(token)

        override fun reduce(
            parseStack: List<ParseNode<Production>>,
            lookahead: ParseNode<Production>?
        ): Pair<Production, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return if (termMatcher.matches(parseStack, lookaheadSymbol)) {
                Pair(TERM, 3)
            } else if (
                followedByMultiply.matches(parseStack, lookaheadSymbol)
            ) {
                null
            } else {
                Pair(TOTAL_DIMENSION, 1)
            }
        }

        override fun fill(node: ParseNode<Production>) {
            super.fill(node)

            val result = when (node.childNodes.count()) {
                1 -> {
                    node.firstChild!!.getUserData(userDataKey)
                }

                2 -> {
                    (node.firstChild!!.getUserData(userDataKey) as Dimension)
                        .pow(node.lastChild!!.getUserData(userDataKey) as Int)
                }

                3 -> {
                    val firstOperand = node
                        .firstChild!!
                        .getUserData(userDataKey) as Dimension

                    val secondOperand = node
                        .lastChild!!
                        .getUserData(userDataKey) as Dimension

                    if ((node.childNodes[1] as ParseNode<*>).type != MULTIPLY) {
                        throw RuntimeException(
                            "Not a multiplication of two 'TERM'."
                        )
                    }

                    firstOperand * secondOperand
                }

                else -> throw RuntimeException(
                    "'TERM' has an invalid number of child nodes."
                )
            }

            node.setUserData(userDataKey, result, null)
        }

        /**
         *  Production matcher for the multiplication of two [TERM].
         */
        private val termMatcher: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(TERM, MULTIPLY, TERM), null)
        }

        /**
         *  Production matcher the lookahead symbol being [MULTIPLY].
         */
        private val followedByMultiply: ProductionMatcher<Production>
        by lazy {
            ProductionMatcher(listOf(TERM), MULTIPLY)
        }
    },

    TOTAL_DIMENSION {
        override val isAccepting: Boolean = true

        override fun matchesFirst(token: String): Boolean =
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

            node.setUserData(
                userDataKey,
                node.firstChild!!.getUserData(userDataKey),
                null
            )
        }
    };

    /**
     *  The following enum constants overrides this to be `true`:
     *      - [TOTAL_DIMENSION]
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
