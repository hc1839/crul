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

import chemistry.species.Element

/**
 *  Template instantiation of [AbstractBasicAtom].
 */
class BasicAtom : AbstractBasicAtom<BasicAtom> {
    /**
     *  See [AbstractBasicAtom] for description of the arguments.
     */
    @JvmOverloads
    constructor(element: Element, name: String = uuid.Generator.inNCName()):
        super(element, name)

    /**
     *  Copy constructor.
     */
    constructor(other: BasicAtom): super(other)

    /**
     *  Copy constructor using a different atom name.
     */
    constructor(other: BasicAtom, name: String): super(other, name)

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): super(msgpack)

    override fun clone(): BasicAtom =
        BasicAtom(this)
}
