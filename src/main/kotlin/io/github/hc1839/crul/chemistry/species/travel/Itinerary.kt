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

package io.github.hc1839.crul.chemistry.species.travel

import io.github.hc1839.crul.chemistry.species.Atom

/**
 *  Iterator of the landmarks encountered by an island traveler.
 *
 *  @param A
 *      Type of atoms.
 *
 *  @param V
 *      Type of landmark value.
 */
interface Itinerary<A : Atom, V> : Iterator<TravelLandmark<A, V>> {
    /**
     *  Failure, if any, in completing the journey.
     *
     *  It is non-`null` if the iteration is done but an unexpected
     *  neighborhood was encountered, in which the landmarks that have been
     *  iterated over should not be regarded as valid.
     */
    val failure: ItineraryFailedException?
}
