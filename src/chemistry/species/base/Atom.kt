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

package chemistry.species.base

import math.coordsys.Vector3D

/**
 *  Atom containing additional information (compared to [BasicAtom]) that are
 *  commonly used with read access.
 */
interface Atom<A : Atom<A>> :
    BasicAtom<A>,
    AtomCollection<A>
{
    override abstract var formalCharge: Double
}
