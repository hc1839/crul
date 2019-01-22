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
 *  Sequence of variators.
 *
 *  There must be at least one variator in the sequence. To access the
 *  individual variators, treat this class as an `Iterable`.
 *
 *  @param variators
 *      Variators of the sequence.
 *
 *  @param bigEndian
 *      Whether the incrementation/decrementation occurs fastest at the
 *      rightmost (highest index) variator in the sequence.
 */
class VariatorSequence<T> @JvmOverloads constructor(
    variators: Iterable<VariatorCollection<T>>,
    val bigEndian: Boolean = true
) : VariatorCollection<T>
{
    init {
        if (variators.count() < 1) {
            throw IllegalArgumentException(
                "Number of variators must be at least one."
            )
        }
    }

    /**
     *  Variators of this sequence.
     */
    private val variators = variators.map {
        if (it.count() == 1) {
            it.first()
        } else {
            it
        }
    }

    override fun iterator() = variators.iterator()

    override fun isBegin() =
        variators.all { it.isBegin() }

    override fun isEnd() =
        variators.all { it.isEnd() }

    override fun begin() =
        VariatorSequence(variators.map { it.begin() }, bigEndian)

    override fun end() =
        VariatorSequence(variators.map { it.end() }, bigEndian)

    override fun inc(): VariatorCollection<T> {
        val orderedVariators =
            if (bigEndian) {
                variators.reversed().toMutableList()
            } else {
                variators.toMutableList()
            }

        for (idx in orderedVariators.indices) {
            if (orderedVariators[idx].isEnd()) {
                orderedVariators[idx] = orderedVariators[idx].begin()
            } else {
                ++orderedVariators[idx]
                break
            }
        }

        return VariatorSequence(
            if (bigEndian) {
                orderedVariators.reversed()
            } else {
                orderedVariators
            },
            bigEndian
        )
    }

    override fun dec(): VariatorCollection<T> {
        val orderedVariators =
            if (bigEndian) {
                variators.reversed().toMutableList()
            } else {
                variators.toMutableList()
            }

        for (idx in orderedVariators.indices) {
            if (orderedVariators[idx].isBegin()) {
                orderedVariators[idx] = orderedVariators[idx].end()
            } else {
                --orderedVariators[idx]
                break
            }
        }

        return VariatorSequence(
            if (bigEndian) {
                orderedVariators.reversed()
            } else {
                orderedVariators
            },
            bigEndian
        )
    }
}
