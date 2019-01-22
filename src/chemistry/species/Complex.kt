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

import chemistry.species.base.Complex as ComplexIntf
import chemistry.species.base.Fragment as FragmentIntf
import chemistry.species.graph.ComplexGraphFactory

/**
 *  Implementation of [chemistry.species.base.Complex].
 */
open class Complex<A, F> :
    BasicComplex<A, F>,
    ComplexIntf<A, F>
    where A : AbstractAtom<A>,
          F : AbstractFragment<A, F>
{
    protected constructor(
        fragments: Iterable<F>,
        name: String,
        indexerFactory: ComplexGraphFactory
    ): super(fragments, name, indexerFactory)

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
    constructor(other: Complex<A, F>): super(other)

    /**
     *  Deserialization constructor.
     */
    constructor(
        msgpack: ByteArray,
        atomFactory: (ByteArray) -> A,
        fragmentFactory: (ByteArray, (ByteArray) -> A) -> F
    ): super(msgpack, atomFactory, fragmentFactory)

    override fun clone(): Complex<A, F> =
        Complex<A, F>(this)
}
