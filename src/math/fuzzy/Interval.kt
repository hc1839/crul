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

import crul.math.descriptive.EndpointInclusion
import crul.math.descriptive.Indicator
import crul.math.descriptive.IntervalEndpointsSpec

/**
 *  Fuzzy interval.
 *
 *  @property intervalEndpoints
 *      Endpoints of the fuzzy interval.
 *
 *  @constructor
 *
 *  @param realLineMembership
 *      Membership function over the entire real line.
 */
open class Interval<E : Comparable<E>> @JvmOverloads constructor(
    protected val intervalEndpoints: IntervalEndpointsSpec<E>,
    realLineMembership: Membership<E> = Indicator<E>({ true })
) : Membership<E>(
        createIndicatorForInterval(intervalEndpoints)
            .intersect(realLineMembership)
    ),
    IntervalEndpointsSpec<E>
{
    /**
     *  Copy constructor.
     */
    constructor(other: Interval<E>): this(
        other.intervalEndpoints,
        other
    )

    override val lo: E
        get() = intervalEndpoints.lo

    override val hi: E
        get() = intervalEndpoints.hi

    override val type: EndpointInclusion
        get() = intervalEndpoints.type

    /**
     *  Membership degree of `element`.
     */
    fun contains(element: E): Double =
        this(element)

    companion object {
        /**
         *  Creates the indicator function for a crisp interval.
         */
        private fun <E : Comparable<E>> createIndicatorForInterval(
            intervalEndpoints: IntervalEndpointsSpec<E>
        ) =
            Indicator<E>({ element: E ->
                when (intervalEndpoints.type) {
                    EndpointInclusion.OPEN -> {
                        intervalEndpoints.lo < element &&
                        element < intervalEndpoints.hi
                    }

                    EndpointInclusion.LEFT_OPEN -> {
                        intervalEndpoints.lo < element &&
                        (
                            element < intervalEndpoints.hi ||
                            element == intervalEndpoints.hi
                        )
                    }

                    EndpointInclusion.RIGHT_OPEN -> {
                        (
                            element == intervalEndpoints.lo ||
                            intervalEndpoints.lo < element
                        ) &&
                        element < intervalEndpoints.hi
                    }

                    EndpointInclusion.CLOSED -> {
                        (
                            element == intervalEndpoints.lo ||
                            intervalEndpoints.lo < element
                        ) &&
                        (
                            element < intervalEndpoints.hi ||
                            element == intervalEndpoints.hi
                        )
                    }
                }
            })
    }
}
