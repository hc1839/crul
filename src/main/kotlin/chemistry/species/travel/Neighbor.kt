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
import crul.chemistry.species.Element
import crul.chemistry.species.Island

/**
 *  Bond order and bonded atom without the source atom.
 *
 *  @property bondOrder
 *      Bond order.
 *
 *  @property bondedAtom
 *      Bonded atom.
 *
 *  @constructor
 */
data class Neighbor<A : Atom>(
    val bondOrder: String,
    val bondedAtom: A
) {
    /**
     *  Pair, where first component is [bondOrder], and second component is
     *  [Atom.element] of [bondedAtom].
     */
    fun toElementalPair(): Pair<String, Element> =
        Pair(bondOrder, bondedAtom.element)
}
