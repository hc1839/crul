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

@file:JvmName("Extensions")
@file:JvmMultifileClass

package crul.apache.math.vector

import org.apache.commons.math3.geometry.Vector
import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

/**
 *  Converts a list of `Double` to Apache's [RealVector].
 */
fun List<Double>.toRealVector(): RealVector =
    ArrayRealVector(toDoubleArray())

/**
 *  Converts Apache's [RealVector] to a list of `Double`.
 */
fun RealVector.toList(): List<Double> =
    toArray().toList()

operator fun Vector3D.plus(v: Vector<Euclidean3D>): Vector3D =
    add(v)

operator fun Vector3D.unaryMinus(): Vector3D =
    negate()

operator fun Vector3D.minus(v: Vector<Euclidean3D>): Vector3D =
    subtract(v)

operator fun Vector3D.times(a: Double): Vector3D =
    scalarMultiply(a)

operator fun Vector3D.div(a: Double): Vector3D =
    this * (1.0 / a)
