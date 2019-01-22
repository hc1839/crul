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
import java.lang.ref.WeakReference

/**
 *  Bond in a molecule complex.
 */
class Bond<A, F> : AbstractFragment<A, F>
    where A : AbstractAtom<A>,
          F : AbstractFragment<A, F>
{
    /**
     *  Bond order as an arbitrary string.
     */
    var order: String
        set(value) {
            val atomNames = atoms.map { it.name }

            parentComplex.setBondOrder(
                Friendship,
                atomNames[0],
                atomNames[1],
                value
            )

            field = value
        }

    /**
     *  Weak reference to the parent complex.
     */
    private val parentComplexRef: WeakReference<MoleculeComplex<A, F>>

    /**
     *  @suppress
     *
     *  @param atom1
     *      One of the two atoms.
     *
     *  @param atom2
     *      Other atom.
     *
     *  @param order
     *      Bond order as an arbitrary string.
     *
     *  @param name
     *      Name of the bond.
     *
     *  @param parentComplex
     *      Complex that contains this bond.
     */
    constructor(
        @Suppress("UNUSED_PARAMETER")
        friendAccess: MoleculeComplex.FriendAccess,
        atom1: A,
        atom2: A,
        order: String,
        name: String,
        parentComplex: MoleculeComplex<A, F>
    ): super(listOf(atom1, atom2), name)
    {
        this.parentComplexRef = WeakReference(parentComplex)
        this.order = order
    }

    /**
     *  Copy constructor.
     *
     *  Atoms are cloned.
     */
    constructor(other: Bond<A, F>): super(other) {
        this.parentComplexRef = other.parentComplexRef
        this.order = other.order
    }

    override fun clone(): Bond<A, F> =
        Bond<A, F>(this)

    /**
     *  Complex that contains this bond.
     */
    val parentComplex: MoleculeComplex<A, F>
        get() {
            val referent = parentComplexRef.get()

            if (referent == null) {
                throw RuntimeException(
                    "Parent complex no longer exists."
                )
            }

            return referent
        }

    private object FriendKey : visaccess.FriendKey()

    open class FriendAccess(key: visaccess.FriendKey) :
        visaccess.FriendAccess
    {
        init {
            if (key != FriendKey) {
                throw IllegalArgumentException(
                    "Invalid friend key."
                )
            }
        }
    }

    private object Friendship : FriendAccess(FriendKey)
}
