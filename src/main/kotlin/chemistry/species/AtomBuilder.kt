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
 *  Mutable builder for [Atom].
 *
 *  To construct an instance of this class, use [newInstance].
 */
open class AtomBuilder<B : AtomBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    protected var element: Element? = null
        private set

    /**
     *  Configures the element.
     *
     *  It must be set before [build] is called.
     */
    fun setElement(value: Element): B {
        element = value
        return _this
    }

    protected var centroid: Vector3D? = null
        private set

    /**
     *  Configures the centroid.
     *
     *  It must be set before [build] is called.
     */
    fun setCentroid(value: Vector3D): B {
        centroid = value
        return _this
    }

    protected var formalCharge: Double? = null
        private set

    /**
     *  Configures the formal charge.
     *
     *  It must be set before [build] is called.
     */
    fun setFormalCharge(value: Double): B {
        formalCharge = value
        return _this
    }

    protected var tag: Int? = null
        private set

    /**
     *  Configures the tag.
     *
     *  If not set, `0` is used.
     */
    fun setTag(value: Int?): B {
        tag = value
        return _this
    }

    /**
     *  Constructs an [Atom] from the data in this builder.
     */
    open fun build(): Atom =
        Atom(
            element!!,
            centroid!!,
            formalCharge!!,
            tag ?: 0
        )

    companion object {
        private class AtomBuilderImpl():
            AtomBuilder<AtomBuilderImpl>()

        /**
         *  Creates an instance of [AtomBuilder].
         */
        @JvmStatic
        fun newInstance(): AtomBuilder<*> =
            AtomBuilderImpl()
    }
}
