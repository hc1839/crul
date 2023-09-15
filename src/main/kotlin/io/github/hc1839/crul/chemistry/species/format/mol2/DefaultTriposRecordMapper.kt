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

package io.github.hc1839.crul.chemistry.species.format.mol2

import io.github.hc1839.crul.chemistry.species.Bond
import io.github.hc1839.crul.chemistry.species.Supermolecule

/**
 *  Default implementation of [TriposRecordMapper].
 */
open class DefaultTriposRecordMapper() : TriposRecordMapper {
    /**
     *  Casts `bondType` as an enumeration name to [TriposBond.BondType].
     *
     *  `supermol` and `bond` are ignored.
     */
    override fun triposBondTypeOf(
        supermol: Supermolecule<TriposAtom>,
        bond: Bond<TriposAtom>,
        bondType: String
    ): TriposBond.BondType =
        enumValueOf<TriposBond.BondType>(bondType)

    /**
     *  `inputRecord` is returned unmodified.
     */
    override fun onMolecule(
        supermol: Supermolecule<TriposAtom>,
        inputRecord: TriposMolecule
    ): TriposMolecule =
        inputRecord

    /**
     *  `inputRecord` is returned unmodified.
     */
    override fun onBond(
        supermol: Supermolecule<TriposAtom>,
        bond: Bond<TriposAtom>,
        inputRecord: TriposBond
    ): TriposBond =
        inputRecord

    /**
     *  Empty list is returned.
     */
    override fun onOther(supermol: Supermolecule<TriposAtom>):
        List<TriposRecord> = listOf()
}
