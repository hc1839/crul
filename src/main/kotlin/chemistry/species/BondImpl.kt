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

package crul.chemistry.species

import java.nio.ByteBuffer

import crul.distinct.Referential

/**
 *  Default implementation of [Bond].
 *
 *  @constructor
 *      See [Bond.newInstance].
 */
internal class BondImpl<A : Atom>(
    atom1: A,
    atom2: A,
    override val bondType: String
) : Bond<A>
{
    override val subspecies: List<A>

    init {
        if (atom1 === atom2) {
            throw IllegalArgumentException(
                "Atoms are referentially equal."
            )
        }

        this.subspecies = listOf(atom1, atom2)
    }

    override fun toAtomPair(): Pair<A, A> =
        Pair(subspecies[0], subspecies[1])
}
