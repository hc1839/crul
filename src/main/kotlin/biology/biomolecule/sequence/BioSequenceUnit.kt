package crul.biology.biomolecule.sequence

/**
 *  Generic coding unit in a biomolecular sequence.
 *
 *  Type of unit depends on the implementation.
 */
interface BioSequenceUnit {
    abstract override fun hashCode(): Int

    abstract override fun equals(other: Any?): Boolean

    /**
     *  Unit in its textual display form.
     *
     *  It is for display purposes and should not be used for comparing units.
     */
    abstract override fun toString(): String

    /**
     *  Character code of the unit.
     *
     *  Case is implementation-dependent.
     */
    val code: Char
}
