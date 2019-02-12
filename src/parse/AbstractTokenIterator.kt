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

package parse

import parse.ParseNode

/**
 *  Iterator over tokens as [ParseNode].
 *
 *  @param T
 *      Enum type of node types in the parse tree.
 */
abstract class AbstractTokenIterator<T : Enum<T>> :
    AbstractIterator<ParseNode<T>>
{
    /**
     *  Staging area as an iterator over strings.
     */
    private var stage: Iterator<String>

    /**
     *  @param stage
     *      Staging area for strings that will turn into tokens by the
     *      subclass.
     */
    constructor(stage: Iterator<String>) {
        this.stage = stage
    }

    /**
     *  Shifts the next string from the stage, or `null` if there are no more
     *  strings.
     */
    protected fun shift(): String? =
        if (stage.hasNext()) {
            stage.next()
        } else {
            null
        }

    /**
     *  Unshifts a string back to the stage.
     */
    protected fun unshift(str: String) {
        stage = (listOf(str).asSequence() + stage.asSequence()).iterator()
    }
}
