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
     *  Hash code of [seqUnits].
     */
    override fun hashCode(): Int =
        seqUnits.hashCode()

    /**
     *  Equality is equivalent to the equality of [seqUnits].
     */
    override fun equals(other: Any?): Boolean =
        other is BioSequence<*> &&
        this::class == other::class && (
            seqUnits == other.seqUnits
        )

    /**
     *  List of [BioSequenceUnit.code] concatenated into a string.
     */
    override fun toString(): String =
        seqUnits.map { it.code }.joinToString("")
}
