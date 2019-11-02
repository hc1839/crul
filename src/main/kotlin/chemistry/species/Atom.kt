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
import crul.serialize.AvroSimple

/**
 *  Interface for an atom.
 *
 *  An atom is a singleton [Species].
 *
 *  To construct an instance of this class, use [newInstance].
 */
interface Atom : Species {
    /**
     *  Tag is cloned.
     */
    abstract override fun clone(): Atom

    /**
     *  Singleton list containing itself.
     */
    override fun atoms(): List<Atom> =
        listOf(this)

    var position: Vector3D

    /**
     *  Element.
     */
    val element: Element

    /**
     *  Charge associated with this atom.
     *
     *  Interpretation depends on the context that the atom is in.
     */
    var charge: Double?

    /**
     *  Arbitrary integer tag.
     */
    var tag: Int

    /**
     *  Island that represents this atom.
     *
     *  Two islands are referentially equal if and only if the two atoms are
     *  referentially equal.
     *
     *  @param A
     *      Type of this atom that the returned island is representing.
     *
     *  @return
     *      Island representing this atom.
     */
    fun <A : Atom> getIsland(): Island<A>

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
         *  @param charge
         *      Charge associated with the atom.
         *
         *  @param tag
         *      Arbitrary integer tag.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            position: Vector3D,
            charge: Double?,
            tag: Int
        ): Atom =
            Atom(
                element,
                position,
                charge,
                tag
            )

        /**
         *  Constructs an [Atom] with a tag value of `0`.
         *
         *  @param element
         *      Element of the atom.
         *
         *  @param position
         *      Position of the atom.
         *
         *  @param charge
         *      Charge associated with the atom.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            position: Vector3D,
            charge: Double?
        ): Atom = newInstance(
            element,
            position,
            charge,
            0
        )
    }
}

/**
 *  Constructs a new instance of [Atom].
 *
 *  See [Atom.newInstance] for description.
 */
fun Atom(
    element: Element,
    position: Vector3D,
    charge: Double?,
    tag: Int = 0
): Atom =
    AtomImpl(
        element,
        position,
        charge,
        tag
    )
