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

@file:JvmName("Extensions")
@file:JvmMultifileClass

package io.github.hc1839.crul.apache.cli

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.OptionGroup

/**
 *  Sets an option in an option group that is specified in the command line as
 *  the selected option.
 *
 *  If the option group is required and no option in the group is specified in
 *  the command line, an exception is thrown. If more than one option in the
 *  option group is specified in the command line, an exception is thrown.
 *
 *  @param commandLine
 *      Command line parsed by Apache Commons CLI.
 */
fun OptionGroup.setSelected(commandLine: CommandLine) {
    val selectedOptions = options.filter {
        commandLine.hasOption(it.opt)
    }

    if (isRequired && selectedOptions.isEmpty()) {
        throw RuntimeException(
            "Exactly one of the following options is required " +
            "but is not specified: ${names.joinToString(", ")}"
        )
    }

    if (selectedOptions.count() > 1) {
        throw RuntimeException(
            "Exactly one of the following is required " +
            "but more than one are specified: ${names.joinToString(", ")}"
        )
    }

    setSelected(selectedOptions.single())
}
