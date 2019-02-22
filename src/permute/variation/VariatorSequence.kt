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

package crul.permute.variation

/**
 *  Sequence of variators.
 *
 *  Instances of this class are iterable over the individual variators.
 *
 *  @param V
 *      Type of variators in this sequence.
 */
class VariatorSequence<V : Variator<V>> :
    Variator<VariatorSequence<V>>,
    Iterable<V>
{
    /**
     *  Variators of this sequence.
     */
    private val variators: List<V>

    /**
     *  Whether the incrementation/decrementation occurs fastest at the
     *  rightmost (highest index) variator in the sequence.
     */
    val bigEndian: Boolean

    /**
     *  Constructs a variator positioned at the current positions of the
     *  individual variators.
     *
     *  @param variators
     *      Variators of the sequence. There must be at least one variator.
     *
     *  @param bigEndian
     *      Whether the incrementation/decrementation occurs fastest at the
     *      rightmost (highest index) variator in the sequence.
     */
    @JvmOverloads
    constructor(variators: Iterable<V>, bigEndian: Boolean = true) {
        if (variators.count() < 1) {
            throw IllegalArgumentException(
                "Number of variators is less than one."
            )
        }

        this.variators = variators.toList()
        this.bigEndian = bigEndian
    }

    override fun iterator(): Iterator<V> =
        variators.iterator()

    /**
     *  Returns itself.
     */
    override fun value(): VariatorSequence<V> =
        this

    override fun isBegin(): Boolean =
        variators.all { it.isBegin() }

    override fun isEnd(): Boolean =
        variators.all { it.isEnd() }

    override fun begin(): VariatorSequence<V> =
        @Suppress("UNCHECKED_CAST")
        VariatorSequence(
            variators.map { it.begin() as V },
            bigEndian
        )

    override fun end(): VariatorSequence<V> =
        @Suppress("UNCHECKED_CAST")
        VariatorSequence(
            variators.map { it.end() as V },
            bigEndian
        )

    override fun inc(): VariatorSequence<V> {
        val orderedVariators =
            if (bigEndian) {
                variators.reversed().toMutableList()
            } else {
                variators.toMutableList()
            }

        for (index in orderedVariators.indices) {
            if (orderedVariators[index].isEnd()) {
                @Suppress("UNCHECKED_CAST")
                orderedVariators[index] =
                    orderedVariators[index].begin() as V
            } else {
                @Suppress("UNCHECKED_CAST")
                orderedVariators[index] =
                    orderedVariators[index].inc() as V

                break
            }
        }

        val incrementedVariators =
            if (bigEndian) {
                orderedVariators.reversed()
            } else {
                orderedVariators
            }

        return VariatorSequence(
            incrementedVariators,
            bigEndian
        )
    }

    override fun dec(): VariatorSequence<V> {
        val orderedVariators =
            if (bigEndian) {
                variators.reversed().toMutableList()
            } else {
                variators.toMutableList()
            }

        for (index in orderedVariators.indices) {
            if (orderedVariators[index].isBegin()) {
                @Suppress("UNCHECKED_CAST")
                orderedVariators[index] =
                    orderedVariators[index].end() as V
            } else {
                @Suppress("UNCHECKED_CAST")
                orderedVariators[index] =
                    orderedVariators[index].dec() as V

                break
            }
        }

        val decrementedVariators =
            if (bigEndian) {
                orderedVariators.reversed()
            } else {
                orderedVariators
            }

        return VariatorSequence(
            decrementedVariators,
            bigEndian
        )
    }
}
