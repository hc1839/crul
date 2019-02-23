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

@file:JvmMultifileClass

package crul.chemistry.species.format

import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.math.round
import kotlin.math.roundToInt

import crul.chemistry.species.Atom
import crul.chemistry.species.MoleculeComplex
import crul.float.Comparison.nearlyEquals
import crul.measure.Quantity
import crul.measure.dimension.BaseDimension
import crul.measure.unit.UnitOfMeasure

/**
 *  Functions related to Chemical Markup Language (CML).
 */
object Cml {
    /**
     *  Serializes this complex to CML.
     *
     *  @param fromLengthUnit
     *      The unit of length that the coordinates are in.
     *
     *  @param toLengthUnit
     *      The unit of length that the coordinates in the CML output are in.
     *      Unit information is not stored in the CML output.
     */
    @JvmStatic
    @JvmOverloads
    fun <A : Atom> MoleculeComplex<A>.toCml(
        fromLengthUnit: UnitOfMeasure,
        toLengthUnit: UnitOfMeasure = UnitOfMeasure.parse("Ao")
    ): String
    {
        if (!fromLengthUnit.isCommensurable(BaseDimension.LENGTH.siUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        if (!toLengthUnit.isCommensurable(BaseDimension.LENGTH.siUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        val cmlDoc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument()

        // Create and append the root node.
        val moleculeNode = cmlDoc.createElement("molecule")
        cmlDoc.appendChild(moleculeNode)

        moleculeNode.setAttribute("xmlns", "http://www.xml-cml.org/schema")

        val moleculeFormalCharge = formalCharge

        // Set the formal charge, converting it to an integer if it is.
        moleculeNode.setAttribute(
            "formalCharge",
            if (
                nearlyEquals(
                    moleculeFormalCharge,
                    round(moleculeFormalCharge)
                )
            ) {
                moleculeFormalCharge.roundToInt().toString()
            } else {
                moleculeFormalCharge.toString()
            }
        )

        moleculeNode.setAttribute("id", id)

        // Create and append the node for an array of atoms.
        val atomArrayNode = cmlDoc.createElement("atomArray")
        moleculeNode.appendChild(atomArrayNode)

        // Create and append a node for each atom.
        for (atom in atoms()) {
            val atomNode = cmlDoc.createElement("atom")
            atomArrayNode.appendChild(atomNode)

            atomNode.setAttribute("id", atom.id)
            atomNode.setAttribute("elementType", atom.element.symbol)

            val cmptsByName = listOf("x3", "y3", "z3")
                .zip(atom.position.components)
                .toMap()

            // Set the position of the atom.
            for ((cmptName, cmpt) in cmptsByName) {
                atomNode.setAttribute(
                    cmptName,
                    Quantity
                        .convertUnit(cmpt, fromLengthUnit, toLengthUnit)
                        .toString()
                )
            }

            val atomFormalCharge = atom.formalCharge

            // Set the formal charge, converting it to an integer if it is.
            atomNode.setAttribute(
                "formalCharge",
                if (
                    nearlyEquals(
                        atomFormalCharge,
                        round(atomFormalCharge)
                    )
                ) {
                    atomFormalCharge.roundToInt().toString()
                } else {
                    atomFormalCharge.toString()
                }
            )
        }

        // Create and append the node for an array of bonds.
        val bondArrayNode = cmlDoc.createElement("bondArray")
        moleculeNode.appendChild(bondArrayNode)

        val bonds = molecules()
            .asSequence()
            .flatMap { it.bonds().asSequence() }

        // Create and append a node for each bond.
        for (bond in bonds) {
            val bondNode = cmlDoc.createElement("bond")
            bondArrayNode.appendChild(bondNode)

            bondNode.setAttribute(
                "atomRefs2",
                bond
                    .atoms()
                    .asSequence()
                    .map { it.id }
                    .joinToString(" ")
            )

            bondNode.setAttribute("order", bond.order)
        }

        val transformer = TransformerFactory
            .newInstance()
            .newTransformer()

        val stringWriter = StringWriter()

        transformer.transform(DOMSource(cmlDoc), StreamResult(stringWriter))

        return stringWriter.toString()
    }
}
