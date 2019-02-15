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

package permute.variation

/**
 *  Variator over `Double` values.
 */
class DoubleVariator : Variator<Double> {
    /**
     *  Starting value.
     */
    val start: Double

    /**
     *  Difference between successive values.
     */
    val step: Double

    /**
     *  Number of values being iterated over.
     */
    val count: Int

    /**
     *  @param start
     *      Starting value.
     *
     *  @param step
     *      Difference between successive values.
     *
     *  @param count
     *      Number of values to iterate over.
     */
    constructor(start: Double, step: Double, count: Int) {
        if (count < 1) {
            throw IllegalArgumentException(
                "Number of values being varied over is not at least one."
            )
        }

        this.start = start
        this.step = step
        this.count = count
    }

    /**
     *  Index of where the variator is currently at.
     */
    private var currIndex: Int = 0

    override fun value(): Double =
        start + step * currIndex.toDouble()

    override fun isBegin(): Boolean =
        currIndex == 0

    override fun isEnd(): Boolean =
        currIndex == count - 1

    override fun begin(): DoubleVariator =
        DoubleVariator(start, step, count)

    override fun end(): DoubleVariator {
        val newVariator = DoubleVariator(start, step, count)

        newVariator.currIndex = count - 1

        return newVariator
    }

    override fun inc(): DoubleVariator {
        val newVariator = DoubleVariator(start, step, count)

        newVariator.currIndex =
            if (isEnd()) {
                0
            } else {
                currIndex + 1
            }

        return newVariator
    }

    override fun dec(): DoubleVariator {
        val newVariator = DoubleVariator(start, step, count)

        newVariator.currIndex =
            if (isBegin()) {
                count - 1
            } else {
                currIndex - 1
            }

        return newVariator
    }
}
