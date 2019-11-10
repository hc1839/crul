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

package crul.chemistry.species.travel

import crul.chemistry.species.Atom

/**
 *  Builder of a composite next function from zero or more next functions.
 *
 *  @param A
 *      Type of atoms.
 *
 *  @param V
 *      Type of landmark values.
 *
 *  @constructor
 */
class CompositeNextFunctionBuilder<A : Atom, V>() {
    private val _nextFunctions =
        mutableListOf<(TravelLandmark<A, V>) -> TravelLandmark<A, V>?>()

    /**
     *  Adds a next function.
     *
     *  @param value
     *      Next function to add.
     *
     *  @return
     *      `this` builder.
     */
    fun addNextFunction(
        value: (TravelLandmark<A, V>) -> TravelLandmark<A, V>?
    ): CompositeNextFunctionBuilder<A, V>
    {
        _nextFunctions.add(value)
        return this
    }

    /**
     *  Creates a composite next function from the builder data.
     */
    fun build(): (TravelLandmark<A, V>) -> TravelLandmark<A, V>? =
        object : (TravelLandmark<A, V>) -> TravelLandmark<A, V>? {
            private val nextFunctionsIterator = _nextFunctions.iterator()

            override fun invoke(
                previousLandmark: TravelLandmark<A, V>
            ): TravelLandmark<A, V>? =
                if (nextFunctionsIterator.hasNext()) {
                    nextFunctionsIterator.next().invoke(previousLandmark)
                } else {
                    throw RuntimeException(
                        "No more next functions in the composite " +
                        "next function."
                    )
                }
        }
}
