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
 *  Fragment containing minimal information with read access.
 */
interface BasicFragment<A, F> : BasicComplex<A, F>
    where A : BasicAtom<A>,
          F : BasicFragment<A, F>
{
    /**
     *  Returns itself.
     */
    override val fragments: List<F>
        get() = listOf(
            @Suppress("UNCHECKED_CAST")
            (this as F)
        )

    /**
     *  `true` if `fragmentName` matches the name of this fragment.
     */
    override fun containsFragment(fragmentName: String): Boolean =
        fragmentName == name

    /**
     *  Returns itself if `fragmentName` matches the name of this fragment;
     *  `null` if otherwise.
     */
    override fun getFragmentByName(fragmentName: String): F? =
        if (fragmentName == name) {
            @Suppress("UNCHECKED_CAST")
            (this as F)
        } else {
            null
        }
}
