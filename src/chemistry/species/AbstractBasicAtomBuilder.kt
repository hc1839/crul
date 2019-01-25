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

import chemistry.species.Element

/**
 *  Abstract mutable builder for subclasses of [AbstractBasicAtom].
 */
abstract class AbstractBasicAtomBuilder<A, B>
    where A : AbstractBasicAtom<A>,
          B : AbstractBasicAtomBuilder<A, B>
{
    constructor()

    /**
     *  Element.
     *
     *  It cannot be null when [build] is called.
     */
    var element: Element? = null
        private set

    /**
     *  Configures the element.
     */
    fun element(value: Element?): B {
        element = value

        @Suppress("UNCHECKED_CAST")
        return this as B
    }

    /**
     *  Name.
     *
     *  If `null`, the default is used.
     */
    var name: String? = null
        private set

    /**
     *  Configures the name.
     */
    fun name(value: String?): B {
        name = value

        @Suppress("UNCHECKED_CAST")
        return this as B
    }

    /**
     *  Constructs an [AbstractBasicAtom] from the data in this builder.
     */
    abstract fun build(): A
}
