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

/**
 *  Generic collection of [BasicAtom].
 *
 *  Atom names must be unique in a collection.
 */
interface BasicAtomCollection<A : BasicAtom<A>> {
    /**
     *  Name of this collection.
     */
    val name: String

    /**
     *  Atoms of this complex.
     *
     *  Atoms are returned in the same order between calls.
     */
    val atoms: List<A>

    /**
     *  Whether an atom with a given name exists in this complex.
     */
    fun containsAtom(atomName: String): Boolean

    /**
     *  Gets an atom by name.
     *
     *  If there is no such atom, `null` is returned.
     */
    fun getAtomByName(atomName: String): A?
}
