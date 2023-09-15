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

package io.github.hc1839.crul.math.fuzzy.number

import io.github.hc1839.crul.float.FloatCompare.nearlyEquals
import io.github.hc1839.crul.math.fuzzy.Interval

/**
 *  Fuzzy number.
 *
 *  @param peak
 *      Peak of the fuzzy number. It must be between the lower and upper bounds
 *      of `interval`.
 *
 *  @param interval
 *      Fuzzy interval. The membership function must return `1.0` for `peak`.
 */
open class FuzzyNumber<E : Comparable<E>>(
    val peak: E,
    interval: Interval<E>
) : Interval<E>(interval)
{
    init {
        if (
            peak < interval.lo ||
            interval.hi < peak
        ) {
            throw IllegalArgumentException(
                "Peak is not between the lower and upper bounds."
            )
        }

        if (!nearlyEquals(interval.contains(peak), 1.0)) {
            throw IllegalArgumentException(
                "Membership degree of peak is not 1.0"
            )
        }
    }
}
