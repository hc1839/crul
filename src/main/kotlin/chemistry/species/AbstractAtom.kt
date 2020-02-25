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
 *  Skeletal implementation of [Atom].
 *
 *  @constructor
 */
abstract class AbstractAtom constructor(
    override val element: Element,
    override val position: Vector3D
) : Atom
{
    private var _island: AtomIsland<*>? = null

    override fun <A : Atom> getIsland(): AtomIsland<A> {
        if (_island == null) {
            _island = AtomIsland(this)
        }

        @Suppress("UNCHECKED_CAST")
        return _island!! as AtomIsland<A>
    }
}
