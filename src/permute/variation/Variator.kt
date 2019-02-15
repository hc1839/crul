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

package permute.variation

/**
 *  Variator over a sequence of elements.
 */
interface Variator<out E> {
    /**
     *  Element that this variator is at.
     */
    fun value(): E

    /**
     *  Whether this variator is at the beginning.
     */
    fun isBegin(): Boolean

    /**
     *  Whether this variator is at the end.
     */
    fun isEnd(): Boolean

    /**
     *  Variator at the beginning.
     */
    fun begin(): Variator<E>

    /**
     *  Variator at the end.
     */
    fun end(): Variator<E>

    /**
     *  Incremented variator.
     *
     *  The meaning of incrementation depends on the class implementing this
     *  interface.
     */
    operator fun inc(): Variator<E>

    /**
     *  Decremented variator.
     *
     *  The meaning of decrementation depends on the class implementing this
     *  interface.
     */
    operator fun dec(): Variator<E>
}
