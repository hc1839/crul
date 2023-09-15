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
 *  Mapper for transforming supported Tripos records when exporting a
 *  supermolecule to Mol2.
 *
 *  Supported Tripos record types are specified in
 *  [TriposRecordMapper.supportedTypes]. Tripos `ATOM` is not part of the
 *  mapper, since a supermolecule being exported must already have [TriposAtom]
 *  as the type of its atoms.
 */
interface TriposRecordMapper {
    /**
     *  Maps [Bond.bondType] to Tripos bond type.
     */
    fun triposBondTypeOf(
        supermol: Supermolecule<TriposAtom>,
        bond: Bond<TriposAtom>,
        bondType: String
    ): TriposBond.BondType

    /**
     *  Maps Tripos `MOLECULE`.
     *
     *  If [Supermolecule.name] of `supermol` is `null`,
     *  [TriposMolecule.molName] of `inputRecord` is a UUID Version 4;
     *  otherwise, it is [Supermolecule.name]. The returned Tripos record must
     *  not have a `null` [TriposMolecule.molName].
     */
    fun onMolecule(
        supermol: Supermolecule<TriposAtom>,
        inputRecord: TriposMolecule
    ): TriposMolecule

    /**
     *  Maps Tripos `BOND`.
     *
     *  [TriposBond.bondType] of `inputRecord` is the result of [triposBondOf].
     */
    fun onBond(
        supermol: Supermolecule<TriposAtom>,
        bond: Bond<TriposAtom>,
        inputRecord: TriposBond
    ): TriposBond

    /**
     *  Additional Tripos record mapping not directly supported by the other
     *  member functions.
     *
     *  @param supermol
     *      Supermolecule being exported.
     *
     *  @return
     *      Additional Tripos records to write for `supermol`, or an empty list
     *      if there are no additional records. It cannot contain any the
     *      Tripos record types specified in [supportedTypes].
     */
    fun onOther(supermol: Supermolecule<TriposAtom>): List<TriposRecord>

    companion object {
        /**
         *  Set of supported Tripos record types.
         *
         *  [TriposRecordType.ATOM] is included despite that there is no member
         *  function in [TriposRecordMapper] for it (see [TriposRecordMapper]
         *  for the reason).
         */
        val supportedTypes: Set<TriposRecordType> = setOf(
            TriposRecordType.ATOM,
            TriposRecordType.MOLECULE,
            TriposRecordType.BOND
        )
    }
}
