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

package crul.transform

/**
 *  Transform that maps an input object of one type to an output object of
 *  another type.
 *
 *  @param I
 *      Type of input object.
 *
 *  @param O
 *      Type of output object.
 */
interface Transform<in I, out O : Any> :
    (I) -> O,
    (I, List<Any?>) -> O
{
    /**
     *  Maps an input object to an output object.
     *
     *  @param input
     *      Input object.
     *
     *  @return
     *      Output object.
     */
    override fun invoke(input: I): O =
        invoke(input, listOf())

    /**
     *  Maps an input object to an output object with additional arguments.
     *
     *  @param input
     *      Input object.
     *
     *  @param args
     *      Additional arguments.
     *
     *  @return
     *      Output object.
     */
    override fun invoke(input: I, args: List<Any?>): O
}
