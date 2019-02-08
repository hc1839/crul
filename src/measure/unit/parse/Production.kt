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

import hierarchy.tree.TypedNode
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
        override fun matchesFirst(token: String): Boolean =
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
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            val tokenSymbol = parseStack
                .last()
                .getUserData(userDataKey) as String

            return when {
                DIGIT.matchesFirst(tokenSymbol) ->
                    Pair(TypedNode(DIGIT), 1)

                SIGN.matchesFirst(tokenSymbol) ->
                    Pair(TypedNode(SIGN), 1)

                MULTIPLY.matchesFirst(tokenSymbol) ->
                    Pair(TypedNode(MULTIPLY), 1)

                DIVIDE.matchesFirst(tokenSymbol) ->
                    Pair(TypedNode(DIVIDE), 1)

                OPENING_ROUND_BRACKET.matchesFirst(tokenSymbol) ->
                    Pair(TypedNode(OPENING_ROUND_BRACKET), 1)

                CLOSING_ROUND_BRACKET.matchesFirst(tokenSymbol) ->
                    Pair(TypedNode(CLOSING_ROUND_BRACKET), 1)

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
        override fun matchesFirst(token: String): Boolean =
            Regex(
                "^[!#-'\\*,0-<>-Z\\\\^-z|~]*[!#-'\\*,:-<>-Z\\\\^-z|~]$"
            ) in token

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
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
                    Pair(TypedNode(PREFIX_SYMBOL), 1)

                ATOM_SYMBOL_METRIC.matchesFirst(tokenSymbol) ->
                    Pair(TypedNode(ATOM_SYMBOL_METRIC), 1)

                ATOM_SYMBOL_NONMETRIC.matchesFirst(tokenSymbol) ->
                    Pair(TypedNode(ATOM_SYMBOL_NONMETRIC), 1)

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
            ProductionMatcher<Production>(listOf(ID), ATOM_SYMBOL_METRIC)
        }
    },

    /**
     *  Opening round bracket.
     */
    OPENING_ROUND_BRACKET {
        override fun matchesFirst(token: String): Boolean =
            token == "("

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
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
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            if (!componentMatcher.matches(parseStack, null)) {
                throw RuntimeException(
                    "Invalid production for 'COMPONENT' " +
                    "at 'CLOSING_ROUND_BRACKET'."
                )
            }

            return Pair(TypedNode(COMPONENT), 3)
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
        override fun matchesFirst(token: String): Boolean =
            token == "."

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
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
        override fun matchesFirst(token: String): Boolean =
            token == "/"

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
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
        private val termMatcher: ProductionMatcher<Production> by lazy {
            ProductionMatcher<Production>(listOf(TERM, DIVIDE), COMPONENT)
        }

        /**
         *  Production matcher for [MAIN_TERM] in the context of [DIVIDE].
         */
        private val mainTermMatcher: ProductionMatcher<Production> by lazy {
            ProductionMatcher<Production>(listOf(DIVIDE), TERM)
        }
    },

    SIGN {
        override fun matchesFirst(token: String): Boolean =
            token in listOf("+", "-")

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            return null
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

            node.setUserData(
                userDataKey,
                node.firstChild!!.getUserData(userDataKey),
                null
            )
        }
    },

    DIGIT {
        override fun matchesFirst(token: String): Boolean =
            token in (0..9).map { it.toString() }

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                digitsLookaheadMatcher.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(TypedNode(DIGITS), 1)
            }
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

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
            ProductionMatcher<Production>(listOf(DIGIT), DIGITS)
        }
    },

    DIGITS {
        override fun matchesFirst(token: String): Boolean =
            DIGIT.matchesFirst(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            return when {
                digitsMatcher.matches(parseStack, null) ->
                    Pair(TypedNode(DIGITS), 2)

                precededBySign.matches(parseStack, null) ->
                    Pair(TypedNode(EXPONENT), 2)

                precededBySimpleUnit.matches(parseStack, null) ->
                    Pair(TypedNode(EXPONENT), 1)

                else ->
                    Pair(TypedNode(FACTOR), 1)
            }
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

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
        override fun matchesFirst(token: String): Boolean =
            DIGITS.matchesFirst(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            return Pair(TypedNode(COMPONENT), 1)
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

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
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            if (!annotatableMatcher.matches(parseStack, null)) {
                throw RuntimeException(
                    "Exponent is not preceded by 'SIMPLE_UNIT'."
                )
            }

            return Pair(TypedNode(ANNOTATABLE), 2)
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

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
            ProductionMatcher<Production>(
                listOf(SIMPLE_UNIT, EXPONENT),
                null
            )
        }
    },

    PREFIX_SYMBOL {
        override fun matchesFirst(token: String): Boolean =
            UnitPrefix.isPrefix(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            return null
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

            val prefixCs = node
                .firstChild!!
                .getUserData(userDataKey) as String

            node.setUserData(userDataKey, UnitPrefix.getValue(prefixCs), null)
        }
    },

    /**
     *  Unit atom that is metric.
     */
    ATOM_SYMBOL_METRIC {
        override fun matchesFirst(token: String): Boolean =
            UnitOfMeasure.isMetric(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            val childCount =
                if (precededByPrefix.matches(parseStack, null)) {
                    2
                } else {
                    1
                }

            return Pair(TypedNode(SIMPLE_UNIT), childCount)
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

            val unitAtomCs = node
                .firstChild!!
                .getUserData(userDataKey) as String

            val unitAtom = Production.createUnitAtom(unitAtomCs)

            node.setUserData(userDataKey, unitAtom, null)
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
        override fun matchesFirst(token: String): Boolean =
            UnitOfMeasure.isUnit(token) &&
            !UnitOfMeasure.isMetric(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            return Pair(TypedNode(SIMPLE_UNIT), 1)
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

            val unitAtomCs = node
                .firstChild!!
                .getUserData(userDataKey) as String

            val unitAtom = Production.createUnitAtom(unitAtomCs)

            node.setUserData(userDataKey, unitAtom, null)
        }
    },

    SIMPLE_UNIT {
        override fun matchesFirst(token: String): Boolean =
            PREFIX_SYMBOL.matchesFirst(token) ||
            ATOM_SYMBOL_METRIC.matchesFirst(token) ||
            ATOM_SYMBOL_NONMETRIC.matchesFirst(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                annotatableMatcher.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(TypedNode(ANNOTATABLE), 1)
            }
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

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
            ProductionMatcher<Production>(listOf(SIMPLE_UNIT), EXPONENT)
        }
    },

    ANNOTATABLE {
        override fun matchesFirst(token: String): Boolean =
            SIMPLE_UNIT.matchesFirst(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            return Pair(TypedNode(COMPONENT), 1)
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

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
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            return when {
                termMultiplyMatcher.matches(parseStack, null) ->
                    Pair(TypedNode(TERM), 3)

                termDivideMatcher.matches(parseStack, null) ->
                    Pair(TypedNode(TERM), 3)

                else ->
                    Pair(TypedNode(TERM), 1)
            }
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

            val component = when (node.firstChild!!.type) {
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
        override fun matchesFirst(token: String): Boolean =
            COMPONENT.matchesFirst(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            val lookaheadSymbol = lookahead
                ?.getUserData(userDataKey) as String?

            return when {
                precededByOpeningParen.matches(parseStack, null) ->
                    null

                precededByDivide.matches(parseStack, null) ->
                    Pair(TypedNode(MAIN_TERM), 2)

                followedByMultiply.matches(parseStack, lookaheadSymbol) ->
                    null

                followedByDivide.matches(parseStack, lookaheadSymbol) ->
                    null

                else ->
                    Pair(TypedNode(MAIN_TERM), 1)
            }
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

            val result = if (node.childNodes.count() == 1) {
                node.firstChild!!.getUserData(userDataKey)
            } else {
                val firstOperand = node
                    .firstChild!!
                    .getUserData(userDataKey) as UnitOfMeasure

                val secondOperand = node
                    .lastChild!!
                    .getUserData(userDataKey) as UnitOfMeasure

                when (node.childNodes[1].type) {
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

        override fun matchesFirst(token: String): Boolean =
            DIVIDE.matchesFirst(token) ||
            TERM.matchesFirst(token)

        override fun reduce(
            parseStack: List<TypedNode<Production>>,
            lookahead: TypedNode<Production>?
        ): Pair<TypedNode<Production>, Int>?
        {
            return null
        }

        override fun colorFill(node: TypedNode<Production>) {
            super.colorFill(node)

            val operand = node
                .lastChild!!
                .getUserData(userDataKey) as UnitOfMeasure

            val result = when (node.childNodes.count()) {
                1 -> operand

                2 -> {
                    if (node.firstChild!!.type != DIVIDE) {
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
    open fun colorFill(node: TypedNode<Production>) {
        if (node.type != this) {
            throw IllegalArgumentException(
                "Node type does not match the enum constant."
            )
        }
    }

    private object FriendKey : visaccess.FriendKey()

    open class FriendAccess(key: visaccess.FriendKey) :
        visaccess.FriendAccess
    {
        init {
            if (key != FriendKey) {
                throw IllegalArgumentException(
                    "Invalid friend key."
                )
            }
        }
    }

    protected object Friendship : FriendAccess(FriendKey)

    companion object {
        /**
         *  Key used by the color filler for associating with the production
         *  value stored as user data in a node of the parse tree.
         */
        @JvmField
        val userDataKey: String = "production-value"

        /**
         *  JSON of UCUM base units parsed by Gson.
         */
        protected val baseUnits: Map<String, Map<String, Any>> by lazy {
            UnitOfMeasure.baseUnits(Friendship)
        }

        /**
         *  JSON of UCUM derived units parsed by Gson.
         */
        protected val derivedUnits: Map<String, Map<String, Any>> by lazy {
            UnitOfMeasure.derivedUnits(Friendship)
        }

        /**
         *  Creates a [UnitOfMeasure] from the UCUM c/s symbol of a unit atom.
         *
         *  @param unitAtomCs
         *      UCUM c/s symbol of a unit atom.
         */
        protected fun createUnitAtom(unitAtomCs: String): UnitOfMeasure =
            if (baseUnits.contains(unitAtomCs)) {
                UnitOfMeasure(unitAtomCs)
            } else if (derivedUnits.contains(unitAtomCs)) {
                val defValue =
                    derivedUnits[unitAtomCs]!!["definition-value"] as Double

                val defUnitText =
                    derivedUnits[unitAtomCs]!!["definition-unit"] as String

                UnitOfMeasure.parse(defUnitText) * defValue
            } else {
                throw IllegalArgumentException(
                    "Unknown unit atom: $unitAtomCs"
                )
            }
    }
}
