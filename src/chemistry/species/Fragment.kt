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
 *  Template instantiation of [AbstractFragment].
 */
class Fragment<A : AbstractAtom<A>> :
    AbstractFragment<A, Fragment<A>>
{
    /**
     *  See [AbstractFragment] for a description of the arguments.
     */
    @JvmOverloads
    constructor(
        atoms: Iterable<A>,
        name: String = uuid.Generator.inNCName()
    ): super(atoms, name)

    /**
     *  Copy constructor.
     *
     *  Atoms are cloned.
     */
    constructor(other: Fragment<A>): super(other)

    /**
     *  Deserialization constructor.
     */
    constructor(
        msgpack: ByteArray,
        atomFactory: (ByteArray) -> A
    ): super(msgpack, atomFactory)

    override fun clone(): Fragment<A> =
        Fragment<A>(this)
}
