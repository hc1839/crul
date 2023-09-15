package io.github.hc1839.crul.biology.biomolecule.nucleicacid.sequence

import io.github.hc1839.crul.biology.biomolecule.sequence.BioSequence

/**
 *  Sequence of nucleobases.
 *
 *  @constructor
 *
 *  @param nucleobases
 *      Backing list of nucleobases.
 */
open class NucleobaseSequence<B : Nucleobase>(
    nucleobases: List<B>
) : BioSequence<B>(nucleobases)
{
    /**
     *  Reverse complement, or `null` if any of the nucleobases does not have a
     *  canonical complement.
     */
    fun reverseComplement(): NucleobaseSequence<B>? {
        val buffer = reversed().map {
            @Suppress("UNCHECKED_CAST")
            it.canonicalComplement as B?
        }

        return if (buffer.all { it != null }) {
            NucleobaseSequence(buffer.map { it!! })
        } else {
            null
        }
    }
}
