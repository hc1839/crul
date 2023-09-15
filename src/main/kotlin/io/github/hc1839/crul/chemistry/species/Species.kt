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

package io.github.hc1839.crul.chemistry.species

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.apache.math.vector.*

/**
 *  Chemical species with immutable data.
 *
 *  Comparisons of species are referential.
 */
interface Species {
    /**
     *  List of all atoms in the species, or singleton list of itself if
     *  [Atom].
     *
     *  Atoms are referentially distinct. Order of the atoms is maintained
     *  between evaluations but is implementation-specific.
     */
    val atoms: List<Atom>

    /**
     *  Centroid of [atoms].
     */
    fun centroid(): Vector3D {
        var positionSum = Vector3D(0.0, 0.0, 0.0)
        var atomCount = 0

        for (atom in atoms) {
            positionSum += atom.position
            ++atomCount
        }

        return positionSum / atomCount.toDouble()
    }

    /**
     *  Center of the extremes along each dimension of the positions of [atoms].
     */
    fun volumetricCenter(): Vector3D {
        val positions = atoms.map { it.position }

        return Vector3D(
            (0..2).map { axisIndex ->
                // Coordinates along this axis.
                val coords = positions.map {
                    it.toArray()[axisIndex]
                }

                coords.min()!! + (coords.max()!! - coords.min()!!) * 0.5
            }.toDoubleArray()
        )
    }
}
