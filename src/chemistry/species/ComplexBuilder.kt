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

/**
 *  Mutable builder for [Complex].
 *
 *  To construct an instance of this class, use [newInstance].
 */
open class ComplexBuilder<B : ComplexBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    protected var id: String? = null
        private set

    protected val subspeciesSet: MutableSet<Species> = mutableSetOf()

    /**
     *  Adds a subspecies.
     */
    fun add(subspecies: Species): B {
        subspeciesSet.add(subspecies)
        return _this
    }

    /**
     *  Adds to this builder all of the subspecies specified in a collection.
     */
    fun addAll(subspeciesCollection: Collection<Species>): B {
        subspeciesSet.addAll(subspeciesCollection)
        return _this
    }

    /**
     *  Removes a subspecies.
     */
    fun remove(subspecies: Species): B {
        subspeciesSet.remove(subspecies)
        return _this
    }

    /**
     *  Removes from this builder all of the subspecies specified in a
     *  collection.
     */
    fun removeAll(subspeciesCollection: Collection<Species>): B {
        subspeciesSet.removeAll(subspeciesCollection)
        return _this
    }

    /**
     *  Removes all subspecies from this builder.
     *
     *  Identifier remains intact.
     */
    fun clear(): B {
        subspeciesSet.clear()
        return _this
    }

    /**
     *  Constructs a [Complex] from the data in this builder.
     */
    open fun <S : Species> build(): Complex<S> =
        @Suppress("UNCHECKED_CAST")
        ComplexImpl<S>(subspeciesSet.toSet() as Collection<S>)

    companion object {
        private class ComplexBuilderImpl():
            ComplexBuilder<ComplexBuilderImpl>()

        /**
         *  Creates an instance of [ComplexBuilder].
         */
        @JvmStatic
        fun newInstance(): ComplexBuilder<*> =
            ComplexBuilderImpl()
    }
}
