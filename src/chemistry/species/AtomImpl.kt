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

import crul.math.coordsys.Vector3D

/**
 *  Default implementation of [Atom].
 */
internal class AtomImpl : AbstractAtom {
    /**
     *  See [crul.chemistry.species.Atom.newInstance] for the description.
     */
    constructor(
        element: Element,
        position: Vector3D,
        charge: Double?,
        tag: Int = 0
    ): super(
        element,
        position,
        charge,
        tag
    )

    /**
     *  Copy constructor.
     */
    @JvmOverloads
    constructor(
        other: AtomImpl,
        tag: Int = other.tag
    ): super(other, tag)

    override fun clone(): Atom =
        AtomImpl(this)
}
