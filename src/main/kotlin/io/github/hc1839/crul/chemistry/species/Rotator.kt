package io.github.hc1839.crul.chemistry.species

import kotlin.math.PI
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.chemistry.species.Atom
import io.github.hc1839.crul.chemistry.species.Supermolecule
import io.github.hc1839.crul.math.geometry.rotate
import io.github.hc1839.crul.permute.variation.ListElementVariator

object Rotator {
    /**
     *  Z-axis as the zenith.
     */
    private val zenith: Vector3D =
        Vector3D(0.0, 0.0, 1.0)

    /**
     *  Generates rotations of a dimer.
     *
     *  Rotation axes used are uniform and unique, but the generated rotations
     *  may not be unique due to any symmetries of the dimer.
     *
     *  @param dimer
     *      Dimer to rotate.
     *
     *  @param numAzimuthalSegments
     *      Number of azimuthal segments to divide into.
     *
     *  @param numPolarSegments
     *      Number of polar segments to divide into.
     *
     *  @param numRotationSegments
     *      Number of rotation segments to divide into.
     *
     *  @param rotatedAtomFactory
     *      Factory of rotated atom given the original, non-rotated atom and
     *      its rotated position.
     *
     *  @return
     *      List of rotated dimers. Rotated dimers are in the order as they
     *      were generated based on the order of the rotation axes from
     *      [RotationAxes], but the order of the rotated dimers is not
     *      significant otherwise.
     */
    @JvmStatic
    fun <A : Atom> generateRotations(
        dimer: Supermolecule<A>,
        numAzimuthalSegments: Int,
        numPolarSegments: Int,
        numRotationSegments: Int,
        rotatedAtomFactory: (A, Vector3D) -> A
    ): List<Supermolecule<A>>
    {
        // TODO: Comment the implementation.

        if (dimer.subspecies.count() != 2) {
            throw IllegalArgumentException(
                "Supermolecule is not a dimer."
            )
        }

        if (numRotationSegments < 1) {
            throw IllegalArgumentException(
                "Number of rotation segments is less than one."
            )
        }

        val rotationAngleStep = 2.0 * PI / numRotationSegments.toDouble()

        val origCentralIslandCentroid = dimer.subspecies[0].centroid()
        val origSatelliteIslandCentroid = dimer.subspecies[1].centroid()

        var centralRotationVariator = ListElementVariator(
            (0 until numRotationSegments).map {
                rotationAngleStep * it.toDouble()
            }
        )

        var satelliteRotationVariator = ListElementVariator(
            (0 until numRotationSegments).map {
                rotationAngleStep * it.toDouble()
            }
        )

        val rotations = mutableListOf<Supermolecule<A>>()

        val centralRotationAxes = RotationAxes(
            numAzimuthalSegments,
            numPolarSegments
        )

        for (centralRotationAxis in centralRotationAxes) {
            for (centralRotationAngle in centralRotationVariator) {
                val rotatedCentralIsland = dimer.subspecies[0].map {
                    rotatedAtomFactory.invoke(
                        it,
                        it.position.rotate(
                            origCentralIslandCentroid,
                            centralRotationAxis,
                            centralRotationAngle
                        )
                    )
                }

                if (
                    Vector3D.angle(centralRotationAxis, zenith) >
                        centralRotationAxes.similarityThresholdAngle &&
                    centralRotationAngle <
                        centralRotationAxes.similarityThresholdAngle
                ) {
                    continue
                }

                val satelliteRotationAxes = RotationAxes(
                    numAzimuthalSegments,
                    numPolarSegments
                )

                for (satelliteRotationAxis in satelliteRotationAxes) {
                    for (satelliteRotationAngle in satelliteRotationVariator) {
                        val rotatedSatelliteIsland = dimer.subspecies[1].map {
                            rotatedAtomFactory.invoke(
                                it,
                                it.position.rotate(
                                    origSatelliteIslandCentroid,
                                    satelliteRotationAxis,
                                    satelliteRotationAngle
                                )
                            )
                        }

                        if (
                            Vector3D.angle(satelliteRotationAxis, zenith) >
                                satelliteRotationAxes
                                    .similarityThresholdAngle &&
                            satelliteRotationAngle <
                                satelliteRotationAxes
                                    .similarityThresholdAngle
                        ) {
                            continue
                        }

                        val rotatedSupermol = Supermolecule(
                            listOf(
                                rotatedCentralIsland,
                                rotatedSatelliteIsland
                            )
                        )

                        rotations.add(rotatedSupermol)
                    }
                }
            }
        }

        return rotations.toList()
    }
}
