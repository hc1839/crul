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
 *  Interface of a species aggregate, which is a species that is composed of
 *  one or more referentially unique subspecies.
 *
 *  @param S
 *      Type of subspecies.
 */
interface Aggregate<S : Species> : Species {
    override fun atoms(): List<Atom> =
        subspecies
            .flatMap { it.atoms() }
            .distinctBy { Referential(it) }

    /**
     *  Subspecies.
     */
    val subspecies: List<S>

    /**
     *  Whether `species` is a subspecies in the aggregate.
     *
     *  Comparison is referential.
     */
    fun contains(species: S): Boolean =
        subspecies.any { it === species }

    /**
     *  Whether all species in `speciesCollection` are subspecies in the
     *  aggregate.
     *
     *  Comparison is referential.
     */
    fun containsAll(speciesCollection: Collection<S>): Boolean =
        speciesCollection.all { contains(it) }

    /**
     *  Whether an atom exists in the aggregate.
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
     *  Largest distance between the centroid and any atom.
     */
    fun radius(): Double {
        val centroid = centroid()

        return atoms()
            .map { (it.position - centroid).getNorm() }
            .max()!!
    }

    companion object {
        /**
         *  Constructs an [Aggregate].
         *
         *  @param subspecies
         *      Subspecies in the aggregate.
         *
         *  @return
         *      New instance of [Aggregate].
         */
        @JvmStatic
        fun <S : Species> newInstance(subspecies: List<S>) : Aggregate<S> =
            AggregateImpl(subspecies)
    }
}

/**
 *  Constructs a new instance of [Aggregate].
 *
 *  See [Aggregate.newInstance].
 */
fun <S : Species> Aggregate(subspecies: List<S>) : Aggregate<S> =
    Aggregate.newInstance(subspecies)
