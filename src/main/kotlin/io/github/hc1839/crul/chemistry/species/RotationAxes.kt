package io.github.hc1839.crul.chemistry.species

import kotlin.math.PI
import kotlin.math.abs
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import io.github.hc1839.crul.apache.math.vector.*
import io.github.hc1839.crul.permute.variation.ListElementVariator
import io.github.hc1839.crul.permute.variation.Variator
import io.github.hc1839.crul.permute.variation.VariatorSequence

/**
 *  Rotation axes for uniformly and uniquely rotating a species.
 *
 *  Rotation axes are in the order as they were generated, but the order is not
 *  significant otherwise.
 *
 *  @param numAzimuthalSegments
 *      Number of azimuthal segments to divide into.
 *
 *  @param numPolarSegments
 *      Number of polar segments to divide into.
 *
 *  @param similarityThresholdAngle
 *      Angle, in radians, at which two axes are different.
 *
 *  @constructor
 */
class RotationAxes private constructor(
    val numAzimuthalSegments: Int,
    val numPolarSegments: Int,
    val similarityThresholdAngle: Double,
    private val rotationAxes: List<Vector3D>
) : List<Vector3D> by rotationAxes
{
    /**
     *  Delegated constructor for separating rotation axes and similarity
     *  threshold angle.
     */
    private constructor(
        numAzimuthalSegments: Int,
        numPolarSegments: Int,
        rotationAxesInfo: RotationAxesInfo
    ): this(
        numAzimuthalSegments,
        numPolarSegments,
        rotationAxesInfo.similarityThresholdAngle,
        rotationAxesInfo.rotationAxes
    )

    constructor(numAzimuthalSegments: Int, numPolarSegments: Int): this(
        numAzimuthalSegments,
        numPolarSegments,
        createRotationAxes(numAzimuthalSegments, numPolarSegments)
    )

    /**
     *  Information about rotation axes.
     *
     *  @param rotationAxes
     *      Rotation axes.
     *
     *  @param similarityThresholdAngle
     *      Angle, in radians, at which two axes are different.
     *
     *  @constructor
     */
    private data class RotationAxesInfo(
        val rotationAxes: List<Vector3D>,
        val similarityThresholdAngle: Double
    )

    companion object {
        /**
         *  Z-axis as the zenith.
         */
        private val zenith: Vector3D =
            Vector3D(0.0, 0.0, 1.0)

        /**
         *  Creates a set of rotation axes for [RotationAxes].
         *
         *  For description of the parameters and the return value, see the
         *  primary constructor of [RotationAxes].
         */
        private fun createRotationAxes(
            numAzimuthalSegments: Int,
            numPolarSegments: Int
        ): RotationAxesInfo
        {
            // TODO: Comment the implementation.

            if (numAzimuthalSegments < 1) {
                throw IllegalArgumentException(
                    "Azimuthal-segment count is less than one."
                )
            }

            if (numPolarSegments < 1) {
                throw IllegalArgumentException(
                    "Polar-segment count is less than one."
                )
            }

            val azimuthalAngleStep = 2.0 * PI / numAzimuthalSegments.toDouble()

            var azimuthalVariator = ListElementVariator(
                (0 until numAzimuthalSegments).map {
                    azimuthalAngleStep * it.toDouble()
                }
            )

            val polarAngleStep = 2.0 * PI / numPolarSegments.toDouble()

            var polarVariator = ListElementVariator(
                (0 until numPolarSegments).map {
                    polarAngleStep * it.toDouble()
                }
            )

            polarVariator.next()

            val polarAngles = mutableListOf<Double>()
            val semicircleAngle = PI

            for (polarValue in polarVariator) {
                polarAngles.add(
                    listOf(
                        polarValue,
                        abs(polarValue - semicircleAngle)
                    ).min()!!
                )
            }

            val polarAngleClosestToZ = polarAngles.min()!!

            val similarityThresholdAngle = listOf(
                Vector3D.angle(
                    SphericalCoordinates(
                        1.0,
                        0.0,
                        polarAngleClosestToZ
                    ).cartesian,
                    SphericalCoordinates(
                        1.0,
                        azimuthalAngleStep,
                        polarAngleClosestToZ
                    ).cartesian
                ),
                listOf(azimuthalAngleStep, polarAngleStep).min()!!
            ).max()!! / 2.0

            polarVariator = polarVariator.begin()

            var sphericalVariator = VariatorSequence(
                listOf(azimuthalVariator, polarVariator),
                true
            )

            val prevRotationAxes = mutableListOf<Vector3D>()
            val rotationAxes = mutableListOf<Vector3D>()

            for (angles in sphericalVariator) {
                val azimuthalAngle = angles[0]
                val polarAngle = angles[1]

                val rotationAxis = SphericalCoordinates(
                    1.0,
                    azimuthalAngle,
                    polarAngle
                ).cartesian

                if (
                    listOf(zenith, -zenith).any { refAxis ->
                        Vector3D.angle(rotationAxis, refAxis) <
                        similarityThresholdAngle
                    } &&
                    azimuthalAngle > similarityThresholdAngle
                ) {
                    continue
                }

                if (!(
                    prevRotationAxes.count() > 0 &&
                    prevRotationAxes.any { prevAxis ->
                        listOf(prevAxis, -prevAxis).any { prevLine ->
                            Vector3D.angle(rotationAxis, prevLine) <
                            similarityThresholdAngle
                        }
                    }
                )) {
                    rotationAxes.add(rotationAxis)
                    prevRotationAxes.add(rotationAxis)
                }
            }

            return RotationAxesInfo(
                rotationAxes = rotationAxes.toList(),
                similarityThresholdAngle = similarityThresholdAngle
            )
        }
    }
}
