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
 *  Skeletal implementation of [Atom].
 */
abstract class AbstractAtom : Atom {
    override val element: Element

    override var position: Vector3D

    override var charge: Double?

    override var tag: Int

    private var _island: Island<*>? = null

    /**
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
    @JvmOverloads
    constructor(
        element: Element,
        position: Vector3D,
        charge: Double?,
        tag: Int = 0
    ) {
        this.element = element
        this.position = position
        this.charge = charge
        this.tag = tag
    }

    /**
     *  Copy constructor.
     */
    @JvmOverloads
    constructor(other: AbstractAtom, tag: Int = other.tag) {
        this.element = other.element
        this.position = other.position
        this.charge = other.charge
        this.tag = tag
    }

    override fun <A : Atom> getIsland(islandCharge: Int): Island<A> {
        if (_island == null) {
            _island = AtomIsland(islandCharge, this)
        }

        _island!!.charge = islandCharge

        @Suppress("UNCHECKED_CAST")
        return _island!! as Island<A>
    }
}
