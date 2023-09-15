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

package io.github.hc1839.crul.measure.dimension

/**
 *  Base dimension according to the International System of Quantities (ISQ).
 */
enum class BaseDimension {
    /**
     *  Length.
     */
    LENGTH {
        override val symbol: String = "L"
    },

    /**
     * Mass.
     */
    MASS {
        override val symbol: String = "M"
    },

    /**
     *  Time.
     */
    TIME {
        override val symbol: String = "T"
    },

    /**
     *  Electric current.
     */
    ELECTRIC_CURRENT {
        override val symbol: String = "I"
    },

    /**
     *  Thermodynamic temperature.
     */
    THERMODYNAMIC_TEMPERATURE {
        override val symbol: String = "Th"
    },

    /**
     *  Amount of substance.
     */
    AMOUNT_OF_SUBSTANCE {
        override val symbol: String = "N"
    },

    /**
     *  Luminous intensity.
     */
    LUMINOUS_INTENSITY {
        override val symbol: String = "J"
    };

    /**
     *  Letter symbol.
     *
     *  Symbols are the same as that specified by ISQ except thermodynamic
     *  temperature, which is represented by the romanized version of uppercase
     *  theta, '`Th`'.
     */
    abstract val symbol: String

    companion object {
        /**
         *  Gets a base dimension by its letter symbol.
         *
         *  Returns `null` if there is no such base dimension.
         */
        @JvmStatic
        fun getBySymbol(symbol: String): BaseDimension? =
            when (symbol) {
                "L" -> LENGTH
                "M" -> MASS
                "T" -> TIME
                "I" -> ELECTRIC_CURRENT
                "Th" -> THERMODYNAMIC_TEMPERATURE
                "N" -> AMOUNT_OF_SUBSTANCE
                "J" -> LUMINOUS_INTENSITY
                else -> null
            }
    }
}
