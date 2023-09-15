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

package io.github.hc1839.crul.math.stat.descriptive

import org.apache.commons.math3.stat.descriptive.rank.Percentile

/**
 *  Tukey Fences for determining outliers.
 */
class TukeyFences {
    constructor()

    /**
     *  Evaluates whether each data point in a list is considered to be an
     *  outlier according to Tukey Fences with respect to a given H-spread
     *  factor.
     *
     *  @param dataList
     *      List of data points.
     *
     *  @param hspreadFactor
     *      Non-negative value for the H-spread factor. `1.5` is considered to
     *      be an "outlier", whereas `3.0` is considered to be "far out".
     *
     *  @return
     *      Truth values indicating whether a corresponding data point in
     *      `dataList` is an outlier.
     */
    @JvmOverloads
    fun evaluate(
        dataList: List<Double>,
        hspreadFactor: Double = 1.5
    ): List<Boolean>
    {
        if (hspreadFactor < 0.0) {
            throw IllegalArgumentException(
                "H-spread factor is negative."
            )
        }

        return if (dataList.isEmpty()) {
            listOf()
        } else {
            val percentile = Percentile()

            val quartiles = listOf(25.0, 75.0).map {
                percentile.evaluate(dataList.toDoubleArray(), it)
            }

            val tukeyFences = listOf(
                quartiles[0] - hspreadFactor * (quartiles[1] - quartiles[0]),
                quartiles[1] + hspreadFactor * (quartiles[1] - quartiles[0])
            )

            dataList.map {
                it < tukeyFences[0] || it > tukeyFences[1]
            }
        }
    }
}
