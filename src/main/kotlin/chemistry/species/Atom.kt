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

import crul.chemistry.species.Element

/**
 *  Interface of an atom.
 *
 *  An atom is a singleton [Species].
 *
 *  To construct an instance of this class, use the factory function of the
 *  same name or [newInstance].
 */
interface Atom : Species {
    /**
     *  Singleton list containing itself.
     */
    override val atoms: List<Atom>
        get() = listOf(this)

    /**
     *  Element of the atom.
     */
    val element: Element

    /**
     *  Position of the atom.
     */
    val position: Vector3D

    /**
     *  Island representing this atom.
     *
     *  It always returns the same atom island. Two atom islands are
     *  referentially equal if and only if the two atoms are referentially
     *  equal.
     */
    val island: AtomIsland<Atom>

    companion object {
        /**
         *  Constructs an [Atom].
         *
         *  @param element
         *      Element of the atom.
         *
         *  @param position
         *      Position of the atom.
         *
         *  @return
         *      New instance of [Atom].
         */
        @JvmStatic
        fun newInstance(element: Element, position: Vector3D): Atom =
            AtomImpl(element, position)
    }
}

/**
 *  Constructs a new instance of [Atom].
 *
 *  See [Atom.newInstance].
 */
fun Atom(element: Element, position: Vector3D): Atom =
    Atom.newInstance(element, position)
