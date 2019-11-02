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

package crul.math.number.quaternion

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import crul.apache.math.vector.*

/**
 *  Quaternion.
 *
 *  @param scalar
 *      Scalar part.
 *
 *  @param vector
 *      Vector part.
 */
open class Quaternion {
    val scalar: Double

    val vector: Vector3D

    constructor(scalar: Double, vector: Vector3D) {
        this.scalar = scalar
        this.vector = vector
    }

    /**
     *  Conjugate.
     */
    fun conjugate() =
        Quaternion(scalar, -vector)

    operator fun component1() =
        scalar

    operator fun component2() =
        vector

    open operator fun plus(other: Quaternion) =
        Quaternion(
            scalar + other.scalar,
            vector + other.vector
        )

    open operator fun plus(other: Double) =
        Quaternion(
            scalar + other,
            vector
        )

    open operator fun unaryMinus() =
        Quaternion(-scalar, -vector)

    open operator fun minus(other: Quaternion) =
        Quaternion(
            scalar - other.scalar,
            vector - other.vector
        )

    open operator fun minus(other: Double) =
        Quaternion(
            scalar - other,
            vector
        )

    open operator fun times(other: Double) =
        Quaternion(
            scalar * other,
            vector * other
        )

    open operator fun times(other: Quaternion) =
        other * scalar + vector
            .toArray()
            .mapIndexed { idx, cmpt ->
                when (idx) {
                    0 -> I * other * cmpt
                    1 -> J * other * cmpt
                    2 -> K * other * cmpt
                    else -> throw RuntimeException(
                        "Not a three-dimensional vector."
                    )
                }
            }
            .reduce { acc, item -> acc + item }
}

/**
 *  *i* quaternion unit.
 */
private object I {
    operator fun times(other: Quaternion) = Quaternion(
        -other.vector.x,
        Vector3D(
            other.scalar,
            -other.vector.z,
            other.vector.y
        )
    )
}

/**
 *  *j* quaternion unit.
 */
private object J {
    operator fun times(other: Quaternion) = Quaternion(
        -other.vector.y,
        Vector3D(
            other.vector.z,
            other.scalar,
            -other.vector.x
        )
    )
}

/**
 *  *k* quaternion unit.
 */
private object K {
    operator fun times(other: Quaternion) = Quaternion(
        -other.vector.z,
        Vector3D(
            -other.vector.y,
            other.vector.x,
            other.scalar
        )
    )
}
