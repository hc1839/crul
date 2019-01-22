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
 *
 *  @property start
 *      Starting value.
 *
 *  @property step
 *      Difference between successive values.
 *
 *  @property count
 *      Number of values to variate over. It must be at least 1.
 *
 *  @constructor
 */
class DoubleVariator(
    val start: Double,
    val step: Double,
    val count: Int
) : VariatorSingle<Double>
{
    init {
        if (count < 1) {
            throw IllegalArgumentException(
                "Number of values being varied over is not at least one."
            )
        }
    }

    /**
     *  Index of where the variator is currently at.
     */
    private var currIndex = 0

    override fun value() =
        start + step * currIndex.toDouble()

    override fun isBegin() =
        currIndex == 0

    override fun isEnd() =
        currIndex == count - 1

    override fun begin() =
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
