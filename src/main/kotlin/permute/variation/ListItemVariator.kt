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
 *  Uses the values from an `Iterable` as the values to variate over in order.
 *
 *  @param E
 *      Type of elements being iterated over.
 */
class ListItemVariator<E> : Variator<E> {
    /**
     *  Values being iterated over.
     */
    private val items: List<E>

    /**
     *  Constructs a variator positioned at the first element of a given
     *  iterable.
     *
     *  @param items
     *      Values to variate over. The first element is defined as the
     *      beginning. It cannot be empty.
     */
    constructor(items: Iterable<E>) {
        if (items.count() < 1) {
            throw IllegalArgumentException(
                "Number of items being varied over must be at least one."
            )
        }

        this.items = items.toList()
    }

    /**
     *  Index of where the variator is currently at.
     */
    private var currIndex: Int = 0

    override fun value(): E =
        items[currIndex]

    override fun isBegin(): Boolean =
        currIndex == 0

    override fun isEnd(): Boolean =
        currIndex == items.count() - 1

    override fun begin(): ListItemVariator<E> =
        ListItemVariator(items)

    override fun end(): ListItemVariator<E> {
        val newVariator = ListItemVariator(items)

        newVariator.currIndex = items.count() - 1

        return newVariator
    }

    override fun inc(): ListItemVariator<E> {
        val newVariator = ListItemVariator(items)

        newVariator.currIndex =
            if (isEnd()) {
                0
            } else {
                currIndex + 1
            }

        return newVariator
    }

    override fun dec(): ListItemVariator<E> {
        val newVariator = ListItemVariator(items)

        newVariator.currIndex =
            if (isBegin()) {
                items.count() - 1
            } else {
                currIndex - 1
            }

        return newVariator
    }
}
