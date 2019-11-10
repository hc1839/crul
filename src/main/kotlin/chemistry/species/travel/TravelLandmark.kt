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
 *  Landmark of where an island traveler is at.
 *
 *  @param A
 *      Type of atoms being visited.
 *
 *  @param V
 *      Type of landmark value.
 *
 *  @property traveler
 *      Island traveler.
 *
 *  @property value
 *      Landmark value.
 *
 *  @constructor
 */
data class TravelLandmark<A : Atom, V>(
    val traveler: IslandTraveler<A>,
    val value: V
)
