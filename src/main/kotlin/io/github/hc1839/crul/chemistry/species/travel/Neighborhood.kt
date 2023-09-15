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

package io.github.hc1839.crul.chemistry.species.travel

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.Island

/**
 *  Neighbors of a source atom in an island.
 *
 *  @property sourceAtom
 *      Source atom of the neighborhood.
 *
 *  @property neighbors
 *      Collection of the neighbors of [sourceAtom].
 *
 *  @constructor
 */
data class Neighborhood<A : Atom>(
    val sourceAtom: A,
    val neighbors: Collection<Neighbor<A>>
) {
    /**
     *  Whether given pattern strings match this neighborhood.
     *
     *  A match is defined as being able to match the pattern strings with the
     *  neighbors one-to-one.
     *
     *  @param patterns
     *      Pattern strings in the syntax specified by [Neighbor.matches].
     *      Order is not important.
     *
     *  @return
     *      `true` if `patterns` match this neighborhood.
     */
    fun matches(vararg patterns: String): Boolean {
        if (neighbors.count() != patterns.count()) {
            return false
        } else {
            var remainingNeighbors = neighbors.toList()

            for (pattern in patterns) {
                val (matchingNeighbors, nonmatchingNeighbors) =
                    remainingNeighbors.partition { it.matches(pattern) }

                if (matchingNeighbors.isEmpty()) {
                    return false
                }

                remainingNeighbors = nonmatchingNeighbors +
                    matchingNeighbors.drop(1)
            }

            return remainingNeighbors.isEmpty()
        }
    }

    /**
     *  Whether given symbols of chemical elements match this neighborhood.
     *
     *  A match is defined as being able to match the symbols with the
     *  neighbors one-to-one.
     *
     *  @param symbols
     *      Symbols of chemical elements. Order is not important.
     *
     *  @return
     *      `true` if `symbols` match this neighborhood.
     */
    fun matchesElements(vararg symbols: String): Boolean {
        if (neighbors.count() != symbols.count()) {
            return false
        } else {
            var remainingNeighbors = neighbors.toList()

            for (symbol in symbols) {
                val (matchingNeighbors, nonmatchingNeighbors) =
                    remainingNeighbors.partition { it.matchesElement(symbol) }

                if (matchingNeighbors.isEmpty()) {
                    return false
                }

                remainingNeighbors = nonmatchingNeighbors +
                    matchingNeighbors.drop(1)
            }

            return remainingNeighbors.isEmpty()
        }
    }

    /**
     *  Whether this neighborhood contains a subset of neighbors where the
     *  neighbors can be matched with given pattern strings one-to-one.
     *
     *  @param patterns
     *      Pattern strings in the syntax specified by [Neighbor.matches].
     *      Order is not important.
     *
     *  @return
     *      `true` if this neighborhood contains `patterns`. If the number of
     *      pattern strings is greater than the number of neighbors, it is
     *      always `false`.
     */
    fun contains(vararg patterns: String): Boolean {
        if (neighbors.count() < patterns.count()) {
            return false
        } else {
            var remainingNeighbors = neighbors.toList()

            for (pattern in patterns) {
                val (matchingNeighbors, nonmatchingNeighbors) =
                    remainingNeighbors.partition { it.matches(pattern) }

                if (matchingNeighbors.isEmpty()) {
                    return false
                }

                remainingNeighbors = nonmatchingNeighbors +
                    matchingNeighbors.drop(1)
            }

            return true
        }
    }

    /**
     *  Whether this neighborhood contains a subset of neighbors where the
     *  neighbors can be matched with given symbols of chemical elements
     *  one-to-one.
     *
     *  @param symbols
     *      Symbols of chemical elements. Order is not important.
     *
     *  @return
     *      `true` if this neighborhood contains `symbols`. If the number of
     *      symbols is greater than the number of neighbors, it is always
     *      `false`.
     */
    fun containsElements(vararg symbols: String): Boolean {
        if (neighbors.count() < symbols.count()) {
            return false
        } else {
            var remainingNeighbors = neighbors.toList()

            for (symbol in symbols) {
                val (matchingNeighbors, nonmatchingNeighbors) =
                    remainingNeighbors.partition { it.matchesElement(symbol) }

                if (matchingNeighbors.isEmpty()) {
                    return false
                }

                remainingNeighbors = nonmatchingNeighbors +
                    matchingNeighbors.drop(1)
            }

            return true
        }
    }
}
