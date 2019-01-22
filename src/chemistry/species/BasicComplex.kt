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

import chemistry.species.base.BasicAtom as BasicAtomIntf
import chemistry.species.base.BasicFragment as BasicFragmentIntf
import chemistry.species.graph.ComplexGraphFactory

/**
 *  Implementation of [chemistry.species.base.BasicComplex].
 */
open class BasicComplex<A, F> : AbstractBasicComplex<A, F>
    where A : AbstractBasicAtom<A>,
          F : AbstractBasicFragment<A, F>
{
    protected constructor(
        fragments: Iterable<F>,
        name: String,
        indexerFactory: ComplexGraphFactory
    ): super(name, indexerFactory)
    {
        for (fragment in fragments) {
            addFragment(fragment)
        }
    }

    /**
     *  @param fragments
     *      Fragments to add to the new complex.
     *
     *  @param name
     *      Name of the complex.
     */
    @JvmOverloads
    constructor(
        fragments: Iterable<F>,
        name: String = uuid.Generator.inNCName()
    ): this(
        fragments,
        name,
        ComplexGraphFactory()
    )

    /**
     *  Copy constructor.
     *
     *  Fragments and atoms are cloned.
     */
    constructor(other: BasicComplex<A, F>): super(other)

    /**
     *  Deserialization constructor.
     */
    constructor(
        msgpack: ByteArray,
        atomFactory: (ByteArray) -> A,
        fragmentFactory: (ByteArray, (ByteArray) -> A) -> F
    ): super(msgpack, atomFactory, fragmentFactory)

    override fun clone(): BasicComplex<A, F> =
        BasicComplex<A, F>(this)
}
