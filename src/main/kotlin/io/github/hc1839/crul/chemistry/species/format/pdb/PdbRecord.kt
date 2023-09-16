package io.github.hc1839.crul.chemistry.species.format.pdb

/**
 *  PDB record.
 */
interface PdbRecord {
    /**
     *  Type of this record.
     */
    val type: Type

    /**
     *  Record type.
     */
    enum class Type {
        ATOM {
            override val numColumns: Int =
                80
        },
        CONECT {
            override val numColumns: Int =
                31
        },
        TER {
            override val numColumns: Int =
                27
        };

        /**
         *  Number of expected columns.
         */
        abstract val numColumns: Int

        /**
         *  Record name padded to six characters with left justification.
         */
        val paddedName: String =
            "%-${PdbRecord.paddedNameLength}s".format(name)
    }

    companion object {
        /**
         *  Number of columns in the first field of any record.
         */
        val paddedNameLength: Int =
            6
    }
}
