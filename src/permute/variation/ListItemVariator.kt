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
 *  Uses the values from an `Iterable` as the values to variate over in order.
 *
 *  @param items
 *      Values to variate over. The first element is defined as the beginning.
 *      It cannot be empty.
 */
class ListItemVariator<T>(items: Iterable<T>) : VariatorSingle<T> {
    private val items = items.toList()

    init {
        if (items.count() < 1) {
            throw IllegalArgumentException(
                "Number of items being varied over must be at least one."
            )
        }
    }

    /**
     *  Index of where the variator is currently at.
     */
    private var currIndex = 0

    override fun value() =
        items[currIndex]

    override fun isBegin() =
        currIndex == 0

    override fun isEnd() =
        currIndex == items.count() - 1

    override fun begin() =
        ListItemVariator(items)

    override fun end(): ListItemVariator<T> {
        val newVariator = ListItemVariator(items)

        newVariator.currIndex = items.count() - 1

        return newVariator
    }

    override fun inc(): ListItemVariator<T> {
        val newVariator = ListItemVariator(items)

        newVariator.currIndex =
            if (isEnd()) {
                0
            } else {
                currIndex + 1
            }

        return newVariator
    }

    override fun dec(): ListItemVariator<T> {
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
