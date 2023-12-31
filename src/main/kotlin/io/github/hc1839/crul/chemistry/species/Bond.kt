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

package io.github.hc1839.crul.chemistry.species

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.*

import io.github.hc1839.crul.serialize.AvroSimple

/**
 *  Interface for a bond in a molecule.
 *
 *  Atoms in a bond are not referentially equal to each other.
 *
 *  To construct an instance of this class, use [newInstance].
 *
 *  @param A
 *      Type of atoms.
 */
interface Bond<A : Atom> : Fragment<A> {
    /**
     *  Bond type as an aribtrary string.
     */
    val bondType: String

    /**
     *  Atoms as a pair in the given order.
     */
    fun toAtomPair(): Pair<A, A>

    companion object {
        /**
         *  Constructs a [Bond].
         *
         *  If the given atoms are equal or have the same identifier, an
         *  exception is raised.
         *
         *  @param atom1
         *      First atom.
         *
         *  @param atom2
         *      Second atom.
         *
         *  @param bondType
         *      Bond type as an arbitrary string.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            atom1: A,
            atom2: A,
            bondType: String
        ): Bond<A> =
            Bond(atom1, atom2, bondType)
    }
}

/**
 *  Constructs a new instance of [Bond].
 *
 *  See [Bond.newInstance] for description.
 */
fun <A : Atom> Bond(atom1: A, atom2: A, bondType: String): Bond<A> =
    BondImpl(atom1, atom2, bondType)
