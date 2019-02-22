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

package crul.math.coordsys

/**
 *  Spherical coordinate tuple in ISO convention.
 */
open class Spherical : Position3D {
    val radius: Double

    val polar: Double

    val azimuthal: Double

    constructor(radius: Double, polar: Double, azimuthal: Double):
        super(radius, polar, azimuthal)
    {
        this.radius = radius
        this.polar = polar
        this.azimuthal = azimuthal
    }

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): super(msgpack) {
        this.radius = component1()
        this.polar = component2()
        this.azimuthal = component3()
    }

    /**
     *  Converts the spherical coordinates to a position vector in Cartesian
     *  coordinates.
     */
    override fun toVector3D() =
        Vector3D(
            radius *
                kotlin.math.sin(polar) *
                kotlin.math.cos(azimuthal),
            radius *
                kotlin.math.sin(polar) *
                kotlin.math.sin(azimuthal),
            radius *
                kotlin.math.cos(polar)
        )
}
