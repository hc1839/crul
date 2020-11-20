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

package crul.measure

import java.io.Closeable

/**
 *  Writer of a [Quantity] to an output source.
 */
interface QuantityWriter : Closeable {
    /**
     *  Writes `obj` to the output source in the UCUM unit specified by
     *  `unitUcum`.
     */
    fun write(obj: Quantity, unitUcum: String)
}
