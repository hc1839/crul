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
 *  To construct an instance of this class, use [newInstance].
 */
open class MoleculeComplexBuilder<B : MoleculeComplexBuilder<B>> {
    /*
     *  TODO: Make this builder cloneable, so that it can be used in building
     *  complexes that differ from each other only in some regions.
     */

    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    /**
     *  Backing property for the bonds in the complex.
     */
    private val wrappedBonds: MutableSet<SpeciesSetElement<Bond<*>>> =
        mutableSetOf()

    /**
     *  Backing property for the atoms.
     */
    private val wrappedAtoms: MutableSet<SpeciesSetElement<Atom>> =
        mutableSetOf()

    protected constructor()

    /**
     *  Adds a bond.
     */
    open fun addBond(newBond: Bond<*>): B {
        wrappedBonds.add(SpeciesSetElement(newBond))
        return _this
    }

    /**
     *  Removes a bond.
     */
    open fun removeBond(oldBond: Bond<*>): B {
        wrappedBonds.remove(SpeciesSetElement(oldBond))
        return _this
    }

    /**
     *  Adds an atom.
     */
    open fun addAtom(newAtom: Atom): B {
        wrappedAtoms.add(SpeciesSetElement(newAtom))
        return _this
    }

    /**
     *  Removes an atom.
     */
    open fun removeAtom(oldAtom: Atom): B {
        wrappedAtoms.remove(SpeciesSetElement(oldAtom))
        return _this
    }

    /**
     *  Removes all bonds and atoms from this builder.
     *
     *  Complex identifier is not modified.
     */
    protected fun clear(): B {
        wrappedBonds.clear()
        wrappedAtoms.clear()

        return _this
    }

    /**
     *  Constructs a [MoleculeComplex] from the data in this builder.
     */
    open fun <A : Atom> build(): MoleculeComplex<A> {
        val molecules = BondAggregator
            .aggregate(
                wrappedBonds.map {
                    @Suppress("UNCHECKED_CAST") (
                        it.species as Bond<A>
                    )
                }
            )
            .map { bondList ->
                Molecule.newInstance(bondList)
            }

        return MoleculeComplexImpl(
            molecules + wrappedAtoms.map {
                @Suppress("UNCHECKED_CAST") (
                    it.species as A
                )
            }
        )
    }

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
