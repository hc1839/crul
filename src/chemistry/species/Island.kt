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

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import crul.serialize.AvroSimple

/**
 *  Interface for a group containing atoms of a molecule or a single atom.
 *
 *  @param A
 *      Type of atoms in this island.
 */
interface Island<A : Atom> : Fragment<A> {
    abstract override fun clone(deep: Boolean): Island<A>

    companion object {
        /**
         *  Constructs an [Island] of one atom.
         *
         *  @param atom
         *      Atom of the island.
         */
        @JvmStatic
        fun <A : Atom> newInstance(atom: A): Island<A> = object :
            AbstractFragment<A>(listOf(atom)),
            Island<A>
        {
            override fun clone(deep: Boolean): Island<A> =
                @Suppress("UNCHECKED_CAST")
                newInstance(
                    atoms().single().clone(deep) as A
                )
        }
    }
}
