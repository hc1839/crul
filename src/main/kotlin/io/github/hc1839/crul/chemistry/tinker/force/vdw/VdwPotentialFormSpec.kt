package io.github.hc1839.crul.chemistry.tinker.force.vdw

/**
 *  Constants for specifying a vdW-potential form.
 *
 *  @property exps
 *      Exponent constants.
 *
 *  @property bufs
 *      Buffering constants.
 *
 *  @constructor
 */
data class VdwPotentialFormSpec(
    val exps: ExponentGroup,
    val bufs: BufferingGroup
) {
    /**
     *  Letters representing the constants are that of Halgren 1992.
     *
     *  @param n
     *      The `n` constant. See [ExponentGroup] for the bounds.
     *
     *  @param m
     *      The `m` constant. See [ExponentGroup] for the bounds.
     *
     *  @param delta
     *      The `delta` buffering constant.
     *
     *  @param gamma
     *      The `gamma` buffering constant.
     */
    constructor(
        n: Int,
        m: Int,
        delta: Double,
        gamma: Double
    ): this(
        exps = ExponentGroup(n = n, m = m),
        bufs = BufferingGroup(delta = delta, gamma = gamma)
    )

    /**
     *  Exponent constants.
     *
     *  @property n
     *      The `n` constant. It must be between `12` and `15`, inclusive.
     *
     *  @property m
     *      The `m` constant. It must be between `6` and `8`, inclusive.
     *
     *  @constructor
     */
    data class ExponentGroup(
        val n: Int,
        val m: Int
    ) {
        init {
            if (n < 12 || n > 15) {
                throw IllegalArgumentException(
                    "'n' is not between 12 and 15, inclusive."
                )
            }

            if (m < 6 || m > 8) {
                throw IllegalArgumentException(
                    "'m' is not between 6 and 8, inclusive."
                )
            }
        }
    }

    /**
     *  Buffering constants.
     *
     *  @property delta
     *      The `delta` buffering constant.
     *
     *  @property gamma
     *      The `gamma` buffering constant.
     *
     *  @constructor
     */
    data class BufferingGroup(
        val delta: Double,
        val gamma: Double
    )
}
