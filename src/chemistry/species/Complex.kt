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

import crul.math.coordsys.Vector3D

/**
 *  Interface for a complex, which is a species that is also set of subspecies.
 *
 *  Subspecies are unique and are in the same order between iterations.
 *
 *  @param S
 *      Type of subspecies in this complex.
 */
interface Complex<S : Species> :
    Species,
    Collection<S>
{
    override val size: Int
        get() = iterator().asSequence().count()

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun contains(species: S): Boolean =
        iterator().asSequence().contains(species)

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun containsAll(speciesCollection: Collection<S>): Boolean =
        speciesCollection.all { contains(it) }

    override fun isEmpty(): Boolean =
        iterator().asSequence().count() == 0

    override fun atoms(): Collection<Atom> =
        iterator()
            .asSequence()
            .toList()
            .flatMap { it.atoms() }
            .distinct()

    override fun clone(): Complex<S> =
        @Suppress("UNCHECKED_CAST") (
            super.clone() as Complex<S>
        )

    abstract override fun clone(deep: Boolean): Complex<S>
}
