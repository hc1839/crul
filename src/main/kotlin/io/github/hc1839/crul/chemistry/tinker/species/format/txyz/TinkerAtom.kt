package io.github.hc1839.crul.chemistry.tinker.species.format.txyz

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.AtomIsland
import io.github.hc1839.crul.chemistry.species.Element

/**
 *  Tinker atom for the TXYZ format.
 *
 *  @property atomId
 *      Identifier of the Tinker atom that determines the connectivities and
 *      its order in TXYZ.
 *
 *  @property atomType
 *      Atom-type code.
 *
 *  @constructor
 */
data class TinkerAtom(
    override val element: Element,
    override val position: Vector3D,
    val atomId: Int,
    val atomType: Int
) : Atom
{
    override val island: AtomIsland<Atom> =
        AtomIsland(this)
}
