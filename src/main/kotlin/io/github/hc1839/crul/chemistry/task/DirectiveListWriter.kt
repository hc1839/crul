package io.github.hc1839.crul.chemistry.task

import java.io.Closeable

/**
 *  Writes a directive list to an output source as input for a software
 *  package.
 *
 *  @param D
 *      Type of directives in a directive list.
 */
interface DirectiveListWriter<D : Directive> : Closeable {
    /**
     *  Writes the string representation of the specified `directiveList` as
     *  input for the software package.
     *
     *  The string representation includes a trailing newline.
     */
    fun write(directiveList: List<D>)
}
