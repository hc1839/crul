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

import chemistry.species.impl.ComplexImpl

/**
 *  Mutable builder for [Complex].
 *
 *  To construct an instance of this class, use [create].
 */
open class ComplexBuilder<B : ComplexBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    protected val _subspecies: MutableSet<Species> = mutableSetOf()

    /**
     *  Adds a subspecies.
     */
    fun add(subspecies: Species): B {
        _subspecies.add(subspecies)
        return _this
    }

    /**
     *  Adds to this builder all of the subspecies specified in a collection.
     */
    fun addAll(subspeciesCollection: Collection<Species>): B {
        _subspecies.addAll(subspeciesCollection)
        return _this
    }

    /**
     *  Removes a subspecies.
     */
    fun remove(subspecies: Species): B {
        _subspecies.remove(subspecies)
        return _this
    }

    /**
     *  Removes from this builder all of the subspecies specified in a
     *  collection.
     */
    fun removeAll(subspeciesCollection: Collection<Species>): B {
        _subspecies.removeAll(subspeciesCollection)
        return _this
    }

    /**
     *  Removes all subspecies from this builder.
     */
    fun clear(): B {
        _subspecies.clear()
        return _this
    }

    /**
     *  Constructs a [Complex] from the data in this builder.
     */
    open fun <S : Species> build(): Complex<S> =
        @Suppress("UNCHECKED_CAST")
        ComplexImpl<S>(_subspecies.toSet() as Iterable<S>)

    companion object {
        private class ComplexBuilderImpl():
            ComplexBuilder<ComplexBuilderImpl>()

        /**
         *  Creates an instance of [ComplexBuilder].
         */
        @JvmStatic
        fun create(): ComplexBuilder<*> =
            ComplexBuilderImpl()
    }
}