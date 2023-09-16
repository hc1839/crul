package io.github.hc1839.crul.chemistry.species.format.pdb

import io.github.hc1839.crul.chemistry.species.Supermolecule

/**
 *  [PdbDecodingListener] as a builder of [Supermolecule].
 *
 *  @param M
 *      Type of supermolecule being built.
 */
interface PdbSupermoleculeBuilder<M : Supermolecule<PdbAtom>> :
    PdbDecodingListener
{
    /**
     *  Builds a list of supermolecules from the data received from
     *  [PdbDecoder].
     */
    fun build(): List<M>
}
