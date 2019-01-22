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

/**
 *  Template instantiation of [AbstractBasicFragment].
 */
class BasicFragment<A : AbstractBasicAtom<A>> :
    AbstractBasicFragment<A, BasicFragment<A>>
{
    /**
     *  See [AbstractBasicFragment] for a description of the arguments.
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
    constructor(other: BasicFragment<A>): super(other)

    /**
     *  Deserialization constructor.
     */
    constructor(
        msgpack: ByteArray,
        atomFactory: (ByteArray) -> A
    ): super(msgpack, atomFactory)

    override fun clone(): BasicFragment<A> =
        BasicFragment<A>(this)
}
