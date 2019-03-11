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
     *  Backing property for the subspecies in this complex.
     */
    protected val _subspecies: MutableList<S>

    /**
     *  @param subspecies
     *      Subspecies within this complex.
     */
    constructor(subspecies: Collection<S>) {
        this._subspecies = subspecies.toMutableList()
    }

    /**
     *  Copy constructor.
     *
     *  Subspecies are cloned.
     *
     *  @param other
     *      Complex to copy.
     */
    constructor(other: AbstractComplex<S>) {
        @Suppress("UNCHECKED_CAST")
        this._subspecies = other
            ._subspecies
            .map { it.clone() as S }
            .toMutableList()
    }

    override fun iterator(): Iterator<S> =
        _subspecies.iterator()
}
