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

import crul.chemistry.species.Element
import crul.math.coordsys.Vector3D

/**
 *  Factory for [Atom].
 */
open class AtomFactory {
    /**
     *  Builder for construction.
     */
    private val builder: AtomBuilder<*> by lazy {
        AtomBuilder.newInstance()
    }

    constructor()

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
     *      Identifier for this atom. It must conform to XML NCName production.
     */
    @JvmOverloads
    open fun create(
        element: Element,
        position: Vector3D,
        formalCharge: Double,
        id: String = crul.uuid.Generator.inNCName()
    ): Atom =
        builder
            .setElement(element)
            .setPosition(position)
            .setFormalCharge(formalCharge)
            .setId(id)
            .build()
}
