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
 *  Mutable builder for [Atom].
 *
 *  To create an instance, use [create].
 */
open class AtomBuilder<B : AtomBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    protected var _element: Element? = null
        private set

    /**
     *  Configures the element.
     *
     *  It must be set before [build] is called.
     */
    fun element(value: Element): B {
        _element = value
        return _this
    }

    protected var _name: String? = null
        private set

    /**
     *  Configures the name.
     *
     *  If `null`, the default argument is used.
     */
    fun name(value: String?): B {
        _name = value
        return _this
    }

    protected var _position: Vector3D? = null
        private set

    /**
     *  Configures the position.
     *
     *  It must be set before [build] is called.
     */
    fun position(value: Vector3D): B {
        _position = value
        return _this
    }

    protected var _formalCharge: Double? = null
        private set

    /**
     *  Configures the formal charge.
     *
     *  It must be set before [build] is called.
     */
    fun formalCharge(value: Double): B {
        _formalCharge = value
        return _this
    }

    /**
     *  Constructs an [Atom] from the data in this builder.
     */
    open fun build(): Atom =
        object : AbstractAtom(
            _element!!,
            _position!!,
            _formalCharge!!,
            _name ?: uuid.Generator.inNCName()
        ) {
            override fun clone(): Atom =
                AtomBuilder
                    .create()
                    .element(this.element)
                    .position(this.position)
                    .formalCharge(this.formalCharge)
                    .name(this.name)
                    .build()
        }

    companion object {
        private class AtomBuilderImpl(): AtomBuilder<AtomBuilderImpl>()

        /**
         *  Creates an instance of [AtomBuilder].
         */
        @JvmStatic
        fun create(): AtomBuilder<*> =
            AtomBuilderImpl()
    }
}