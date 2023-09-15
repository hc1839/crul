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

package io.github.hc1839.crul.math.descriptive

/**
 *  Interface for specifying the endpoints of an interval.
 *
 *  Upper bound of the interval cannot be less than the lower bound.
 */
interface IntervalEndpointsSpec<E : Comparable<E>> {
    /**
     *  Lower bound (leftmost) endpoint.
     */
    val lo: E

    /**
     *  Upper bound (rightmost) endpoint.
     */
    val hi: E

    /**
     *  Type of the interval.
     */
    val type: EndpointInclusion
}

/**
 *  Endpoints of an interval.
 */
data class IntervalEndpoints<E : Comparable<E>>(
    override val lo: E,
    override val hi: E,
    override val type: EndpointInclusion
) : IntervalEndpointsSpec<E>
{
    init {
        if (hi < lo) {
            throw IllegalArgumentException(
                "Upper bound of the interval " +
                "is less than the lower bound."
            )
        }
    }
}
