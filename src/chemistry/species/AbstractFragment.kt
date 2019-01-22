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
import chemistry.species.base.Fragment as FragmentIntf

/**
 *  Skeletal implementation of [chemistry.species.base.Fragment].
 *
 *  All properties and member functions are concrete.
 *
 *  @param A
 *      Type parameter that is to be instantiated by a subclass.
 */
abstract class AbstractFragment<A, F> :
    AbstractBasicFragment<A, F>,
    FragmentIntf<A, F>
    where A : AbstractAtom<A>,
          F : AbstractFragment<A, F>
{
    /**
     *  @param atoms
     *      Atoms to include in the fragment.
     *
     *  @param name
     *      Name of the fragment.
     */
    @JvmOverloads
    constructor(
        atoms: Iterable<A>,
        name: String = uuid.Generator.inNCName()
    ): super(atoms, name)

    /**
     *  Copy constructor.
     */
    constructor(other: AbstractFragment<A, F>): super(other)

    /**
     *  Deserialization constructor.
     */
    constructor(
        msgpack: ByteArray,
        atomFactory: (ByteArray) -> A
    ): super(msgpack, atomFactory)
}
