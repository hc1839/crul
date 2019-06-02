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
 *  Mutable builder for [MoleculeComplex].
 *
 *  This builder can be cloned, which is useful in building complexes that
 *  differ from each other only in some regions.
 *
 *  To construct an instance of this class, use [newInstance].
 */
open class MoleculeComplexBuilder<B : MoleculeComplexBuilder<B>> : Cloneable {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    /**
     *  Backing property for the bonds in the complex.
     */
    private val bonds: MutableSet<Bond<*>> = mutableSetOf()

    /**
     *  Backing property for the atoms.
     */
    private val atoms: MutableSet<Atom> = mutableSetOf()

    protected constructor()

    /**
     *  Copy constructor.
     *
     *  Atoms and bonds are cloned.
     */
    constructor(other: MoleculeComplexBuilder<B>) {
        this.bonds.addAll(other.bonds.map { it.clone() })
        this.atoms.addAll(other.atoms.map { it.clone() })
        this.id = other.id
    }

    protected var id: String? = null
        private set

    /**
     *  Configures the identifier.
     *
     *  If it is not set, UUID Version 4 is used.
     */
    fun setId(value: String?): B {
        id = value
        return _this
    }

    /**
     *  Adds a bond.
     */
    open fun addBond(newBond: Bond<*>): B {
        bonds.add(newBond)
        return _this
    }

    /**
     *  Removes a bond.
     */
    open fun removeBond(oldBond: Bond<*>): B {
        bonds.remove(oldBond)
        return _this
    }

    /**
     *  Adds an atom.
     */
    open fun addAtom(newAtom: Atom): B {
        atoms.add(newAtom)
        return _this
    }

    /**
     *  Removes an atom.
     */
    open fun removeAtom(oldAtom: Atom): B {
        atoms.remove(oldAtom)
        return _this
    }

    /**
     *  Removes all bonds and atoms from this builder.
     *
     *  Complex identifier is not modified.
     */
    protected fun clear(): B {
        bonds.clear()
        atoms.clear()

        return _this
    }

    /**
     *  Constructs a [MoleculeComplex] from the data in this builder.
     */
    open fun <A : Atom> build(): MoleculeComplex<A> {
        val molecules = BondAggregator
            .aggregate(
                @Suppress("UNCHECKED_CAST") (
                    bonds as Set<Bond<A>>
                )
            )
            .map { bondList ->
                Molecule.newInstance(bondList.toSet())
            }

        return MoleculeComplexImpl(
            molecules + atoms.toList(),
            id ?: crul.uuid.Generator.inNCName()
        )
    }

    /**
     *  Clones this builder.
     *
     *  Atoms and bonds are cloned.
     */
    public override fun clone(): MoleculeComplexBuilder<B> =
        MoleculeComplexBuilder(this)

    companion object {
        private class MoleculeComplexBuilderImpl() :
            MoleculeComplexBuilder<MoleculeComplexBuilderImpl>()

        /**
         *  Creates an instance of [MoleculeComplexBuilder].
         */
        @JvmStatic
        fun newInstance(): MoleculeComplexBuilder<*> =
            MoleculeComplexBuilderImpl()
    }
}
