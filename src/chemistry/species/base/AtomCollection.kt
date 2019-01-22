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

package chemistry.species.base

import math.coordsys.Vector3D

/**
 *  Generic collection of [Atom].
 */
interface AtomCollection<A : Atom<A>> : BasicAtomCollection<A> {
    /**
     *  Formal charge of this collection, which is defined as the sum of the
     *  formal charges of the atoms.
     *
     *  An exception is raised if there are no atoms in this complex.
     *
     *  It is a concrete property.
     */
    val formalCharge: Double
        get() {
            val atomsBuf = atoms

            if (atomsBuf.count() == 0) {
                throw RuntimeException("No atoms.")
            }

            return atomsBuf
                .map { it.formalCharge }
                .reduce { acc, item -> acc + item }
        }

    /**
     *  Centroid of this collection, which is defined as the centroid of the
     *  group of atoms.
     *
     *  Setting the centroid is equivalent to translating the atoms such that
     *  the new centroid is the given centroid.
     *
     *  An exception is raised if there are no atoms in this complex.
     *
     *  It is a concrete property.
     */
    var centroid: Vector3D
        get() {
            val atomsBuf = atoms

            return atomsBuf
                .map { it.centroid }
                .reduce { acc, item -> acc + item} /
                    atomsBuf.count().toDouble()
        }
        set(value) {
            val atomsBuf = atoms

            if (atomsBuf.count() == 0) {
                throw RuntimeException("No atoms in this species.")
            }

            val centroidBuf = centroid

            for (atom in atomsBuf) {
                atom.translate(value - centroidBuf)
            }
        }

    /**
     *  Translates the atoms by `displacement`.
     *
     *  It is a concrete function.
     */
    fun translate(displacement: Vector3D) {
        centroid += displacement
    }
}
