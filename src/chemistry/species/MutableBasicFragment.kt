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
import chemistry.species.base.MutableBasicFragment as MutableBasicFragmentIntf

/**
 *  Implementation of [chemistry.species.base.MutableBasicFragment].
 */
open class MutableBasicFragment<A, F> :
    AbstractBasicFragment<A, F>,
    MutableBasicFragmentIntf<A, F>
    where A : AbstractBasicAtom<A>,
          F : AbstractBasicFragment<A, F>
{
    /**
     *
     *  @param name
     *      Name of the fragment. It must conform to XML NCName production.
     */
    @JvmOverloads
    constructor(
        name: String = uuid.Generator.inNCName()
    ): super(listOf<A>(), name)

    /**
     *  Copy constructor.
     *
     *  Atoms are cloned.
     */
    constructor(other: MutableBasicFragment<A, F>): super(other)

    /**
     *  Atoms associated by their names.
     */
    private val atomsByName: MutableMap<String, A> = mutableMapOf()

    override val atoms: List<A>
        get() = atomsByName.values.sortedBy { it.name }

    override fun containsAtom(atomName: String) =
        atomsByName.containsKey(atomName)

    override fun getAtomByName(atomName: String) =
        atomsByName[atomName]

    override fun add(atom: A) {
        if (containsAtom(atom.name)) {
            throw IllegalArgumentException(
                "Atom with the same name exists: ${atom.name}"
            )
        }

        atomsByName.put(atom.name, atom)
    }

    override fun remove(atomName: String) {
        atomsByName.remove(atomName)
    }

    override fun clone(): MutableBasicFragment<A, F> =
        MutableBasicFragment<A, F>(this)
}
