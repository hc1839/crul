package io.github.hc1839.crul.chemistry.tinker

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.Supermolecule
import io.github.hc1839.crul.chemistry.tinker.AtomClass
import io.github.hc1839.crul.chemistry.tinker.keyword.AtomKeyword

/**
 *  Mapper that determines the atom class of an atom in a supermolecule.
 */
interface AtomClassMapper<A : Atom> :
    (A, Supermolecule<out A>) -> AtomClass
{
    /**
     *  Atom class of an atom in a supermolecule.
     *
     *  @param atom
     *      Atom in a supermolecule. If it is not in `supermol`, an exception
     *      is raised.
     *
     *  @param supermol
     *      Supermolecule that the atom is in.
     *
     *  @return
     *      Atom class of `atom`.
     */
    abstract override fun invoke(
        atom: A,
        supermol: Supermolecule<out A>
    ): AtomClass

    /**
     *  Whether an atom class is univalent.
     */
    fun isUnivalent(atomClass: AtomClass): Boolean
}
