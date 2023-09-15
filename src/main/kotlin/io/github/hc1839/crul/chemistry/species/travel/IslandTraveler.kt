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

package io.github.hc1839.crul.chemistry.species.travel

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.Island
import io.github.hc1839.crul.distinct.Referential

/**
 *  Traveler of the neighborhoods in an [Island].
 *
 *  @property atom
 *      Atom that the traveler is at.
 *
 *  @property island
 *      Island of [atom].
 *
 *  @property journey
 *      List of previously visited atoms without backtracked atoms. The most
 *      recently visited atom is the last element.
 *
 *  @constructor
 *      If [atom] does not exist in [island], an exception is thrown.
 *
 *  @param visitedAtoms
 *      See [visitedAtoms]. If it is not a superset of [journey], an exception
 *      is thrown.
 */
class IslandTraveler<A : Atom> private constructor(
    val atom: A,
    val island: Island<A>,
    val journey: List<A>,
    visitedAtoms: Collection<A>
) {
    /**
     *  Collection of all visited atoms in no particular order.
     */
    val visitedAtoms: Collection<A>

    init {
        if (!island.containsAtom(atom)) {
            throw IllegalArgumentException(
                "Island does not contain the given atom."
            )
        }

        val wrappedVisitedAtoms = visitedAtoms
            .map { Referential(it) }
            .toSet()

        if (
            !wrappedVisitedAtoms.containsAll(
                journey.map { Referential(it) }
            )
        ) {
            throw IllegalArgumentException(
                "Not all atoms of the journey are " +
                "in the collection of visited atoms."
            )
        }

        this.visitedAtoms = wrappedVisitedAtoms.map { it.value }
    }

    /**
     *  See the corresponding properties for description of the parameters.
     */
    constructor(atom: A, island: Island<A>): this(
        atom,
        island,
        listOf(),
        listOf()
    )

    /**
     *  Neighborhood of [atom].
     */
    fun neighborhood(): Neighborhood<A> =
        island.getNeighborhood(atom)

    /**
     *  Neighbors that have not been visited before as next travel spots.
     */
    fun next(): Iterator<IslandTraveler<A>> {
        val wrappedVisitedAtoms = visitedAtoms
            .map { Referential(it) }
            .toSet()

        return island
            .getAtomsBondedTo(atom)
            .asSequence()
            .map { bondedAtom ->
                Referential(bondedAtom)
            }
            .filter { wrappedBondedAtom ->
                wrappedBondedAtom !in wrappedVisitedAtoms
            }
            .map { wrappedUnvisitedBondedAtom ->
                IslandTraveler(
                    wrappedUnvisitedBondedAtom.value,
                    island,
                    journey.plusElement(atom),
                    visitedAtoms.plusElement(atom)
                )
            }
            .iterator()
    }

    /**
     *  Gets the traveler of the previous atom in [journey].
     *
     *  The returned traveler considers the current atom as being visited but
     *  not as part of [journey]. If [journey] is empty, an exception is
     *  thrown.
     */
    fun previous(): IslandTraveler<A> =
        if (!journey.isEmpty()) {
            IslandTraveler(
                journey.last(),
                island,
                journey.dropLast(1),
                visitedAtoms.plusElement(atom)
            )
        } else {
            throw RuntimeException(
                "No previous atom in the journey."
            )
        }

    /**
     *  Creates a new island traveler starting a new journey where this
     *  traveler is at.
     */
    fun newPast(): IslandTraveler<A> =
        IslandTraveler(atom, island)
}
