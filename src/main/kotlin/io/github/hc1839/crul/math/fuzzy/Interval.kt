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

package io.github.hc1839.crul.math.fuzzy

import io.github.hc1839.crul.math.descriptive.EndpointInclusion
import io.github.hc1839.crul.math.descriptive.Indicator
import io.github.hc1839.crul.math.descriptive.IntervalEndpointsSpec

/**
 *  Fuzzy interval.
 */
open class Interval<E : Comparable<E>> :
    Membership<E>,
    IntervalEndpointsSpec<E>
{
    /**
     *  Endpoints of the fuzzy interval.
     */
    protected val intervalEndpoints: IntervalEndpointsSpec<E>

    /**
     *  @param intervalEndpoints
     *      Endpoints of the fuzzy interval.
     *
     *  @param realLineMembership
     *      Membership function over the entire real line. Return value is
     *      ignored for elements outside the interval, since the membership
     *      degree of such element is always `0.0`. Default value (the
     *      indicator function) results in a crisp interval.
     */
    @JvmOverloads
    constructor(
        intervalEndpoints: IntervalEndpointsSpec<E>,
        realLineMembership: Membership<E> = Indicator<E>({ true })
    ): super(
        createIndicatorForCrispInterval(intervalEndpoints).intersect(
            realLineMembership
        )
    ) {
        this.intervalEndpoints = intervalEndpoints
    }

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

    companion object {
        /**
         *  Creates the indicator function for a crisp interval.
         */
        private fun <E : Comparable<E>> createIndicatorForCrispInterval(
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
