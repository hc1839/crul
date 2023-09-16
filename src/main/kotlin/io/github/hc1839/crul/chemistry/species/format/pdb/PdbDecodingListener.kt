package io.github.hc1839.crul.chemistry.species.format.pdb

/**
 *  Listener of [PdbRecord] instances passed by [PdbDecoder].
 */
interface PdbDecodingListener {
    /**
     *  [PdbRecord.Type.ATOM] record.
     */
    fun onAtom(record: PdbAtom) { }

    /**
     *  [PdbRecord.Type.CONECT] record.
     */
    fun onConect(record: PdbConect) { }

    /**
     *  [PdbRecord.Type.TER] record.
     */
    fun onTer(record: PdbTer) { }
}
