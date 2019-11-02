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

import crul.measure.dimension.BaseDimension

/**
*  Unit of time in atomic unit system.
*/
private val atomicTimeUnit: UnitOfMeasure =
    UnitOfMeasure(BaseUnit.SECOND) * 2.418884326505e-17

/**
 *  Hartree atomic unit system.
 *
 *  The base unit for amount of substance is set to be the same as the
 *  corresponding SI unit, which is the mole.
 *
 *  Luminous intensity does not have an apparent atomic unit, so it is derived
 *  by first determining the irradiance (according to radiometry) when the
 *  electric field is set to 1 when in atomic units. The irradiance formula
 *  used is
 *
 *  irradiance = 0.5 * speedOfLight * permittivity * electricField^2
 *
 *  Then, a sphere with radius of 1 bohr is used to determine the radiant
 *  intensity from irradiance. Finally, the relative magnitude of the radiant
 *  intensity in atomic units with respect to 1 candela's radiant intensity of
 *  1 / 683 W/sr defines the atomic unit of luminous intensity, which is
 *  approximately 671.215 cd.
 */
object AtomicUnitSystem : UnitSystem(
    mapOf(
        BaseDimension.LENGTH to
            UnitOfMeasure(BaseUnit.METER) * 5.2917721092e-11,

        BaseDimension.MASS to
            UnitOfMeasure.parse("kg") * 9.10938291e-31,

        BaseDimension.TIME to
            atomicTimeUnit,

        BaseDimension.ELECTRIC_CURRENT to
            UnitOfMeasure(BaseUnit.COULOMB) * 1.602176565e-19 / atomicTimeUnit,

        BaseDimension.THERMODYNAMIC_TEMPERATURE to
            UnitOfMeasure(BaseUnit.KELVIN) * 3.1577464e5,

        BaseDimension.AMOUNT_OF_SUBSTANCE to
            UnitOfMeasure.parse("mol"),

        BaseDimension.LUMINOUS_INTENSITY to
            UnitOfMeasure(BaseUnit.CANDELA) * 671.2146654428731
    )
)
