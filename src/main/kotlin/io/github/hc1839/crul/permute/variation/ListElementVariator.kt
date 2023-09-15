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

package io.github.hc1839.crul.permute.variation

/**
 *  Variator that uses the elements of a `List` as the values to variate over.
 *
 *  @param E
 *      Type of values being variated over.
 *
 *  @property elements
 *      Non-empty list of values being variated over.
 *
 *  @constructor
 *      Constructs a variator positioned at the first element of [elements].
 */
class ListElementVariator<E>(private val elements: List<E>) :
    AbstractIterator<E>(),
    Variator<E>
{
    init {
        if (elements.isEmpty()) {
            throw IllegalArgumentException(
                "List of values to variate over cannot be empty."
            )
        }
    }

    /**
     *  Index of where the variator is currently at.
     */
    private var currIndex: Int = 0

    override fun begin(): ListElementVariator<E> =
        ListElementVariator(elements)

    override fun computeNext() {
        if (currIndex != elements.count()) {
            setNext(elements[currIndex++])
        } else {
            done()
        }
    }
}
