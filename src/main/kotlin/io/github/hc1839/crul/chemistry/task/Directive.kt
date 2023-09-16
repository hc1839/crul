package io.github.hc1839.crul.chemistry.task

import java.io.Writer

/**
 *  Directive in a task.
 */
interface Directive {
    /**
     *  Command name of the directive.
     */
    val commandName: String

    /**
     *  Directive that is composed of subdirectives.
     *
     *  @param D
     *      Type of subdirectives.
     */
    interface Compound<D : Directive> : Directive, List<D>

    /**
     *  Simple directive composed of fields only.
     */
    interface Simple : Directive {
        /**
         *  Field values.
         */
        val fields: List<String>
    }
}
