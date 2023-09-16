package io.github.hc1839.crul.chemistry.task

import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import java.io.Writer

/**
 *  Token filter.
 *
 *  Token filtering is case sensitive.
 *
 *  @property tokens
 *      Map of tokens. Key is a whitespace-less token string without the token
 *      separators. Value is the replacement string.
 *
 *  @property separator
 *      Token separator that is not a word or whitespace character.
 *
 *  @constructor
 */
class TokenFilter(
    val tokens: Map<String, String>,
    val separator: Char = '@'
) {
    init {
        val whitespaceRegex = Regex("\\s")

        for (tokenString in tokens.keys) {
            if (whitespaceRegex in tokenString) {
                throw IllegalArgumentException(
                    "Token string contains whitespace characters: " +
                    "$tokenString"
                )
            }
        }

        if (Regex("\\w|\\s") in separator.toString()) {
            throw IllegalArgumentException(
                "Token separator is a word or whitespace character: " +
                "$separator"
            )
        }
    }

    /**
     *  Filters an input string by substituting tokens with their replacement
     *  strings.
     *
     *  @param input
     *      Reader of input string.
     *
     *  @param output
     *      Writer of output string.
     */
    fun filter(input: Reader, output: Writer) {
        val tokenSeparator = separator.toString()
        val inputString = input.readText()

        var outputString = inputString

        for ((tokenString, tokenValue) in tokens.entries) {
            outputString = outputString.replace(
                tokenSeparator + tokenString + tokenSeparator,
                tokenValue,
                false
            )
        }

        output.write(outputString)
    }

    /**
     *  @param input
     *      Input string.
     *
     *  @return output
     *      Output string.
     */
    fun filter(input: String): String {
        val outputWriter = StringWriter()

        filter(StringReader(input), outputWriter)

        outputWriter.flush()
        outputWriter.close()

        return outputWriter.toString()
    }
}
