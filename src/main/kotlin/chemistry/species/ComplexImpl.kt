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
 *  Default implementation of [Complex].
 *
 *  @param S
 *      Type of subspecies in this complex.
 */
internal class ComplexImpl<S : Species> : AbstractComplex<S> {
    constructor(subspecies: List<S>): super(subspecies)

    /**
     *  Copy constructor.
     */
    constructor(other: ComplexImpl<S>): super(other)

    override fun clone(): ComplexImpl<S> =
        ComplexImpl(this)
}
