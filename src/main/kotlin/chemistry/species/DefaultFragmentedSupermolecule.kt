package crul.chemistry.species

import crul.distinct.Referential

/**
 *  Default implementation of [FragmentedSupermolecule].
 *
 *  @property supermol
 *      Supermolecule as the delegated object.
 *
 *  @param A
 *      Type of atoms.
 *
 *  @constructor
 */
open class DefaultFragmentedSupermolecule<A : Atom>(
    private val supermol: Supermolecule<A>,
    override val fragments: List<Fragment<A>>,
    override val name: String?
) : FragmentedSupermolecule<A>,
    Supermolecule<A> by supermol
{
    init {
        if (fragments.isEmpty()) {
            throw IllegalArgumentException(
                "List of fragments is empty."
            )
        }

        val wrappedDistinctAtomsOfFragments = fragments
            .flatMap { it.atoms.map(::Referential) }
            .distinct()

        if (wrappedDistinctAtomsOfFragments.count() !=
            fragments.map { it.atoms.count() }.sum()
        ) {
            throw IllegalArgumentException(
                "Atoms are not referentially distinct across all fragments."
            )
        }

        if (wrappedDistinctAtomsOfFragments.count() !=
            supermol.atoms.count()
        ) {
            throw IllegalArgumentException(
                "Not all atoms of the supermolecule are " +
                "in the fragments or vice versa."
            )
        }

        val containsAllAtoms = supermol
            .atoms
            .map(::Referential)
            .containsAll(wrappedDistinctAtomsOfFragments)

        if (!containsAllAtoms) {
            throw IllegalArgumentException(
                "Not all atoms of the supermolecule are " +
                "in the fragments or vice versa."
            )
        }
    }

    override fun <R : Atom> map(name: String?, transform: (A) -> R):
        FragmentedSupermolecule<R>
    {
        val mappedSupermol = supermol.map(name, transform)

        // From wrapped original atom to mapped atom.
        val atomCorrespondence = atoms.zip(mappedSupermol.atoms) {
            origAtom, mappedAtom ->

            Pair(Referential(origAtom), mappedAtom)
        }.toMap()

        val newFragments = fragments.map { fragment ->
            // Transform the fragment such that the mapped atoms are used.
            fragment.map { atom ->
                atomCorrespondence[Referential(atom)]!!
            }
        }

        return DefaultFragmentedSupermolecule(
            mappedSupermol,
            newFragments,
            name
        )
    }
}
