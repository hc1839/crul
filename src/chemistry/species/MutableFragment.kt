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

import chemistry.species.base.Atom as AtomIntf
import chemistry.species.base.MutableFragment as MutableFragmentIntf

/**
 *  Implementation of [chemistry.species.base.MutableFragment].
 */
open class MutableFragment<A, F> :
    MutableBasicFragment<A, F>,
    MutableFragmentIntf<A, F>
    where A : AbstractAtom<A>,
          F : AbstractFragment<A, F>
{
    @JvmOverloads
    constructor(
        name: String = uuid.Generator.inNCName()
    ): super(name)

    /**
     *  Copy constructor.
     *
     *  Atoms are cloned.
     */
    constructor(other: MutableFragment<A, F>): super(other)

    override fun clone(): MutableFragment<A, F> =
        MutableFragment<A, F>(this)
}
