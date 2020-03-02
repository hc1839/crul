package crul.biology.biomolecule.nucleicacid.sequence

/**
 *  Nucleobase found in DNA.
 */
sealed class Deoxyribonucleobase() : Nucleobase {
    override fun hashCode(): Int =
        code.hashCode()

    override fun equals(other: Any?): Boolean =
        other is Deoxyribonucleobase && (
            code == other.code
        )

    abstract override val canonicalComplement: Deoxyribonucleobase

    companion object {
        /**
         *  Gets the nucleobase corresponding to the specified `code`.
         *
         *  `code` is case-insensitive.
         */
        @JvmStatic
        fun getBase(code: Char): Deoxyribonucleobase =
            when (code) {
                'A', 'a' -> DeoxyadenosineBase
                'T', 't' -> DeoxythymidineBase
                'G', 'g' -> DeoxyguanosineBase
                'C', 'c' -> DeoxycytidineBase

                else -> throw IllegalArgumentException(
                    "Invalid nucleobase code: $code"
                )
            }
    }
}

/**
 *  Deoxyadenosine base.
 */
object DeoxyadenosineBase : Deoxyribonucleobase() {
    override fun toString(): String = "dA"

    override val code: Char = 'A'

    override val canonicalComplement: Deoxyribonucleobase =
        DeoxythymidineBase
}

/**
 *  Deoxythymidine base.
 */
object DeoxythymidineBase : Deoxyribonucleobase() {
    override fun toString(): String = "dT"

    override val code: Char = 'T'

    override val canonicalComplement: Deoxyribonucleobase =
        DeoxyadenosineBase
}

/**
 *  Deoxyguanosine base.
 */
object DeoxyguanosineBase : Deoxyribonucleobase() {
    override fun toString(): String = "dG"

    override val code: Char = 'G'

    override val canonicalComplement: Deoxyribonucleobase =
        DeoxycytidineBase
}

/**
 *  Deoxycytidine base.
 */
object DeoxycytidineBase : Deoxyribonucleobase() {
    override fun toString(): String = "dC"

    override val code: Char = 'C'

    override val canonicalComplement: Deoxyribonucleobase =
        DeoxyguanosineBase
}
