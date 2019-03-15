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

import kotlin.math.pow

/**
 *  Vector with an arbitrary number of components.
 */
open class Vector : Spatial {
    constructor(components: List<Double>): super(components)

    constructor(vararg components: Double): this(components.toList())

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): super(msgpack)

    /**
     *  Dot product of this and `other`.
     *
     *  `other` must have the same dimensionality as this.
     */
    fun dot(other: Vector): Double {
        if (other.dimensionality != dimensionality) {
            throw IllegalArgumentException(
                "Different dimensionalities."
            )
        }

        return components
            .zip(other.components)
            .map { it.first * it.second }
            .reduce { acc, item -> acc + item }
    }

    /**
     *  Magnitude.
     */
    fun magnitude(): Double =
        when (dimensionality) {
            0 -> 0.0
            else -> kotlin.math.sqrt(this.dot(this))
        }

    /**
     *  Angle, in radians, between this and `other`.
     *
     *  Both vectors must be non-zero vectors.
     */
    fun angle(other: Vector): Double {
        if (other.dimensionality != dimensionality) {
            throw IllegalArgumentException(
                "Different dimensionalities."
            )
        }

        val thisMagnitude = magnitude()
        val otherMagnitude = other.magnitude()

        if (
            crul.float.Comparison.nearlyEquals(thisMagnitude, 0.0) ||
            crul.float.Comparison.nearlyEquals(otherMagnitude, 0.0)
        ) {
            throw RuntimeException(
                "Cannot be a zero vector."
            )
        }

        return kotlin
            .math
            .acos(
                dot(other) / (thisMagnitude * otherMagnitude)
            )
    }

    /**
     *  Unit vector.
     *
     *  The vector cannot be zero-dimensional and cannot be a zero vector.
     */
    fun unit(): Vector {
        if (dimensionality == 0) {
            throw IllegalArgumentException(
                "Vector has zero dimensions."
            )
        }

        val magnitude = magnitude()

        if (crul.float.Comparison.nearlyEquals(magnitude, 0.0)) {
            throw RuntimeException(
                "Cannot be a zero vector."
            )
        }

        return this / magnitude
    }

    open operator fun plus(other: Vector): Vector =
        Vector(
            components.zip(other.components) { first, second ->
                first + second
            }
        )

    open operator fun unaryMinus(): Vector =
        Vector(components.map { -it })

    open operator fun minus(other: Vector): Vector =
        Vector(
            components.zip(other.components) { first, second ->
                first - second
            }
        )

    open operator fun times(other: Double): Vector =
        Vector(components.map { it * other })

    open operator fun div(other: Double): Vector =
        Vector(components.map { it / other })
}
