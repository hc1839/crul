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

package hypergraph.notify

import hypergraph.base.*

/**
 *  Message from one construct to another.
 *
 *  @property source
 *      Construct that is sending the message.
 *
 *  @constructor
 */
open class Message<out S : Construct>(val source: S)

/**
 *  Error response as part of messaging.
 *
 *  @property what
 *      Description of the error.
 *
 *  @constructor
 */
open class Error(open val what: String)

/**
 *  Response for signaling the end of messaging.
 */
interface Signal

/**
 *  Basic implementation of the response signal.
 */
enum class BasicSignal : Signal {
    ACKNOWLEDGED,
    CONFIRMED,
    DENIED
}

/**
 *  Groups together the response signal with the messaging error (if any).
 *
 *  @param signal
 *      Response signal.
 *
 *  @param error
 *      Messaging error. `null` if there was none.
 */
open class Response(val signal: Signal, val error: Error? = null)
