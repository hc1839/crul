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

package crul.math.stat.descriptive

import org.apache.commons.math3.stat.descriptive.rank.Percentile

/**
 *  Tukey Fences for determining outliers.
 */
class TukeyFences {
    /**
     *  The `k` constant.
     */
    var k: Double
        set(value) {
            if (value < 0.0) {
                throw IllegalArgumentException(
                    "'k' cannot be negative."
                )
            }

            field = value
        }

    /**
     *  @param k
     *      Non-negative value for the `k` constant. `k = 1.5` (default) is
     *      considered to be an "outlier", whereas `k = 3.0` is considered to
     *      be "far out".
     */
    @JvmOverloads
    constructor(k: Double = 1.5) {
        if (k < 0.0) {
            throw IllegalArgumentException(
                "'k' is negative."
            )
        }

        this.k = k
    }

    /**
     *  Truth values that indicate whether the corresponding element of a given
     *  list is considered to be an outlier according to Tukey Fences with
     *  respect to [k].
     */
    fun isOutlier(dataList: List<Double>): List<Boolean> =
        if (dataList.isEmpty()) {
            listOf()
        } else {
            val percentile = Percentile()

            val quartiles = listOf(25.0, 75.0).map {
                percentile.evaluate(dataList.toDoubleArray(), it)
            }

            val tukeyFences = listOf(
                quartiles[0] - k * (quartiles[1] - quartiles[0]),
                quartiles[1] + k * (quartiles[1] - quartiles[0])
            )

            dataList.map {
                it < tukeyFences[0] || it > tukeyFences[1]
            }
        }
}
