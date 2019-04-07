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

import java.nio.ByteBuffer

import crul.chemistry.species.Element
import crul.math.coordsys.Vector3D

/**
 *  Interface for an atom.
 *
 *  An atom is a singleton [Species].
 *
 *  To construct an instance of this class, use [newInstance].
 */
interface Atom : Species {
    /**
     *  Singleton collection containing itself.
     */
    override fun atoms(): Collection<Atom> =
        listOf(this)

    /**
     *  Element.
     */
    val element: Element

    /**
     *  Position of the center.
     */
    var position: Vector3D

    /**
     *  Formal charge.
     */
    var formalCharge: Double

    /**
     *  Identifier for this atom.
     *
     *  It must conform to XML NCName production.
     */
    val id: String

    abstract override fun clone(): Atom

    companion object {
        /**
         *  Constructs an [Atom].
         *
         *  @param element
         *      Element of the atom.
         *
         *  @param position
         *      Position of the center of the atom.
         *
         *  @param formalCharge
         *      Formal charge of the atom.
         *
         *  @param id
         *      Identifier for this atom. It must conform to XML NCName
         *      production.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            position: Vector3D,
            formalCharge: Double,
            id: String
        ): Atom = AtomImpl(
            element,
            position,
            formalCharge,
            id
        )

        /**
         *  Constructs an [Atom] using an automatically generated UUID as the
         *  identifer.
         *
         *  @param element
         *      Element of the atom.
         *
         *  @param position
         *      Position of the center of the atom.
         *
         *  @param formalCharge
         *      Formal charge of the atom.
         */
        @JvmStatic
        fun newInstance(
            element: Element,
            position: Vector3D,
            formalCharge: Double
        ): Atom = newInstance(
            element,
            position,
            formalCharge,
            crul.uuid.Generator.inNCName()
        )
    }
}
