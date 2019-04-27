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
 *  Interface for species.
 */
interface Species : Cloneable {
    /**
     *  Clones this species and any subspecies.
     */
    public override fun clone(): Species =
        clone(true)

    /**
     *  Clones this species.
     *
     *  @param deep
     *      Whether subspecies are cloned.
     *
     *  @return
     *      Clone of this species.
     */
    fun clone(deep: Boolean): Species

    /**
     *  Atoms in this species, or itself if [Atom].
     *
     *  Collection may be empty. Atoms are unique and are in the same order
     *  between iterations. Atoms in the collection are not guaranteed to be in
     *  any particular order. A subinterface or an implementation, however, is
     *  allowed to make specified guarantees.
     */
    fun atoms(): Collection<Atom>
}
