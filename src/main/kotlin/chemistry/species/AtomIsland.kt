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

package crul.chemistry.species

/**
 *  Island containing one atom.
 *
 *  It should be used only by an instance of [Atom] to construct an island only
 *  once such that there is one-to-one correspondence with the atom that the
 *  island is representing.
 *
 *  @constructor
 *
 *  @param atom
 *      Atom that the island represents.
 */
class AtomIsland<A : Atom>(atom: A) :
    AbstractFragment<A>(listOf(atom)),
    Island<A>
{
    override fun bonds(): Collection<Bond<A>> =
        listOf()

    override fun clone(): Island<A> =
        atoms().single().clone().getIsland<A>()
}
