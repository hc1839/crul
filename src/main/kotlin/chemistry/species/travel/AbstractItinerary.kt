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
import crul.chemistry.species.Island

/**
 *  Skeletal implementation of [Itinerary].
 *
 *  @property firstLandmark
 *      First landmark, or `null` for a failed empty sequence.
 *
 *  @property nextFunction
 *      Function that calculates the next landmark based on the previous one on
 *      each iteration. First `null` ends the iteration and indicates that no
 *      unexpected neighborhoods were encountered. Throwing an
 *      [ItineraryFailedException] ends the iteration, indicates that an
 *      unexpected neighborhood was encountered, and stores the exception in
 *      [failure]. Throwing an [ItinerarySkipException] skips to the next
 *      landmark. Any other exception is unhandled.
 *
 *  @constructor
 */
abstract class AbstractItinerary<A : Atom, V>(
    private val firstLandmark: TravelLandmark<A, V>?,
    private val nextFunction: (TravelLandmark<A, V>) -> TravelLandmark<A, V>?
) : AbstractIterator<TravelLandmark<A, V>>(),
    Itinerary<A, V>
{
    override var failure: ItineraryFailedException? = null
        protected set

    /**
     *  Previous landmark in the iteration.
     */
    protected var previousLandmark: TravelLandmark<A, V>? = null

    override fun computeNext() {
        if (firstLandmark == null) {
            done()
            failure = ItineraryFailedException("First landmark is 'null'.")
            return
        }

        if (previousLandmark == null) {
            setNext(firstLandmark)
            previousLandmark = firstLandmark
            return
        }

        var failureException: ItineraryFailedException? = null
        var nextLandmark: TravelLandmark<A, V>? = null

        do {
            // Whether to invoke the next function again.
            val invokeAgain = try {
                nextLandmark = nextFunction.invoke(previousLandmark!!)
                false
            } catch (e: ItineraryFailedException) {
                failureException = e
                nextLandmark = null
                false
            } catch (e: ItinerarySkipLandmarkException) {
                true
            } finally {
                failure = failureException
            }
        } while (invokeAgain)

        if (nextLandmark != null) {
            setNext(nextLandmark)
        } else {
            done()
        }

        previousLandmark = nextLandmark
    }
}
