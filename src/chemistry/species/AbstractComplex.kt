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
 *  Skeletal implementation of [Complex].
 *
 *  Iterating over the subspecies is guaranteed to be in the same order.
 *
 *  @param S
 *      Type of subspecies in this complex.
 */
abstract class AbstractComplex<S : Species> : Complex<S> {
    /**
     *  Subspecies in this complex in the order that is given to the
     *  constructor.
     */
    protected val subspecies: MutableList<S>

    /**
     *  @param subspecies
     *      Subspecies in this complex.
     */
    constructor(subspecies: Collection<S>) {
        val subspeciesList = subspecies
            .distinctBy { Referential(it) }
            .toMutableList()

        if (subspeciesList.count() != subspecies.count()) {
            throw IllegalArgumentException(
                "Subspecies in the collection are not unique."
            )
        }

        this.subspecies = subspeciesList
    }

    /**
     *  Copy constructor.
     *
     *  @param other
     *      Complex and subspecies to copy.
     */
    constructor(other: AbstractComplex<S>) {
        this.subspecies = other
            .subspecies
            .map {
                @Suppress("UNCHECKED_CAST")
                it.clone() as S
            }
            .toMutableList()
    }

    override fun iterator(): Iterator<S> =
        subspecies.iterator()
}
