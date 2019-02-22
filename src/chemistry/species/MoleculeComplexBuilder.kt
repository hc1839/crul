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

import crul.chemistry.species.impl.MoleculeComplexImpl
import crul.chemistry.species.impl.MoleculeImpl
import crul.hypergraph.Edge
import crul.hypergraph.Graph
import crul.hypergraph.GraphSystem
import crul.hypergraph.Vertex

/**
 *  Mutable builder for [MoleculeComplex].
 *
 *  An atom acting as a singleton molecule can be added or removed. Adding or
 *  removing bonds has no effect on the singleton molecules stored in this
 *  builder. However, when the complex is being built, bonds take precedence
 *  over atoms that are acting as singleton molecules. In other words, if a
 *  bond contains an atom that is added as a singleton molecule, the singleton
 *  molecule is ignored and not created.
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
    private val _bonds: MutableSet<Bond<Atom>> = mutableSetOf()

    /**
     *  Backing property for the atoms as singleton molecules in the complex.
     */
    private val _singletons: MutableSet<Atom> = mutableSetOf()

    protected constructor()

    /**
     *  Copy constructor.
     *
     *  Atoms and bonds are cloned.
     */
    constructor(other: MoleculeComplexBuilder<B>) {
        this._bonds.addAll(other._bonds.map { it.clone() })
        this._singletons.addAll(other._singletons.map { it.clone() })
    }

    /**
     *  Adds a bond.
     *
     *  Nothing is done if an equal bond already exists.
     *
     *  An exception is raised in the following scenarios.
     *      - An unequal bond with both atoms being equal to the atoms in the
     *        given bond, regardless of the equality of the bond order.
     *      - Another bond has an unequal atom with the same name or an equal
     *        atom with a different name compared to one of the atoms in the
     *        given bond.
     */
    open fun addBond(newBond: Bond<Atom>): B {
        if (_bonds.contains(newBond)) {
            return _this
        }

        // Atoms of the given bond.
        val newAtoms = newBond.toAtomPair().toList().toSet()

        // Sanity checks.
        for (existingBond in _bonds) {
            val existingAtoms = existingBond.toAtomPair().toList().toSet()

            if (newAtoms == existingAtoms) {
                throw IllegalArgumentException(
                    "Another bond has the same atoms."
                )
            }

            // Atoms of the existing bond associated by name.
            val existingAtomsByName = existingAtoms.associateBy { it.name }

            for (newAtom in newAtoms) {
                if (
                    existingAtomsByName.containsKey(newAtom.name) &&
                    newAtom != existingAtomsByName[newAtom.name]!!
                ) {
                    throw IllegalArgumentException(
                        "Different atom with the same name exists: " +
                        newAtom.name
                    )
                }

                val equivalentExistingAtom = existingAtoms
                    .find { it == newAtom }

                if (
                    equivalentExistingAtom != null &&
                    equivalentExistingAtom.name != newAtom.name
                ) {
                    throw IllegalArgumentException(
                        "Equivalent atoms have different names: " +
                        "'${equivalentExistingAtom.name}' and '${newAtom.name}'"
                    )
                }
            }
        }

        _bonds.add(newBond)

        return _this
    }

    /**
     *  Removes a bond.
     */
    open fun removeBond(oldBond: Bond<Atom>): B {
        _bonds.remove(oldBond)

        return _this
    }

    /**
     *  Adds an atom as a singleton molecule.
     *
     *  If an unequal atom with the same name or an equal atom with a different
     *  name exists (as a singleton molecule or as one of the atoms in a bond),
     *  an exception is raised.
     */
    open fun addAtom(newAtom: Atom): B {
        // All atoms in the complex.
        val existingAtomsAll =
            (
                _singletons.asSequence() +
                _bonds.asSequence().flatMap { it.atoms().asSequence() }
            )
            .distinct()

        // Sanity checks.
        for (existingAtom in existingAtomsAll) {
            if (
                newAtom.name == existingAtom.name &&
                newAtom != existingAtom
            ) {
                throw IllegalArgumentException(
                    "Different atom with the same name exists: " +
                    newAtom.name
                )
            }

            if (
                newAtom == existingAtom &&
                newAtom.name != existingAtom.name
            ) {
                throw IllegalArgumentException(
                    "Equivalent atoms have different names: " +
                    "'${existingAtom.name}' and '${newAtom.name}'"
                )
            }
        }

        _singletons.add(newAtom)

        return _this
    }

    /**
     *  Removes a singleton molecule.
     *
     *  If an unequal atom with the same name or an equal atom with a different
     *  name exists (only as a singleton molecule), an exception is raised.
     */
    open fun removeAtom(oldAtom: Atom): B {
        val existingAtomOfSameName = _singletons
            .find { it.name == oldAtom.name }

        if (
            existingAtomOfSameName != null &&
            existingAtomOfSameName != oldAtom
        ) {
            throw IllegalArgumentException(
                "Different atom with the same name exists: " +
                oldAtom.name
            )
        }

        val equivalentExistingAtom = _singletons
            .find { it == oldAtom }

        if (
            equivalentExistingAtom != null &&
            equivalentExistingAtom.name != oldAtom.name
        ) {
            throw IllegalArgumentException(
                "Equivalent atoms have different names: " +
                "'${equivalentExistingAtom.name}' and '${oldAtom.name}'"
            )
        }

        _singletons.remove(oldAtom)

        return _this
    }

    /**
     *  Groups the bonds and atoms into a set of [Complex] of [Species], which
     *  is either [Bond] or [Atom], respectively.
     *
     *  Each complex of species represents either the bonds or one atom of a
     *  molecule that is used for constructing an [AbstractMolecule].
     */
    protected fun groupSpecies(): Set<Complex<Species>> {
        val graphSystem = GraphSystem()
        val graph = graphSystem.createGraph(crul.uuid.Generator.inNCName())

        // Vertex acting as the edge type for a fragment.
        val fragmentEdgeType = graph.createVertex()

        // All atoms in the complex.
        val atomsAll =
            (
                _singletons.asSequence() +
                _bonds.asSequence().flatMap { it.atoms().asSequence() }
            )
            .distinct()

        // Populate the graph with atoms.
        for (atom in atomsAll) {
            // Create a vertex for the atom, and store the atom.
            val atomVertex = graph.createVertex()
            atomVertex.addName(atom.name)
            atomVertex.userData = atom

            // Create an edge representing a singleton fragment.
            val fragmentEdge = graph.createEdge(fragmentEdgeType)
            fragmentEdge.addVertex(atomVertex)

            // Create a proxy for the fragment to store its bonds.
            val fragmentEdgeProxy = graph.createVertex()
            fragmentEdge.proxy = fragmentEdgeProxy
            fragmentEdgeProxy.userData = mutableSetOf<Bond<Atom>>()
        }

        // Combine fragments based on atom connectivity.
        for (bond in _bonds) {
            val (atom1, atom2) = bond.toAtomPair()
            val atom1Vertex = graph.getVertexByName(atom1.name)!!
            val atom2Vertex = graph.getVertexByName(atom2.name)!!

            // Fragment that contains the first atom.
            val fragment1Edge = atom1Vertex
                .getEdgesByType(fragmentEdgeType)
                .single()

            // Fragment that contains the second atom.
            val fragment2Edge = atom2Vertex
                .getEdgesByType(fragmentEdgeType)
                .single()

            if (fragment1Edge == fragment2Edge) {
                // Atoms are already in the same fragment.

                val fragment1EdgeProxy = fragment1Edge.proxy!! as Vertex

                @Suppress("UNCHECKED_CAST")
                (fragment1EdgeProxy.userData as MutableSet<Bond<Atom>>)
                    .add(bond)
            } else {
                // Combine the two different fragments with the second fragment
                // as the arbitrary target.

                // Names of atoms in the first fragment.
                val migrantAtomNames = fragment1Edge
                    .vertices
                    .map { it.names.single() }

                val fragment1EdgeProxy = fragment1Edge.proxy!! as Vertex

                @Suppress("UNCHECKED_CAST")
                val fragment1Bonds = fragment1EdgeProxy
                    .userData as Set<Bond<Atom>>

                fragment1Edge.proxy = null
                fragment1Edge.remove()

                val fragment2EdgeProxy = fragment2Edge.proxy!! as Vertex

                @Suppress("UNCHECKED_CAST")
                val fragment2Bonds = fragment2EdgeProxy
                    .userData as MutableSet<Bond<Atom>>

                fragment2Bonds.add(bond)

                // Add bonds from the first fragment to the second.
                fragment2Bonds.addAll(fragment1Bonds)

                fragment1EdgeProxy.remove()

                // Add atom vertices from the first fragment edge to the
                // second.
                for (migrantAtomName in migrantAtomNames) {
                    val migrantAtomVertex = graph
                        .getVertexByName(migrantAtomName)!!

                    fragment2Edge.addVertex(migrantAtomVertex)
                }
            }
        }

        // Use the same builder for all complex constructions.
        val complexBuilder = ComplexBuilder.newInstance()

        // Construct a temporary sequence of complexes of bonds.
        val bondComplexes = graph
            .getEdgesByType(fragmentEdgeType)
            .asSequence()
            .filter { fragmentEdge ->
                fragmentEdge.vertices.count() > 1
            }
            .map { fragmentEdge ->
                @Suppress("UNCHECKED_CAST")
                val fragmentEdgeProxy = fragmentEdge.proxy!! as Vertex

                @Suppress("UNCHECKED_CAST")
                val bonds = fragmentEdgeProxy.userData as Set<Bond<Atom>>

                complexBuilder.clear()

                val complex = complexBuilder
                    .addAll(bonds)
                    .build<Species>()

                complexBuilder.clear()

                complex
            }

        // Construct a temporary sequence of complexes of atoms.
        val atomComplexes = graph
            .getEdgesByType(fragmentEdgeType)
            .asSequence()
            .filter { fragmentEdge ->
                fragmentEdge.vertices.count() == 1
            }
            .map { fragmentEdge ->
                val atomVertex = fragmentEdge.vertices.single()

                @Suppress("UNCHECKED_CAST")
                val atom = atomVertex.userData as Atom

                complexBuilder.clear()

                val complex = complexBuilder
                    .add(atom)
                    .build<Species>()

                complexBuilder.clear()

                complex
            }

        return (bondComplexes + atomComplexes).toSet()
    }

    /**
     *  Constructs a [MoleculeComplex] from the data in this builder.
     */
    open fun <A : Atom> build(): MoleculeComplex<A> =
        MoleculeComplexImpl(
            groupSpecies().map { speciesComplex ->
                when (speciesComplex.first()) {
                    is Bond<*> -> MoleculeImpl<A>(
                        speciesComplex
                            .map {
                                @Suppress("UNCHECKED_CAST")
                                it as Bond<A>
                            }
                            .toSet()
                    )

                    is Atom ->
                        @Suppress("UNCHECKED_CAST")
                        MoleculeImpl<A>(speciesComplex.first() as A)

                    else -> throw RuntimeException(
                        "[Internal Error] Unexpected subspecies type: " +
                        "${speciesComplex.first()::class.qualifiedName}"
                    )
                }
            }
        )

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
