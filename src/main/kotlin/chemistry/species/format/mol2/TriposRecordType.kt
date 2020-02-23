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

/**
 *  Tripos record type.
 *
 *  Each name corresponds to a record type indicator (RTI) in Mol2.
 */
enum class TriposRecordType {
    ALT_TYPE {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    ANCHOR_ATOM {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    ASSOCIATED_ANNOTATION {
        override val numDataLines: Int = 3

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    ATOM {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            TriposAtom.Builder()
    },
    BOND {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            TriposBond.Builder()
    },
    CENTER_OF_MASS {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    CENTROID {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    COMMENT {
        override val numDataLines: Int = -1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    CRYSIN {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    DATA_FILE {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    DICT {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    EXTENSION_POINT {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    FF_PBC {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    FFCON_ANGLE {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    FFCON_DIST {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    FFCON_MULTI {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    FFCON_RANGE {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    FFCON_TORSION {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    LINE {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    LSPLANE {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    MOLECULE {
        override val numDataLines: Int = 6

        override fun createBuilder(): TriposRecord.Builder? =
            TriposMolecule.Builder()
    },
    NORMAL {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    QSAR_ALIGN_RULE {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    RING_CLOSURE {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    ROTATABLE_BOND {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    SEARCH_DIST {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    SEARCH_OPTIONS {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    SET {
        override val numDataLines: Int = 2

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    SUBSTRUCTURE {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    U_FEAT {
        override val numDataLines: Int = 1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    UNITY_ATOM_ATTR {
        override val numDataLines: Int = -1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    },
    UNITY_BOND_ATTR {
        override val numDataLines: Int = -1

        override fun createBuilder(): TriposRecord.Builder? =
            null
    };

    /**
     *  Number of data lines, including optional ones, that the record has in
     *  Mol2 format. If a record has variable number of data lines, it is `-1`.
     */
    abstract val numDataLines: Int

    /**
     *  Creates a new builder for the type of the Tripos record, or `null` if
     *  the Tripos record is not supported.
     */
    abstract fun createBuilder(): TriposRecord.Builder?

    /**
     *  Tripos record type as a record type indicator (RTI).
     */
    fun rti(): String =
        "@<TRIPOS>$name"
}
