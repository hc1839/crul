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

package crul.chemistry.species.travel

import crul.chemistry.species.Atom
import crul.chemistry.species.Element
import crul.chemistry.species.Island
import crul.chemistry.species.format.mol2.TriposBond

/**
 *  Bond order and bonded atom without the source atom.
 *
 *  @property bondOrder
 *      Bond order.
 *
 *  @property bondedAtom
 *      Bonded atom.
 *
 *  @constructor
 */
data class Neighbor<A : Atom>(
    val bondOrder: String,
    val bondedAtom: A
) {
    /**
     *  Whether a given pattern string matches this neighbor.
     *
     *  @param pattern
     *      Pattern string. It is composed of the string value of a Tripos bond
     *      type (`1`, `2`, `3`, `am`, `ar`, `du`, `un`, or `nc`) and the
     *      symbol of an element. The string value of the Tripos bond type
     *      ([TriposBond.BondType.value]) is converted to
     *      [TriposBond.BondType.name], which is then compared to [bondOrder].
     *      For example, `2C` indicates double bonding to a carbon, and it
     *      matches if [bondOrder] is `DOUBLE` and the element of [bondedAtom]
     *      is carbon. If the pattern string does not conform to the expected
     *      syntax, an exception is thrown.
     *
     *  @return
     *      `true` if `pattern` matches this neighbor.
     */
    fun matches(pattern: String): Boolean {
        val matchResult = patternRegex.find(pattern)

        if (matchResult == null) {
            throw IllegalArgumentException(
                "Invalid pattern expression: $pattern"
            )
        }

        val patternBondType = enumValues<TriposBond.BondType>().single {
            it.value == matchResult.groupValues[1]
        }

        val patternElement = Element(matchResult.groupValues[2])

        return bondOrder == patternBondType.name &&
            bondedAtom.element.symbol == patternElement.symbol
    }

    /**
     *  Whether `symbol` of a chemical element matches this neighbor.
     */
    fun matchesElement(symbol: String): Boolean =
        bondedAtom.element.symbol == Element(symbol).symbol

    /**
     *  Pair, where first component is [bondOrder], and second component is
     *  [Atom.element] of [bondedAtom].
     */
    fun toElementalPair(): Pair<String, Element> =
        Pair(bondOrder, bondedAtom.element)

    companion object {
        /**
         *  Regular expression for matching a given pattern.
         */
        val patternRegex: Regex = Regex(
            "^(" +
            enumValues<TriposBond.BondType>()
                .map { it.value }
                .joinToString("|") +
            ")" +
            "([A-Z][a-z]*)$"
        )
    }
}
