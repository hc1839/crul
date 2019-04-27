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
 *  Skeletal implementation of [MoleculeComplex].
 *
 *  @param A
 *      Type of atoms.
 */
abstract class AbstractMoleculeComplex<A : Atom> :
    AbstractComplex<Species>,
    MoleculeComplex<A>
{
    override val id: String

    /**
     *  @param subspecies
     *      Molecules and atoms of the complex.
     *
     *  @param id
     *      Identifier for this complex. It must conform to XML NCName
     *      production.
     */
    @JvmOverloads
    constructor(
        subspecies: Collection<Species>,
        id: String = crul.uuid.Generator.inNCName()
    ): super(subspecies)
    {
        if (!subspecies.all { it is Molecule<*> || it is Atom }) {
            throw IllegalArgumentException(
                "Subspecies are not molecules or atoms."
            )
        }

        if (!crul.xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "ID does not conform to XML NCName production: $id"
            )
        }

        // Atom identifiers from all subspecies without removing redundancies.
        val atomIds = subspecies.flatMap { species ->
            species.atoms().map { atom ->
                atom.id
            }
        }

        for (atomId in atomIds.toSet()) {
            if (atomIds.filter { it == atomId }.count() > 1) {
                throw IllegalArgumentException(
                    "Atom identifier exists in more than one subspecies: " +
                    "$atomId"
                )
            }
        }

        this.id = id
    }

    /**
     *  Copy constructor.
     *
     *  @param other
     *      Molecule complex to copy.
     *
     *  @param deep
     *      Whether molecules and atoms are copied.
     *
     *  @param id
     *      Identifier to use for the copied complex. It must conform to XML
     *      NCName production.
     */
    @JvmOverloads
    constructor(
        other: AbstractMoleculeComplex<A>,
        deep: Boolean = false,
        id: String = other.id
    ): super(other, deep)
    {
        if (!crul.xml.Datatype.isNCName(id)) {
            throw IllegalArgumentException(
                "ID does not conform to XML NCName production: $id"
            )
        }

        this.id = id
    }

    override fun getMoleculeWithAtom(atom: A): Molecule<A>? =
        molecules()
            .filter { it.containsAtom(atom) }
            .singleOrNull()
}
