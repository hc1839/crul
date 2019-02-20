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

/**
 *  Factory for SI unit system.
 */
class SiUnitSystemFactory {
    /**
     *  Builder for construction.
     */
    private val builder: UnitSystemBuilder =
        UnitSystemBuilder.newInstance()

    constructor() {
        for (baseDim in enumValues<BaseDimension>()) {
            builder.baseUnit(baseDim, baseDim.siUnit)
        }
    }

    /**
     *  Constructs an SI unit system.
     */
    fun create(): UnitSystem =
        builder.build()
}
