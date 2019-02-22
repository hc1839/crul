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

@file:JvmMultifileClass

package crul.float

/**
 *  Functions for comparing two floating-point numbers.
 */
object Comparison {
    /**
     *  Adapted from `https://floating-point-gui.de/errors/comparison/`.
     */
    @JvmStatic
    @JvmOverloads
    fun nearlyEquals(a: Float, b: Float, epsilon: Float = 2.0e-16f): Boolean {
        val absA = kotlin.math.abs(a)
        val absB = kotlin.math.abs(b)
        val diff = kotlin.math.abs(a - b)

        if (a == b) {
            return true
        } else if (a == 0.0f || b == 0.0f || diff < java.lang.Float.MIN_NORMAL) {
            return diff < (epsilon * java.lang.Float.MIN_NORMAL);
        } else {
            return diff / kotlin.math.min(absA + absB, java.lang.Float.MAX_VALUE) < epsilon
        }
    }

    /**
     *  Same but for `Double`.
     */
    @JvmStatic
    @JvmOverloads
    fun nearlyEquals(a: Double, b: Double, epsilon: Double = 2.0e-16) = nearlyEquals(
        a.toFloat(),
        b.toFloat(),
        epsilon.toFloat()
    )
}
