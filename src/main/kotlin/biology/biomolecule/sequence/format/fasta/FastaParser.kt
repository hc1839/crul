package crul.biology.biomolecule.sequence.format.fasta

import java.io.BufferedReader
import java.io.Closeable
import java.io.Reader

/**
 *  Parser of the FASTA format.
 *
 *  @constructor
 *
 *  @param reader
 *      Reader of FASTA.
 */
class FastaParser(reader: Reader) :
    AbstractIterator<FastaParser.Event>(),
    Closeable
{
    /**
     *  Buffered reader of FASTA.
     */
    private val reader: BufferedReader

    init {
        this.reader = BufferedReader(reader)
    }

    /**
     *  Current state of the parser, or `null` if nothing has been parsed.
     */
    private var currState: Event? = null

    /**
     *  Current text in FASTA where the parser is at.
     */
    private var currRawText: String = ""

    override fun computeNext() {
        var letterCode = reader.read()
        var whitespace = ""

        // Get the whitespaces up to the next non-whitespace character.
        while (letterCode != -1 &&
               letterCode.toChar().toString().matches(whitespaceCharRegex)
        ) {
            whitespace += letterCode.toChar()
            letterCode = reader.read()
        }

        if (letterCode == -1) {
            done()
            return
        }

        val letter = letterCode.toChar()

        currState = when (letter) {
            '>' -> {
                if (!whitespace.isEmpty() &&
                    trailingNewlineRegex !in whitespace
                ) {
                    throw RuntimeException(
                        "Encountered '>' with leading non-newline " +
                        "whitespaces."
                    )
                }

                currRawText = reader.readLine()

                Event.DEFLINE
            }

            else -> {
                currRawText = letter.toString()

                Event.SEQUENCE_CHAR
            }
        }

        setNext(currState!!)
    }

    /**
     *  Gets the description line without the leading '>' and with surrounding
     *  whitespaces removed.
     *
     *  If the parser state is not [Event.DEFLINE], an exception is thrown.
     */
    fun getDefline(): String {
        if (currState != Event.DEFLINE) {
            throw RuntimeException(
                "Parser is not at a description line."
            )
        }

        return currRawText.trim()
    }

    /**
     *  Gets the sequence character.
     *
     *  If the parser state is not [Event.SEQUENCE_CHAR], an exception is
     *  thrown.
     */
    fun getSequenceChar(): Char {
        if (currState != Event.SEQUENCE_CHAR) {
            throw RuntimeException(
                "Parser is not at a sequence character."
            )
        }

        return currRawText.single()
    }

    override fun close() {
        reader.close()
    }

    /**
     *  Event from [FastaParser].
     */
    enum class Event {
        /**
         *  Description line.
         */
        DEFLINE,

        /**
         *  Sequence character.
         */
        SEQUENCE_CHAR
    }

    companion object {
        /**
         *  Regular expression for a single whitespace character.
         *
         *  It is anchored at the beginning and the end of a line.
         */
        private val whitespaceCharRegex: Regex =
            Regex("^\\s$")

        /**
         *  Regular expression for a newline or newline with carriage return
         *  anchored at the end of a string.
         */
        private val trailingNewlineRegex: Regex =
            Regex("\\n(\\r)?$")
    }
}
