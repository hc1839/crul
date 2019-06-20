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

package crul.measure.unit

import crul.measure.dimension.Dimension

/**
 *  Base unit according to the Unified Code for Units of Measure (UCUM).
 */
enum class BaseUnit {
    METER {
        override val cs: String = "m"

        override val dimension: Dimension =
            Dimension.parse("L")
    },

    SECOND {
        override val cs: String = "s"

        override val dimension: Dimension =
            Dimension.parse("T")
    },

    GRAM {
        override val cs: String = "g"

        override val dimension: Dimension =
            Dimension.parse("M")
    },

    RADIAN {
        override val cs: String = "rad"

        override val dimension: Dimension =
            Dimension()
    },

    KELVIN {
        override val cs: String = "K"

        override val dimension: Dimension =
            Dimension.parse("Th")
    },

    COULOMB {
        override val cs: String = "C"

        override val dimension: Dimension =
            Dimension.parse("T.I")
    },

    CANDELA {
        override val cs: String = "cd"

        override val dimension: Dimension =
            Dimension.parse("J")
    };

    /**
     *  UCUM c/s symbol.
     */
    abstract val cs: String

    /**
     *  ISQ dimension of the base unit.
     */
    abstract val dimension: Dimension

    companion object {
        /**
         *  Gets a base unit by its UCUM c/s symbol.
         *
         *  Returns `null` if there is no such base unit.
         */
        @JvmStatic
        fun getByCs(cs: String): BaseUnit? =
            enumValues<BaseUnit>()
                .asIterable()
                .filter { it.cs == cs }
                .singleOrNull()
    }
}
