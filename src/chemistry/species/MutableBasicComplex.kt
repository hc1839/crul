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

import chemistry.species.base.BasicFragment as BasicFragmentIntf
import chemistry.species.base.MutableBasicComplex as MutableBasicComplexIntf
import chemistry.species.graph.ComplexGraphFactory

/**
 *  Implementation of [chemistry.species.base.MutableBasicComplex].
 */
open class MutableBasicComplex<A, F> :
    AbstractBasicComplex<A, F>,
    MutableBasicComplexIntf<A, F>
    where A : AbstractBasicAtom<A>,
          F : AbstractBasicFragment<A, F>
{
    /**
     *  @param name
     *      Name of the complex.
     *
     *  @param indexerFactory
     *      Factory for the indexer to use.
     */
    protected constructor(
        name: String,
        indexerFactory: ComplexGraphFactory
    ): super(name, indexerFactory)

    /**
     *  @param name
     *      Name of the complex.
     */
    @JvmOverloads
    constructor(name: String = uuid.Generator.inNCName()):
        this(name, ComplexGraphFactory())

    /**
     *  Copy constructor.
     *
     *  Fragments and atoms are cloned.
     */
    constructor(other: MutableBasicComplex<A, F>): super(other)

    override fun add(fragment: F) {
        addFragment(fragment)
    }

    override fun remove(fragmentName: String) {
        indexer.removeFragmentEdge(fragmentName)
    }

    override fun clone(): MutableBasicComplex<A, F> =
        MutableBasicComplex<A, F>(this)
}
