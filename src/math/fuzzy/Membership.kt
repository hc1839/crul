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

package crul.math.fuzzy

/**
 *  Membership function as a class.
 *
 *  Since a membership function defines a fuzzy set, an instance of this class
 *  also represents a fuzzy set.
 *
 *  @param mem
 *      Membership function. It must return a value in `[0, 1]`.
 */
open class Membership<E>(
    private val mem: (E) -> Double
) : (E) -> Double
{
    /**
     *  Value of the membership function for `element`.
     */
    override fun invoke(element: E): Double {
        val memDegree = mem(element)

        if (memDegree < 0.0 || memDegree > 1.0) {
            throw RuntimeException(
                "Membership function did not return a value in [0, 1]."
            )
        }

        return memDegree
    }

    /**
     *  Intersection of this fuzzy set with `other`.
     */
    fun intersect(other: Membership<E>) = Membership<E>({ element: E ->
        listOf(mem, other.mem).map { memFn ->
            memFn(element)
        }.min()!!
    })

    /**
     *  Union of this fuzzy set with `other`.
     */
    fun union(other: Membership<E>) = Membership<E>({ element: E ->
        listOf(mem, other.mem).map { memFn ->
            memFn(element)
        }.max()!!
    })
}
