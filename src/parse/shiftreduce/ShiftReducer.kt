package parse.shiftreduce

import hierarchy.tree.Node

/**
 *  Interface for a class that performs shift-reduce operations in succession.
 *
 *  To execute the shift-reduce operations successively, use
 *  [ShiftReduceActuator].
 *
 *  @param T
 *      Enum type to use as node types in the parse tree.
 */
interface ShiftReducer<T : Enum<T>> {
    /**
     *  Advances in the input stream by one token.
     *
     *  @return
     *      Next token in the stream. The returned node is cloned along with
     *      the user data, and any descendants are ignored. If there are no
     *      more tokens, `null` is returned. It must return a token upon the
     *      first call.
     */
    fun shift(): Node<T>?

    /**
     *  Performs the reduce step, with the return value acting as a decision
     *  for the next step.
     *
     *  @param parseStack
     *      Parse stack. The most recently shifted token has the highest index.
     *
     *  @param lookahead
     *      Node that represents the current lookahead symbol, or `null` if the
     *      last token in the stream has been placed in the parse stack.
     *      Modifying it has no effect.
     *
     *  @return
     *      New parse stack as a non-empty list with the rightmost node(s)
     *      reduced, or `null`. If not `null`, this function is called
     *      immediately with the returned parse stack as the argument for
     *      `parseStack`. The returned list, if any, is not stored, and the
     *      nodes are not cloned. When the parsing is complete, the parse stack
     *      must contain exactly one node, which is the root node of the parse
     *      tree. If nothing is to be reduced, `null` is returned, causing the
     *      next step to be a shift step.
     */
    fun reduce(parseStack: List<Node<T>>, lookahead: Node<T>?): List<Node<T>>?
}
