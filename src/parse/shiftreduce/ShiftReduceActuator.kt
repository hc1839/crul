package parse.shiftreduce

import hierarchy.tree.Node

/**
 *  Actuator that executes a sequence of shift-reduce steps.
 *
 *  The actuator is constrained to run only once.
 *
 *  @param T
 *      Enum type to use as node types in the parse tree.
 */
class ShiftReduceActuator<T : Enum<T>> {
    /**
     *  Shift-reduce parser.
     */
    private val shiftReducer: ShiftReducer<T>

    /**
     *  Whether this actuator has previously run.
     */
    var hasRun: Boolean = false
        private set

    /**
     *  @param shiftReducer
     *      Shift-reduce parser that performs the shift-reduce steps in
     *      succession.
     */
    constructor(shiftReducer: ShiftReducer<T>) {
        this.shiftReducer = shiftReducer
    }

    /**
     *  Executes the sequence of shift-reduce steps, and returns the parse
     *  tree.
     *
     *  If the actuator has previously run, an exception is raised.
     */
    fun actuate(): Node<T> {
        if (hasRun) {
            throw RuntimeException(
                "Actuator has previously run."
            )
        } else {
            hasRun = true
        }

        val parseStack: MutableList<Node<T>> = mutableListOf()

        var lookahead: Node<T>? = shiftReducer
            .shift()
            ?.cloneNode(false, true)

        // There must be at least one lookahead symbol.
        if (lookahead == null) {
            throw IllegalArgumentException(
                "First shift did not return a token."
            )
        }

        // Actuate the sequence of shift-reduce steps.
        do {
            parseStack.add(lookahead!!)
            lookahead = shiftReducer.shift()?.cloneNode(false, true)

            var parseStackBuf: List<Node<T>>?

            // Keep reducing until the next step is a shift step.
            while (true) {
                parseStackBuf = shiftReducer
                    .reduce(parseStack.toList(), lookahead)

                if (parseStackBuf == null) {
                    break
                } else if (parseStackBuf.isEmpty()) {
                    throw RuntimeException(
                        "Parse stack is empty."
                    )
                }

                parseStack.clear()
                parseStack.addAll(parseStackBuf)
            }
        } while (lookahead != null)

        // Completion of parsing must result in one parse tree.
        if (parseStack.count() != 1) {
            throw IllegalArgumentException(
                "Parsing is complete, but the parse stack " +
                "does not contain exactly one node."
            )
        }

        return parseStack.first()
    }
}
