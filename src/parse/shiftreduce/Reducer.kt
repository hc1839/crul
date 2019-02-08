/*
 *  Copyright Han Chen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package parse.shiftreduce

import hierarchy.tree.Node

/**
 *  Performer of an individual reduce step corresponding to the node type of
 *  the rightmost token in the parse stack.
 *
 *  Implementer of this interface is to be used with [Actuator]. Therefore, the
 *  implementor is expected to be an enum constant that represents a node type.
 *
 *  @param T
 *      Enum type of the node types in the parse tree.
 */
interface Reducer<T : Enum<T>> {
    /**
     *  Whether this node type is an accepting state in the parsing.
     */
    val isAccepting: Boolean

    /**
     *  Whether a token matches a first token of the production corresponding
     *  to this reducer.
     *
     *  It is mainly used in determining whether a token can be matched to the
     *  leftmost token of a specific production in order to decide how the
     *  parse stack should be reduced. It does not necessarily mean that the
     *  token will be reduced as such in the parse stack.
     */
    fun matchesFirst(token: String): Boolean

    /**
     *  Performs an individual reduce step.
     *
     *  @param parseStack
     *      Parse stack of scanned tokens. The most recently shifted token is
     *      the rightmost (last) element whose node type corresponds to this
     *      implementer. Nodes are not cloned and, therefore, should not be
     *      modified.
     *
     *  @param lookahead
     *      Current lookahead symbol; `null` if the last token in the stream
     *      has already been placed in the parse stack. It is not cloned and,
     *      therefore, should not be modified.
     *
     *  @return
     *      Pair, where the first value is the new parent node to be appended
     *      to the parse stack, and the second value is the number of the
     *      rightmost nodes in `parseStack` that will be removed and become
     *      children of the new parent; `null` if the parse stack cannot be
     *      further reduced. If a pair is returned, the new parent node is
     *      cloned along with the user data, and any descendants are ignored.
     *      The number of rightmost nodes in the parse stack to become its
     *      children must be positive.
     */
    fun reduce(parseStack: List<Node<T>>, lookahead: Node<T>?): Pair<Node<T>, Int>?
}
