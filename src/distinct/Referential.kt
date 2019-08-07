package crul.distinct

/**
 *  Referential distinctor of a value.
 */
data class Referential<V>(override val value: V) : Distinctor<V> {
    /**
     *  Always `1`.
     */
    override fun hashCode(): Int = 1

    /**
     *  Two instances are equal if and only if their wrapped values are
     *  referentially equal.
     */
    override fun equals(other: Any?): Boolean =
        other is Referential<*> &&
        this.value === other.value
}
