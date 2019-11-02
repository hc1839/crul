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
 *  Skeletal implementation of [Fragment].
 */
abstract class AbstractFragment<A : Atom> :
    AbstractComplex<A>,
    Fragment<A>
{
    /**
     *  @param atoms
     *      Atoms of the fragment.
     */
    constructor(atoms: List<A>): super(atoms.toList()) {
        if (atoms.isEmpty()) {
            throw IllegalArgumentException(
                "List of atoms given to construct a fragment is empty."
            )
        }
    }

    /**
     *  Copy constructor.
     *
     *  @param other
     *      Fragment to copy.
     */
    constructor(other: AbstractFragment<A>): super(other)
}
