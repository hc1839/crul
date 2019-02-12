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

package chemistry.species.sandbox

import chemistry.species.Element
import math.coordsys.Vector3D

/**
 *  Interface for an atom.
 *
 *  An atom is a singleton [Species].
 */
interface Atom : Species, Cloneable {
    /**
     *  Element.
     */
    val element: Element

    /**
     *  Arbitrary name.
     *
     *  It must conform to XML NCName production.
     */
    val name: String

    /**
     *  Position of the center.
     */
    var position: Vector3D

    /**
     *  Formal charge.
     */
    var formalCharge: Double

    public abstract override fun clone(): Atom
}
