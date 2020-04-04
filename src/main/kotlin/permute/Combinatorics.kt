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

package crul.permute

import crul.permute.variation.ListElementVariator
import crul.permute.variation.VariatorSequence

/**
 *  Permutations of a set.
 *
 *  @param k
 *      Size of permutations. It must be a non-negative integer that is less
 *      than or equal to the number of elements in the set.
 *
 *  @return
 *      `k`-permutations of the set.
 */
fun <T> Set<T>.permutations(k: Int): Sequence<List<T>> {
    if (k < 0 || k > count()) {
        throw IllegalArgumentException(
            "k is negative or greater than " +
            "the number of elements in the set."
        )
    }

    return if (k == 0 || isEmpty()) {
        listOf(listOf<T>()).iterator().asSequence()
    } else {
        val elements = toList()

        val variator = VariatorSequence(
            (0 until k).map { ListElementVariator(elements) },
            true
        )

        variator.asSequence().filter { permutation ->
            permutation.distinct().count() == permutation.count()
        }
    }
}

/**
 *  Combinations of a set.
 *
 *  @param k
 *      Size of combinations. It must be a non-negative integer that is less
 *      than or equal to the number of elements in the set.
 *
 *  @return
 *      `k`-combinations of the set.
 */
fun <T> Set<T>.combinations(k: Int): Sequence<List<T>> =
    permutations(k).distinctBy { it.toSet() }
