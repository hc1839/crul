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
 *  @param S
 *      Type of subspecies.
 *
 *  @constructor
 */
abstract class AbstractAggregate<S : Species>(
    override val subspecies: List<S>
) : Aggregate<S>
{
    init {
        if (this.subspecies.isEmpty()) {
            throw IllegalArgumentException(
                "List of subspecies to construct an aggregate " +
                "is empty."
            )
        }

        if (
            this.subspecies.distinctBy { Referential(it) }.count() !=
            this.subspecies.count()
        ) {
            throw IllegalArgumentException(
                "Subspecies in the given list to construct an aggregate " +
                "are not unique."
            )
        }
    }
}
