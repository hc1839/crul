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
 *  Variators as a collection.
 */
interface VariatorCollection<out T> : Iterable<VariatorCollection<T>> {
    /**
     *  Whether all variators are at the beginning.
     */
    fun isBegin(): Boolean

    /**
     *  Whether all variators are at the end.
     */
    fun isEnd(): Boolean

    /**
     *  New variator collection with all variators at the beginning.
     */
    fun begin(): VariatorCollection<T>

    /**
     *  New variator collection with all variators at the end.
     */
    fun end(): VariatorCollection<T>

    /**
     *  Increments the variator collection.
     *
     *  The meaning of incrementation depends on the class implementing this
     *  interface.
     */
    operator fun inc(): VariatorCollection<T>

    /**
     *  Decrements the variator collection.
     *
     *  The meaning of decrementation depends on the class implementing this
     *  interface.
     */
    operator fun dec(): VariatorCollection<T>
}
