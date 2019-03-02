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

package crul.chemistry.species.impl

import org.msgpack.core.MessagePack
import org.msgpack.value.Value

import crul.chemistry.species.AbstractAtom
import crul.chemistry.species.Atom
import crul.chemistry.species.Element
import crul.math.coordsys.Vector3D

/**
 *  Default implementation of [Atom].
 */
internal class AtomImpl : AbstractAtom {
    /**
     *  See [crul.chemistry.species.AtomFactory.create] for the description.
     */
    constructor(
        element: Element,
        position: Vector3D,
        formalCharge: Double,
        id: String = crul.uuid.Generator.inNCName()
    ): super(
        element,
        position,
        formalCharge,
        id
    )

    /**
     *  Copy constructor.
     */
    constructor(other: AtomImpl): super(other)

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): super(msgpack)

    override fun clone(): Atom =
        AtomImpl(this)
}
