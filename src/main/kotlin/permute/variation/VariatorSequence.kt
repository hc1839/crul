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
 *  Variator over a sequence of variators.
 *
 *  @param E
 *      Type of values of the individual variators.
 *
 *  @property bigEndian
 *      Whether the incrementation occurs fastest at the highest-indexed
 *      variator in the sequence.
 *
 *  @constructor
 *      Constructs a variator with all the individual variators positioned at
 *      the beginning.
 *
 *  @param variators
 *      Non-empty list of individual variators.
 */
class VariatorSequence<E>(
    variators: List<Variator<E>>,
    val bigEndian: Boolean
) : AbstractIterator<List<E>>(),
    Variator<List<E>>
{
    /**
     *  Individual variators.
     */
    private val variators: MutableList<Variator<E>>

    init {
        if (variators.isEmpty()) {
            throw IllegalArgumentException(
                "List of individual variators is empty."
            )
        }

        this.variators = variators.map { it.begin() }.toMutableList()
    }

    /**
     *  Values of the individual variators, or an empty list if iteration has
     *  not begun.
     */
    private val variatorValues: MutableList<E> =
        mutableListOf<E>()

    override fun begin(): VariatorSequence<E> =
        VariatorSequence(
            variators.map { it.begin() },
            bigEndian
        )

    override fun computeNext() {
        if (variators.all { !it.hasNext() }) {
            done()
            return
        }

        if (variatorValues.isEmpty()) {
            // Populate with the initial values of the variators. If any of the
            // variators has no elements, the variator sequence iterates over
            // no elements.
            for (variator in variators) {
                if (variator.hasNext()) {
                    variatorValues.add(variator.next())
                } else {
                    done()
                    return
                }
            }
        } else {
            // Sort the indices for incrementation according to endianness.
            val orderedVariatorIndices = if (bigEndian) {
                variators.indices.reversed()
            } else {
                variators.indices
            }

            for (variatorIndex in orderedVariatorIndices) {
                var variator = variators[variatorIndex]

                if (variator.hasNext()) {
                    variatorValues[variatorIndex] = variator.next()
                    break
                } else {
                    variator = variator.begin()
                    variators[variatorIndex] = variator
                    variatorValues[variatorIndex] = variator.next()
                }
            }
        }

        setNext(variatorValues.toList())
    }
}
