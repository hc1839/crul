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

@file:JvmName("Datatype")
@file:JvmMultifileClass

package crul.xml

/**
 *  Functions related to data types in XML.
 */
object Datatype {
    /**
     *  Regular expression, as a string, of the NCName production.
     */
    fun NCNameRegexString(): String =
        """[a-zA-Z_][a-zA-Z0-9\.\-_]*"""

    /**
     *  Whether `string` conforms to the NCName production.
     */
    @JvmStatic
    fun isNCName(string: String): Boolean =
        kotlin.text.Regex("^${NCNameRegexString()}$") in string
}
