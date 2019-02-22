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
 *  Implementation of Tukey Fences for determining outliers.
 *
 *  @constructor
 *
 *  @param k
 *      Non-negative value for the `k` constant. `k = 1.5` (default) is
 *      considered to be an "outlier", whereas `k = 3.0` is considered to be
 *      "far out".
 */
class TukeyFences @JvmOverloads constructor(k: Double = 1.5) {
    /**
     *  The `k` constant.
     */
    var k: Double = k
        set(value) {
            if (value < 0.0) {
                throw IllegalArgumentException(
                    "'k' cannot be negative."
                )
            }

            field = value
        }

    init {
        this.k = k
    }

    /**
     *  Zero-based indices of a given `Iterable` that are considered to be
     *  outliers according to Tukey Fences with respect to [k].
     */
    fun outlierIndices(dataListIter: Iterable<Double>): List<Int> {
        val dataList = dataListIter.toList()

        return if (dataList.isEmpty()) {
            listOf<Int>()
        } else {
            val percentile = Percentile()

            val quartiles = listOf(25.0, 75.0).map {
                percentile.evaluate(dataList.toDoubleArray(), it)
            }

            val tukeyFences = listOf(
                quartiles[0] - k * (quartiles[1] - quartiles[0]),
                quartiles[1] + k * (quartiles[1] - quartiles[0])
            )

            dataList.indices.filter {
                dataList[it] < tukeyFences[0] || dataList[it] > tukeyFences[1]
            }
        }
    }
}
