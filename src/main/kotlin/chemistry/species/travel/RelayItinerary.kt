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
 *  Itinerary as a relay of itineraries.
 *
 *  [failure] is set to the failure of any of the relay itineraries or an
 *  [ItineraryFailedException] from [relayFunction].
 *
 *  @param A
 *      Type of atoms.
 *
 *  @param V
 *      Type of landmark values.
 *
 *  @property relayFunction
 *      Function that calculates the next itinerary based on the last landmark
 *      of the previous itinerary on each iteration. `null` ends the relay of
 *      itineraries. Throwing an [ItineraryFailedException] ends the iteration,
 *      indicates that an unexpected neighborhood was encountered, and stores
 *      the exception in [failure].
 *
 *  @constructor
 *
 *  @param firstItinerary
 *      First itinerary in the relay.
 */
open class RelayItinerary<A : Atom, V>(
    firstItinerary: Itinerary<A, V>,
    private val relayFunction: (TravelLandmark<A, V>) -> Itinerary<A, V>?
) : AbstractIterator<TravelLandmark<A, V>>(),
    Itinerary<A, V>
{
    override var failure: ItineraryFailedException? = null
        protected set

    /**
     *  Current itinerary in the relay.
     */
    protected var currentItinerary: Itinerary<A, V>? = firstItinerary

    /**
     *  Previous landmark in the entire iteration.
     */
    protected var previousLandmark: TravelLandmark<A, V>? = null

    override fun computeNext() {
        if (currentItinerary == null || failure != null) {
            done()
            return
        }

        if (currentItinerary!!.hasNext()) {
            val nextLandmark = currentItinerary!!.next()
            setNext(nextLandmark)
            previousLandmark = nextLandmark
            failure = currentItinerary!!.failure
        } else if (previousLandmark != null) {
            failure = currentItinerary!!.failure
            currentItinerary = try {
                relayFunction.invoke(previousLandmark!!)
            } catch (e: ItineraryFailedException) {
                failure = e
                null
            }
            computeNext()
        } else {
            failure = currentItinerary!!.failure
            done()
        }
    }
}
