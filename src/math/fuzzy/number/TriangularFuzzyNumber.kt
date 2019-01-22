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

package math.fuzzy.number

import math.descriptive.EndpointInclusion
import math.descriptive.IntervalEndpoints
import math.descriptive.IntervalEndpointsSpec
import math.fuzzy.Interval
import math.fuzzy.Membership

/**
 *  Triangular fuzzy number (TFN).
 *
 *  @param peak
 *      Height of the triangle.
 *
 *  @param intervalEndpoints
 *      Endpoints of the TFN.
 */
open class TriangularFuzzyNumber(
    peak: Double,
    intervalEndpoints: IntervalEndpointsSpec<Double>
) : FuzzyNumber<Double>(
        peak,
        Interval<Double>(
            intervalEndpoints,
            Membership<Double>({ element: Double ->
                if (element == peak) {
                    1.0
                } else if (element < peak) {
                    (element - intervalEndpoints.lo) /
                    (peak - intervalEndpoints.lo)
                } else {
                    (intervalEndpoints.hi - element) /
                    (intervalEndpoints.hi - peak)
                }
            })
        )
    )
{
    /**
     *  Uses an isosceles triangle for the TFN.
     *
     *  @param peak
     *      Height of the isosceles triangle.
     *
     *  @param base
     *      Length of the base of the isosceles triangle.
     */
    constructor(peak: Double, base: Double): this(
        peak,
        IntervalEndpoints<Double>(
            peak - base / 2.0,
            peak + base / 2.0,
            EndpointInclusion.CLOSED
        )
    )
}
