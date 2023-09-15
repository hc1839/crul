package io.github.hc1839.crul.distinct

/**
 *  Distinctor that wraps a value for custom distinguishability.
 *
 *  @param V
 *      Type of value that is being wrapped.
 */
interface Distinctor<V> {
    abstract override fun hashCode(): Int

    abstract override fun equals(other: Any?): Boolean

    /**
     *  Value that is being wrapped.
     */
    val value: V
}
