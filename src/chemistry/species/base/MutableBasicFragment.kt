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
 *  Mutable version of [BasicFragment].
 */
interface MutableBasicFragment<A, F> : BasicFragment<A, F>
    where A : BasicAtom<A>,
          F : BasicFragment<A, F>
{
    /**
     *  Adds an atom to this fragment.
     */
    fun add(atom: A)

    /**
     *  Removes an atom from this fragment.
     */
    fun remove(atomName: String)
}
