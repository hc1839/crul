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

package crul.chemistry.species.format.mol2

/**
 *  Tripos field.
 */
interface TriposStringField {
    /**
     *  Value of the Tripos string field.
     */
    val value: String

    companion object {
        /**
         *  Field value used to indicate that a non-optional string field is
         *  empty.
         */
        @JvmField
        val FOUR_STARS: String = "****"

        /**
         *  Gets the enum value of a Tripos string field.
         *
         *  @param value
         *      Value of the Tripos string field, or [FOUR_STARS]. If it does
         *      not correspond to [TriposStringField.value] of an enum value of
         *      `F`, an exception is raised unless it is [FOUR_STARS].
         *
         *  @return
         *      Enum value of `F` such that [TriposStringField.value] is equal
         *      to `value`, or `null` if `value` is [FOUR_STARS].
         */
        @JvmStatic
        inline fun <reified F> enumValueOf(value: String): F?
            where F : Enum<F>,
                  F : TriposStringField
        {
            val enums = enumValues<F>().toList()

            val enumValue = enums.singleOrNull { it.value == value }

            if (enumValue == null && value != FOUR_STARS) {
                throw IllegalArgumentException(
                    "Unrecognized field value: $value"
                )
            }

            return enumValue
        }

        /**
         *  Gets the value of a Tripos string field.
         *
         *  @param value
         *      Value of the Tripos string field.
         *
         *  @return
         *      If the trimmed version of `value` is not [FOUR_STARS], the
         *      trimmed version of `value` is returned, otherwise `null` is
         *      returned.
         */
        @JvmStatic
        fun stringValueOf(value: String): String? =
            if (value.trim() != FOUR_STARS) {
                value.trim()
            } else {
                null
            }
    }
}
