package crul.biology.biomolecule.sequence

/**
 *  List of [BioSequenceUnit].
 *
 *  @property seqUnits
 *      Backing list of sequence units.
 *
 *  @constructor
 */
open class BioSequence<U : BioSequenceUnit>(private val seqUnits: List<U>) :
    List<U> by seqUnits
{
    /**
     *  List of [BioSequenceUnit.code] concatenated into a string.
     */
    override fun toString(): String =
        seqUnits.map { it.code }.joinToString("")
}