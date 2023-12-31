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
import io.github.hc1839.crul.measure.dimension.Dimension
import io.github.hc1839.crul.measure.unit.UnitOfMeasure

/**
 *  Mutable builder for [UnitSystem].
 *
 *  To construct an instance of this class, use [newInstance].
 */
open class UnitSystemBuilder<B : UnitSystemBuilder<B>> {
    @Suppress("UNCHECKED_CAST")
    protected val _this: B = this as B

    protected constructor()

    /**
     *  Base units to use for each base dimension when constructing a
     *  [UnitSystem].
     */
    protected val baseUnits: MutableMap<BaseDimension, UnitOfMeasure?> =
        enumValues<BaseDimension>()
            .asIterable()
            .associateWith { null }
            .toMutableMap()

    /**
     *  Configures the base unit of a base dimension.
     *
     *  Base units for all base dimensions must be set before [build] is
     *  called.
     *
     *  @param baseDim
     *      Base dimension whose base unit is to be set.
     *
     *  @param baseUnit
     *      New base unit for `baseDim`. It must be commensurable with the
     *      corresponding SI base unit.
     *
     *  @return
     *      `this`.
     */
    fun setBaseUnit(
        baseDim: BaseDimension,
        baseUnit: UnitOfMeasure
    ): B
    {
        if (!baseUnit.isUnitOf(Dimension(baseDim))) {
            throw IllegalArgumentException(
                "New base unit is not commensurable with the " +
                "corresponding SI base unit of '${baseDim.name}'."
            )
        }

        baseUnits[baseDim] = baseUnit

        return _this
    }

    /**
     *  Constructs a [UnitSystem] from the data in this builder.
     */
    fun build(): UnitSystem =
        UnitSystem(
            baseUnits.mapValues { (baseDim, baseUnit) ->
                if (baseUnit != null) {
                    baseUnit
                } else {
                    throw RuntimeException(
                        "Base unit for '${baseDim.name} is not set."
                    )
                }
            }
        )

    companion object {
        private class UnitSystemBuilderImpl():
            UnitSystemBuilder<UnitSystemBuilderImpl>()

        /**
         *  Creates an instance of [UnitSystemBuilder].
         */
        @JvmStatic
        fun newInstance(): UnitSystemBuilder<*> =
            UnitSystemBuilderImpl()
    }
}
