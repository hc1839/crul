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
    private val bondSet: MutableSet<Bond<*>> = mutableSetOf()

    /**
     *  Backing property for the atoms as singleton molecules in the complex.
     */
    private val singletonSet: MutableSet<Atom> = mutableSetOf()

    protected constructor()

    /**
     *  Copy constructor.
     *
     *  Atoms and bonds are cloned.
     */
    constructor(other: MoleculeComplexBuilder<B>) {
        this.bondSet.addAll(other.bondSet.map { it.clone() })
        this.singletonSet.addAll(other.singletonSet.map { it.clone() })
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
     *
     *  Nothing is done if an equal bond already exists.
     *
     *  An exception is raised in the following scenarios:
     *      - An unequal bond with both atoms being equal to the atoms in the
     *        given bond, regardless of the equality of the bond order.
     *      - Another bond has an unequal atom with the same ID or an equal
     *        atom with a different ID compared to one of the atoms in the given
     *        bond.
     */
    open fun addBond(newBond: Bond<*>): B {
        if (bondSet.contains(newBond)) {
            return _this
        }

        // Atoms of the given bond.
        val newAtoms = newBond.toAtomPair().toList().toSet()

        // Sanity checks.
        for (existingBond in bondSet) {
            val existingAtoms = existingBond.toAtomPair().toList().toSet()

            if (newAtoms == existingAtoms) {
                throw IllegalArgumentException(
                    "Another bond has the same atoms."
                )
            }

            // Atoms of the existing bond associated by ID.
            val existingAtomsById = existingAtoms.associateBy { it.id }

            for (newAtom in newAtoms) {
                if (
                    existingAtomsById.containsKey(newAtom.id) &&
                    newAtom != existingAtomsById[newAtom.id]!!
                ) {
                    throw IllegalArgumentException(
                        "Different atom with the same ID exists: " +
                        newAtom.id
                    )
                }

                val equivalentExistingAtom = existingAtoms
                    .find { it == newAtom }

                if (
                    equivalentExistingAtom != null &&
                    equivalentExistingAtom.id != newAtom.id
                ) {
                    throw IllegalArgumentException(
                        "Equivalent atoms have different IDs: " +
                        "'${equivalentExistingAtom.id}' and '${newAtom.id}'"
                    )
                }
            }
        }

        bondSet.add(newBond)

        return _this
    }

    /**
     *  Removes a bond.
     */
    open fun removeBond(oldBond: Bond<*>): B {
        bondSet.remove(oldBond)

        return _this
    }

    /**
     *  Adds an atom as a singleton molecule.
     *
     *  If an unequal atom with the same ID or an equal atom with a different
     *  ID exists (as a singleton molecule or as one of the atoms in a bond),
     *  an exception is raised.
     */
    open fun addAtom(newAtom: Atom): B {
        // All atoms in the complex.
        val existingAtomsAll =
            (
                singletonSet.asSequence() +
                bondSet.asSequence().flatMap { it.atoms().asSequence() }
            )
            .distinct()

        // Sanity checks.
        for (existingAtom in existingAtomsAll) {
            if (
                newAtom.id == existingAtom.id &&
                newAtom != existingAtom
            ) {
                throw IllegalArgumentException(
                    "Different atom with the same ID exists: " +
                    newAtom.id
                )
            }

            if (
                newAtom == existingAtom &&
                newAtom.id != existingAtom.id
            ) {
                throw IllegalArgumentException(
                    "Equivalent atoms have different IDs: " +
                    "'${existingAtom.id}' and '${newAtom.id}'"
                )
            }
        }

        singletonSet.add(newAtom)

        return _this
    }

    /**
     *  Removes a singleton molecule.
     *
     *  If an unequal atom with the same ID or an equal atom with a different
     *  ID exists (only as a singleton molecule), an exception is raised.
     */
    open fun removeAtom(oldAtom: Atom): B {
        val existingAtomOfSameId = singletonSet
            .find { it.id == oldAtom.id }

        if (
            existingAtomOfSameId != null &&
            existingAtomOfSameId != oldAtom
        ) {
            throw IllegalArgumentException(
                "Different atom with the same ID exists: " +
                oldAtom.id
            )
        }

        val equivalentExistingAtom = singletonSet
            .find { it == oldAtom }

        if (
            equivalentExistingAtom != null &&
            equivalentExistingAtom.id != oldAtom.id
        ) {
            throw IllegalArgumentException(
                "Equivalent atoms have different IDs: " +
                "'${equivalentExistingAtom.id}' and '${oldAtom.id}'"
            )
        }

        singletonSet.remove(oldAtom)

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
                singletonSet.asSequence() +
                bondSet.asSequence().flatMap { it.atoms().asSequence() }
            )
            .distinct()

        // Populate the graph with atoms.
        for (atom in atomsAll) {
            // Create a vertex for the atom, and store the atom.
            val atomVertex = graph.createVertex()
            atomVertex.addName(atom.id)
            atomVertex.userData = atom

            // Create an edge representing a singleton fragment.
            val fragmentEdge = graph.createEdge(fragmentEdgeType)
            fragmentEdge.addVertex(atomVertex)

            // Create a proxy for the fragment to store its bonds.
            val fragmentEdgeProxy = graph.createVertex()
            fragmentEdge.proxy = fragmentEdgeProxy
            fragmentEdgeProxy.userData = mutableSetOf<Bond<*>>()
        }

        // Combine fragments based on atom connectivity.
        for (bond in bondSet) {
            val (atom1, atom2) = bond.toAtomPair()
            val atom1Vertex = graph.getVertexByName(atom1.id)!!
            val atom2Vertex = graph.getVertexByName(atom2.id)!!

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
                (fragment1EdgeProxy.userData as MutableSet<Bond<*>>)
                    .add(bond)
            } else {
                // Combine the two different fragments with the second fragment
                // as the arbitrary target.

                // IDs of atoms in the first fragment.
                val migrantAtomIds = fragment1Edge
                    .vertices
                    .map { it.names.single() }

                val fragment1EdgeProxy = fragment1Edge.proxy!! as Vertex

                @Suppress("UNCHECKED_CAST")
                val fragment1Bonds = fragment1EdgeProxy
                    .userData as Set<Bond<*>>

                fragment1Edge.proxy = null
                fragment1Edge.remove()

                val fragment2EdgeProxy = fragment2Edge.proxy!! as Vertex

                @Suppress("UNCHECKED_CAST")
                val fragment2Bonds = fragment2EdgeProxy
                    .userData as MutableSet<Bond<*>>

                fragment2Bonds.add(bond)

                // Add bonds from the first fragment to the second.
                fragment2Bonds.addAll(fragment1Bonds)

                fragment1EdgeProxy.remove()

                // Add atom vertices from the first fragment edge to the
                // second.
                for (migrantAtomId in migrantAtomIds) {
                    val migrantAtomVertex = graph
                        .getVertexByName(migrantAtomId)!!

                    fragment2Edge.addVertex(migrantAtomVertex)
                }
            }
        }

        // Use the same builder for all complex constructions.
        val complexBuilder = ComplexBuilder.newInstance()

        // Construct a temporary sequence of complexes of bonds.
        val bondComplexes = graph
            .getEdgesByType(fragmentEdgeType)
            .filter { fragmentEdge ->
                fragmentEdge.vertices.count() > 1
            }
            .map { fragmentEdge ->
                @Suppress("UNCHECKED_CAST")
                val fragmentEdgeProxy = fragmentEdge.proxy!! as Vertex

                @Suppress("UNCHECKED_CAST")
                val bonds = fragmentEdgeProxy.userData as Set<Bond<*>>

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
     *  Removes all bonds and atoms from this builder.
     *
     *  Identifier remains intact.
     */
    protected fun clear(): B {
        bondSet.clear()
        singletonSet.clear()

        return _this
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
            },
            id ?: crul.uuid.Generator.inNCName()
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
