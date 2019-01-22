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

package chemistry.species.base

import chemistry.species.Element

/**
 *  Atom containing minimal information with read access.
 */
interface BasicAtom<A : BasicAtom<A>> : BasicAtomCollection<A> {
    /**
     *  Element of this atom.
     */
    val element: Element

    /**
     *  Returns itself.
     */
    override val atoms: List<A>
        get() = listOf(
            @Suppress("UNCHECKED_CAST")
            (this as A)
        )

    /**
     *  `true` if `atomName` matches the name of this atom.
     */
    override fun containsAtom(atomName: String): Boolean =
        atomName == name

    /**
     *  Returns itself if `atomName` matches the name of this atom; `null` if
     *  otherwise.
     */
    override fun getAtomByName(atomName: String): A? =
        if (atomName == name) {
            @Suppress("UNCHECKED_CAST")
            (this as A)
        } else {
            null
        }
}
