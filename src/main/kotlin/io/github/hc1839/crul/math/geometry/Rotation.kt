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

@file:JvmName("Rotation")
@file:JvmMultifileClass

package io.github.hc1839.crul.math.geometry

import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.apache.math.vector.*
import io.github.hc1839.crul.math.number.quaternion.Quaternion

/**
 *  Rotates a position pointed by this vector for `angle` (in radians) with
 *  respect to `axis` that crosses the origin.
 */
fun Vector3D.rotate(axis: Vector3D, angle: Double): Vector3D {
    val origPos = Quaternion(0.0, this)

    val rotation = Quaternion(0.0, axis / axis.norm) * sin(angle / 2.0) +
        cos(angle / 2.0)

    return (rotation * origPos * rotation.conjugate()).vector
}

/**
 *  Rotates a position pointed by this vector for `angle` (in radians) with
 *  respect to `axis` that crosses `point`.
 */
fun Vector3D.rotate(point: Vector3D, axis: Vector3D, angle: Double) =
    point + (this - point).rotate(axis, angle)
