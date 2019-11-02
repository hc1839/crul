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
import crul.distinct.Referential

/**
 *  Interface for a complex, which is a species that is composed of
 *  referentially unique subspecies.
 *
 *  @param S
 *      Type of subspecies in this complex.
 */
interface Complex<S : Species> : Species {
    override fun atoms(): List<Atom> =
        subspecies
            .flatMap { it.atoms() }
            .distinctBy { Referential(it) }

    abstract override fun clone(): Complex<S>

    /**
     *  Subspecies in this complex.
     */
    val subspecies: List<S>

    /**
     *  Whether a given species is a subspecies in this complex.
     *
     *  Comparison is referential.
     */
    fun contains(species: S): Boolean =
        subspecies.any { it === species }

    /**
     *  Whether all species in a given collection are subspecies in this
     *  complex.
     *
     *  Comparison is referential.
     */
    fun containsAll(speciesCollection: Collection<S>): Boolean =
        speciesCollection.all { contains(it) }

    /**
     *  Whether an atom exists in this complex.
     *
     *  Comparison is referential.
     */
    fun containsAtom(atom: Atom): Boolean =
        atoms().any { it === atom }

    /**
     *  Centroid of [atoms].
     */
    fun centroid(): Vector3D {
        var positionSum = Vector3D(0.0, 0.0, 0.0)
        var atomCount = 0

        for (atom in atoms()) {
            positionSum += atom.position
            ++atomCount
        }

        return positionSum / atomCount.toDouble()
    }

    /**
     *  Radius.
     *
     *  It is defined as the largest distance between the centroid and any
     *  atom.
     */
    fun radius(): Double {
        val centroid = centroid()

        return atoms()
            .map {
                (it.position - centroid).norm
            }
            .max()!!
    }
}
