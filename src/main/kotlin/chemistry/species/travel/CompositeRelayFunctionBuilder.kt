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
 *  Builder of a composite relay function from zero or more relay functions for
 *  [RelayItinerary].
 *
 *  @param A
 *      Type of atoms.
 *
 *  @param V
 *      Type of landmark values.
 *
 *  @constructor
 */
class CompositeRelayFunctionBuilder<A : Atom, V>() {
    private val _relayFunctions =
        mutableListOf<(TravelLandmark<A, V>) -> Itinerary<A, V>?>()

    /**
     *  Adds a relay function.
     *
     *  @param value
     *      Relay function to add.
     *
     *  @return
     *      `this` builder.
     */
    fun addRelayFunction(
        value: (TravelLandmark<A, V>) -> Itinerary<A, V>?
    ): CompositeRelayFunctionBuilder<A, V>
    {
        _relayFunctions.add(value)
        return this
    }

    /**
     *  Creates a composite relay function from the builder data.
     */
    fun build(): (TravelLandmark<A, V>) -> Itinerary<A, V>? =
        object : (TravelLandmark<A, V>) -> Itinerary<A, V>? {
            private val relayFunctionsIterator = _relayFunctions.iterator()

            override fun invoke(
                previousLandmark: TravelLandmark<A, V>
            ): Itinerary<A, V>? =
                if (relayFunctionsIterator.hasNext()) {
                    relayFunctionsIterator.next().invoke(previousLandmark)
                } else {
                    throw RuntimeException(
                        "No more relay functions in the composite " +
                        "relay function."
                    )
                }
        }
}
