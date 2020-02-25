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

import crul.distinct.Referential

/**
 *  Skeletal implementation of [Aggregate].
 *
 *  Iterating over the subspecies must be in the same order.
 *
 *  @param A
 *      Type of atoms.
 *
 *  @constructor
 *
 *  @param S
 *      Type of subspecies.
 *
 *  @param subspecies
 *      Subspecies in the aggregate.
 */
abstract class AbstractAggregate<S : Species>(subspecies: List<S>) :
    Aggregate<S>
{
    override val subspecies: List<S> =
        subspecies.toList()

    init {
        if (subspecies.isEmpty()) {
            throw IllegalArgumentException(
                "List of subspecies to construct an aggregate " +
                "is empty."
            )
        }

        if (
            subspecies.distinctBy { Referential(it) }.count() !=
            subspecies.count()
        ) {
            throw IllegalArgumentException(
                "Subspecies in the given list to construct an aggregate " +
                "are not unique."
            )
        }
    }
}
