package io.github.hc1839.crul.chemistry.tinker.keyword

/**
 *  Interface for a Tinker keyword.
 */
interface Keyword {
    /**
     *  Converts the data to an input text for Tinker's key file.
     *
     *  @param args
     *      Arguments to pass. It depends on the implementation.
     *
     *  @param separator
     *      Separator to use between fields.
     */
    fun toInputText(vararg args: Any?, separator: String = " "): String
}
