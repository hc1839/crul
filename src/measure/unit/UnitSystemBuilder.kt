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

package measure.unit

import measure.dimension.BaseDimension
import measure.unit.UnitOfMeasure

/**
 *  Abstract mutable builder for [UnitSystem].
 *
 *  When instantiated, it initializes the base units to the SI unit system. An
 *  instance of a subclass acts as a factory that creates a specific unit
 *  system by setting the individual base units.
 */
abstract class UnitSystemBuilder {
    constructor()

    /**
     *  Base units to use for building a [UnitSystem].
     */
    protected val baseUnits: MutableMap<BaseDimension, UnitOfMeasure> =
        enumValues<BaseDimension>()
            .asIterable()
            .associateWith { it.siUnit }
            .toMutableMap()

    /**
     *  Sets the base unit of a base dimension.
     *
     *  @param baseDimension
     *      Base dimension whose base unit is to be set.
     *
     *  @param baseUnit
     *      New base unit for `baseDimension`. It must be commensurable with
     *      the corresponding SI base unit.
     *
     *  @return
     *      `this`.
     */
    protected fun set(
        baseDimension: BaseDimension,
        baseUnit: UnitOfMeasure
    ): UnitSystemBuilder
    {
        if (!baseUnit.isCommensurable(baseDimension.siUnit)) {
            throw IllegalArgumentException(
                "New base unit is not commensurable with the " +
                "corresponding SI base unit of '${baseDimension}'."
            )
        }

        baseUnits[baseDimension] = baseUnit

        return this
    }

    /**
     *  Constructs a [UnitSystem] from the data in this builder.
     */
    protected fun build(): UnitSystem {
        val newUnitSystem = UnitSystem()

        for ((baseDim, baseUnit) in baseUnits) {
            newUnitSystem.set(baseDim, baseUnit)
        }

        return newUnitSystem
    }

    /**
     *  Factory function specific to a unit system.
     */
    abstract fun create(): UnitSystem
}
