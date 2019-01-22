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
 *  Complex containing minimal information with read access.
 */
interface BasicComplex<A, F> : BasicAtomCollection<A>
    where A : BasicAtom<A>,
          F : BasicFragment<A, F>
{
    /**
     *  Fragments of this complex.
     *
     *  Fragments are returned in the same order between calls.
     *
     *  The meaning of fragment and whether it is stored or dynamically
     *  constructed are implementation-specific. If the fragments are
     *  dynamically constructor, there is no guarantee that the fragment names
     *  are the same between calls.
     */
    val fragments: List<F>

    /**
     *  Whether a fragment with a given name exists in this complex.
     *
     *  If the fragment names change between calls for an implementation, the
     *  implementation should raise an exception, since `fragmentName` is no
     *  longer defined.
     */
    fun containsFragment(fragmentName: String): Boolean

    /**
     *  Gets a fragment by name.
     *
     *  If there is no such fragment, `null` is returned.
     *
     *  If the fragment names change between calls for an implementation, the
     *  implementation should raise an exception, since `fragmentName` is no
     *  longer defined.
     */
    fun getFragmentByName(fragmentName: String): F?
}
