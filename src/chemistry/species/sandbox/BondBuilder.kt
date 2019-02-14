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

/**
 *  Mutable builder for [Bond].
 */
open class BondBuilder<B : BondBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    protected var _atom1: Atom? = null
        private set

    /**
     *  Configures the first atom.
     *
     *  It must be set before [build] is called.
     */
    fun atom1(value: Atom): B {
        _atom1 = value
        return _this
    }

    protected var _atom2: Atom? = null
        private set

    /**
     *  Configures the second atom.
     *
     *  It must be set before [build] is called.
     */
    fun atom2(value: Atom): B {
        _atom2 = value
        return _this
    }

    protected var _order: String? = null
        private set

    /**
     *  Configures the bond order.
     *
     *  It must be set before [build] is called.
     */
    fun order(value: String): B {
        _order = value
        return _this
    }

    /**
     *  Constructs a [Bond] from the data in this builder.
     *
     *  @param A
     *      Type of atoms in this bond. The given atoms are casted to this type
     *      during construction.
     */
    open fun <A : Atom> build(): Bond<A> =
        @Suppress("UNCHECKED_CAST")
        BondImpl<A>(
            _atom1!! as A,
            _atom2!! as A,
            _order!!
        )

    companion object {
        private class BondBuilderImpl() :
            BondBuilder<BondBuilderImpl>()

        /**
         *  Creates an instance of [BondBuilder].
         */
        @JvmStatic
        fun create(): BondBuilder<*> =
            BondBuilderImpl()
    }
}
