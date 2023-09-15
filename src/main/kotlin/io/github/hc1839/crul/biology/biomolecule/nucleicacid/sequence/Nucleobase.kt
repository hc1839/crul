package io.github.hc1839.crul.biology.biomolecule.nucleicacid.sequence

import io.github.hc1839.crul.biology.biomolecule.sequence.BioSequenceUnit

/**
 *  Generic nucleobase.
 *
 *  Type of nucleobase depends on the implementation.
 */
interface Nucleobase : BioSequenceUnit {
    /**
     *  Canonical complement of the nucleobase, or `null` if there none.
     *
     *  If not `null`, the [canonicalComplement] of the returned nucleobase
     *  must be this nucleobase.
     */
    val canonicalComplement: Nucleobase?
}
