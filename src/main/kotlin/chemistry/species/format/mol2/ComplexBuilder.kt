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

package crul.chemistry.species.format.mol2

import crul.chemistry.species.Atom
import crul.chemistry.species.MoleculeComplex

/**
 *  [Mol2DecodingListener] as a builder of [MoleculeComplex].
 */
interface ComplexBuilder : Mol2DecodingListener {
    /**
     *  Builds a list of molecule complexes from the data received from
     *  [Mol2Decoder].
     */
    fun build(): List<MoleculeComplex<Atom>>
}
