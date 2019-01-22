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

package math.coordsys

/**
 *  Position as a coordinate tuple in three dimensions.
 */
abstract class Position3D : Spatial {
    constructor(component1: Double, component2: Double, component3: Double):
        super(component1, component2, component3)

    /**
     *  Deserialization constructor.
     */
    constructor(msgpack: ByteArray): super(msgpack)

    operator fun component1() = this.components[0]
    operator fun component2() = this.components[1]
    operator fun component3() = this.components[2]

    /**
     *  Converts this position to a vector.
     */
    abstract fun toVector3D(): Vector3D
}
