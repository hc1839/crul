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

import chemistry.species.base.Fragment as FragmentIntf
import chemistry.species.base.MutableComplex as MutableComplexIntf
import chemistry.species.graph.ComplexGraphFactory

/**
 *  Implementation of [chemistry.species.base.MutableComplex].
 */
open class MutableComplex<A, F> :
    MutableBasicComplex<A, F>,
    MutableComplexIntf<A, F>
    where A : AbstractAtom<A>,
          F : AbstractFragment<A, F>
{
    protected constructor(
        name: String,
        indexerFactory: ComplexGraphFactory
    ): super(name, indexerFactory)

    @JvmOverloads
    constructor(name: String = uuid.Generator.inNCName()):
        this(name, ComplexGraphFactory())

    /**
     *  Copy constructor.
     *
     *  Fragments and atoms are cloned.
     */
    constructor(other: MutableComplex<A, F>): super(other)

    override fun clone(): MutableComplex<A, F> =
        MutableComplex<A, F>(this)
}
