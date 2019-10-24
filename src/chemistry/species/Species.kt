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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import crul.apache.math.vector.*

/**
 *  Chemical species.
 *
 *  Comparisons of species are referential.
 */
interface Species : Cloneable {
    /**
     *  All atoms in this species, or itself if [Atom].
     *
     *  Collection may be empty. Atoms are unique and are in the same order
     *  between iterations. Atoms in the collection are not guaranteed to be in
     *  any particular order. A subinterface or an implementation, however, is
     *  allowed to make specified guarantees.
     */
    fun atoms(): Collection<Atom>

    /**
     *  Translates atoms in this species by a given displacement.
     *
     *  @param displacement
     *      Displacement of the translation.
     */
    fun translate(displacement: Vector3D) {
        for (atom in atoms()) {
            atom.position += displacement
        }
    }

    /**
     *  Gets a list of atoms by tag, or an empty list if there are no such
     *  atoms.
     */
    fun getAtomsByTag(tag: Int): List<Atom> =
        atoms().filter { it.tag == tag }

    /**
     *  Clones this species and all subspecies.
     *
     *  @return
     *      Deep clone of this species.
     */
    public abstract override fun clone(): Species
}
