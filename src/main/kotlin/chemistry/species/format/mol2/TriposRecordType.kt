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
    },
    ANCHOR_ATOM {
        override val numDataLines: Int = 1
    },
    ASSOCIATED_ANNOTATION {
        override val numDataLines: Int = 3
    },
    ATOM {
        override val numDataLines: Int = 1
    },
    BOND {
        override val numDataLines: Int = 1
    },
    CENTER_OF_MASS {
        override val numDataLines: Int = 2
    },
    CENTROID {
        override val numDataLines: Int = 2
    },
    COMMENT {
        override val numDataLines: Int = -1
    },
    CRYSIN {
        override val numDataLines: Int = 1
    },
    DATA_FILE {
        override val numDataLines: Int = 1
    },
    DICT {
        override val numDataLines: Int = 1
    },
    EXTENSION_POINT {
        override val numDataLines: Int = 2
    },
    FF_PBC {
        override val numDataLines: Int = 1
    },
    FFCON_ANGLE {
        override val numDataLines: Int = 1
    },
    FFCON_DIST {
        override val numDataLines: Int = 1
    },
    FFCON_MULTI {
        override val numDataLines: Int = 1
    },
    FFCON_RANGE {
        override val numDataLines: Int = 1
    },
    FFCON_TORSION {
        override val numDataLines: Int = 1
    },
    LINE {
        override val numDataLines: Int = 2
    },
    LSPLANE {
        override val numDataLines: Int = 2
    },
    MOLECULE {
        override val numDataLines: Int = 6
    },
    NORMAL {
        override val numDataLines: Int = 2
    },
    QSAR_ALIGN_RULE {
        override val numDataLines: Int = 2
    },
    RING_CLOSURE {
        override val numDataLines: Int = 1
    },
    ROTATABLE_BOND {
        override val numDataLines: Int = 1
    },
    SEARCH_DIST {
        override val numDataLines: Int = 1
    },
    SEARCH_OPTIONS {
        override val numDataLines: Int = 1
    },
    SET {
        override val numDataLines: Int = 2
    },
    SUBSTRUCTURE {
        override val numDataLines: Int = 1
    },
    U_FEAT {
        override val numDataLines: Int = 1
    },
    UNITY_ATOM_ATTR {
        override val numDataLines: Int = -1
    },
    UNITY_BOND_ATTR {
        override val numDataLines: Int = -1
    };

    /**
     *  Number of data lines, including optional ones, that the record has in
     *  Mol2 format. If a record has variable number of data lines, it is `-1`.
     */
    abstract val numDataLines: Int

    /**
     *  Tripos record type as a record type indicator (RTI).
     */
    fun rti(): String =
        "@<TRIPOS>$name"
}
