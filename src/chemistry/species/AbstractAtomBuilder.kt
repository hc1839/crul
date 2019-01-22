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

package chemistry.species

import math.coordsys.Vector3D

/**
 *  Abstract mutable builder for subclasses of [AbstractAtom].
 */
abstract class AbstractAtomBuilder<A, B> :
    AbstractBasicAtomBuilder<Atom, AtomBuilder>
    where A : AbstractAtom<A>,
          B : AbstractAtomBuilder<A, B>
{
    constructor(): super()

    /**
     *  Formal charge.
     *
     *  It cannot be `null` when [build] is called.
     */
    var formalCharge: Double? = null
        private set

    /**
     *  Configure the formal charge.
     */
    fun formalCharge(value: Double?): B {
        formalCharge = value

        @Suppress("UNCHECKED_CAST")
        return this as B
    }

    /**
     *  Centroid.
     *
     *  It cannot be `null` when [build] is called.
     */
    var centroid: Vector3D? = null
        private set

    /**
     *  Configure the centroid.
     */
    fun centroid(value: Vector3D?): B {
        centroid = value

        @Suppress("UNCHECKED_CAST")
        return this as B
    }
}
