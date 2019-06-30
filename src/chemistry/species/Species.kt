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
 *  Interface for species.
 *
 *  Comparisons of species shall be referential.
 */
interface Species {
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
     *  Centroid of [atoms].
     *
     *  If this species is an [Atom], this property must be overridden to
     *  provide and to set the position of the atom itself, or an exception is
     *  raised.
     */
    var centroid: Vector3D
        get() {
            // Check that this species is not an atom.
            if (atoms().singleOrNull() == this) {
                throw RuntimeException(
                    "This species is an atom, and the property " +
                    "for obtaining the centroid is not properly overridden."
                )
            }

            var positionSum = Vector3D(0.0, 0.0, 0.0)
            var atomCount = 0

            for (atom in atoms()) {
                positionSum += atom.centroid
                ++atomCount
            }

            if (atomCount == 0) {
                throw RuntimeException(
                    "No atoms in this complex."
                )
            }

            return positionSum / atomCount.toDouble()
        }
        set(value) {
            val displacement = value - centroid

            for (atom in atoms()) {
                atom.centroid += displacement
            }
        }

    /**
     *  Gets a list of atoms by tag, or an empty list if there are no such
     *  atoms.
     */
    fun getAtomsByTag(tag: Int): List<Atom> =
        atoms().filter { it.tag == tag }

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
}
