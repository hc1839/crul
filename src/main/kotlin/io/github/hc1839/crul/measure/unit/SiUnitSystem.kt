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

package io.github.hc1839.crul.measure.unit

import io.github.hc1839.crul.measure.dimension.BaseDimension

/**
 *  SI unit system.
 */
object SiUnitSystem : UnitSystem(
    mapOf(
        BaseDimension.LENGTH to
            UnitOfMeasure(BaseUnit.METER),

        BaseDimension.MASS to
            UnitOfMeasure.parse("kg"),

        BaseDimension.TIME to
            UnitOfMeasure(BaseUnit.SECOND),

        BaseDimension.ELECTRIC_CURRENT to
            UnitOfMeasure.parse("A"),

        BaseDimension.THERMODYNAMIC_TEMPERATURE to
            UnitOfMeasure(BaseUnit.KELVIN),

        BaseDimension.AMOUNT_OF_SUBSTANCE to
            UnitOfMeasure.parse("mol"),

        BaseDimension.LUMINOUS_INTENSITY to
            UnitOfMeasure(BaseUnit.CANDELA)
    )
)
