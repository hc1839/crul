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
 *  Wrapper of a species in a set that allows species to be compared by
 *  referential equality.
 */
class SpeciesSetElement<E : Species> {
    /**
     *  Species that is being wrapped.
     */
    val species: E

    /**
     *  @param species
     *      Species to be wrapped and used in a set.
     */
    constructor(species: E) {
        this.species = species
    }

    /**
     *  Always `1`.
     */
    override fun hashCode(): Int = 1

    /**
     *  Two [SpeciesSetElement] instances are equal if and only if their
     *  wrapped species are referentially equal.
     */
    override fun equals(other: Any?): Boolean =
        other is SpeciesSetElement<*> &&
        this.species === other.species
}
