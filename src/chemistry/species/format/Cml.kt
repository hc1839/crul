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

@file:JvmName("Cml")
@file:JvmMultifileClass

package crul.chemistry.species.format

import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.math.round
import kotlin.math.roundToInt
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import crul.chemistry.species.Atom
import crul.chemistry.species.AtomBuilder
import crul.chemistry.species.Bond
import crul.chemistry.species.BondBuilder
import crul.chemistry.species.Element
import crul.chemistry.species.MoleculeComplex
import crul.chemistry.species.MoleculeComplexBuilder
import crul.float.Comparison.nearlyEquals
import crul.math.coordsys.Vector3D
import crul.measure.Quantity
import crul.measure.dimension.BaseDimension
import crul.measure.unit.UnitOfMeasure

/**
 *  Serializes this complex to CML.
 *
 *  @param fromLengthUnit
 *      The unit of length that the coordinates are in.
 *
 *  @param toLengthUnit
 *      The unit of length that the coordinates in the CML output are in. Unit
 *      information is not stored in the CML output.
 */
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

/**
 *  Parses CML, and imports the constructs.
 *
 *  The `id` attribute of the root element, `molecule`, is used to set the
 *  complex identifier in this builder.
 *
 *  Constructs in the CML are merged in to the data in this builder.
 *
 *  @param cml
 *      CML to parse and import.
 *
 *  @param fromLengthUnit
 *      Unit of length that the coordinates in the CML are in.
 *
 *  @param toLengthUnit
 *      Unit of length that the coordinates in the deserialized complex are in.
 *
 *  @param atomBuilder
 *      Builder for constructing atoms. Identifier in this builder is ignored.
 *
 *  @param bondBuilder
 *      Builder for constructing bonds.
 */
@JvmOverloads
fun <A : Atom> MoleculeComplexBuilder<*>.parseIn(
    cml: String,
    fromLengthUnit: UnitOfMeasure,
    toLengthUnit: UnitOfMeasure,
    atomBuilder: AtomBuilder<*> = AtomBuilder.newInstance(),
    bondBuilder: BondBuilder<*> = BondBuilder.newInstance()
): MoleculeComplexBuilder<*>
{
    val xpathEvaluator = XPathFactory.newInstance().newXPath()

    // Parse the CML.
    val cmlDoc = DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse(InputSource(StringReader(cml)))

    val moleculeNode = cmlDoc.firstChild as org.w3c.dom.Element

    if (moleculeNode.tagName != "molecule") {
        throw IllegalArgumentException(
            "Root element is not 'molecule'."
        )
    }

    // Get the node list of atom nodes.
    val atomsNodeList = xpathEvaluator
        .evaluate("/*/atomArray/atom", cmlDoc, XPathConstants.NODESET)
        as NodeList

    val atomsById: MutableMap<String, A> = mutableMapOf()

    // Construct each atom using the information stored in the node.
    for (index in 0 until atomsNodeList.length) {
        val atomNode = atomsNodeList.item(index)
            as org.w3c.dom.Element

        val element = Element(atomNode.getAttribute("elementType"))

        val position = Vector3D(
            listOf("x3", "y3", "z3").map { cmptName ->
                Quantity.convertUnit(
                    atomNode.getAttribute(cmptName).toDouble(),
                    fromLengthUnit,
                    toLengthUnit
                )
            }
        )

        val formalCharge =
            if (atomNode.hasAttribute("formalCharge")) {
                atomNode.getAttribute("formalCharge").toDouble()
            } else {
                0.0
            }

        val atomId = atomNode.getAttribute("id")

        if (atomsById.containsKey(atomId)) {
            throw RuntimeException(
                "Atom with the same ID has already been added: $atomId"
            )
        }

        // Construct the atom.
        val atom = atomBuilder
            .element(element)
            .position(position)
            .formalCharge(formalCharge)
            .id(atomId)
            .build()

        @Suppress("UNCHECKED_CAST")
        atomsById[atomId] = atom as A
    }

    // Check that the formal charge specified in the root node matches the
    // sum of the formal charges of the atoms.
    if (moleculeNode.hasAttribute("formalCharge")) {
        val molecularFormalCharge = moleculeNode
            .getAttribute("formalCharge")
            .toDouble()

        if (
            !nearlyEquals(
                molecularFormalCharge,
                atomsById
                    .values
                    .map { it.formalCharge }
                    .reduce { acc, item -> acc + item }
            )
        ) {
            throw RuntimeException(
                "Formal charge of the complex does not equal " +
                "to the sum of the formal charges of the atoms."
            )
        }
    }

    // Get the node list of bond nodes.
    val bondsNodeList = xpathEvaluator
        .evaluate("/*/bondArray/bond", cmlDoc, XPathConstants.NODESET)
        as NodeList

    val bondNodes = (0 until bondsNodeList.length)
        .map { index ->
            bondsNodeList.item(index) as org.w3c.dom.Element
        }

    val bonds: MutableList<Bond<Atom>> = mutableListOf()

    // Construct each bond.
    for (bondNode in bondNodes) {
        val atomRefids = bondNode
            .getAttribute("atomRefs2")
            .trim()
            .split(Regex("\\s+"))

        if (atomRefids.count() != 2) {
            throw RuntimeException(
                "'bond' element node does not specify " +
                "exactly two atom names."
            )
        }

        bonds.add(
            bondBuilder
                .atom1(atomsById[atomRefids[0]]!!)
                .atom2(atomsById[atomRefids[1]]!!)
                .order(bondNode.getAttribute("order"))
                .build()
        )
    }

    id(moleculeNode.getAttribute("id"))

    // Add the atoms to the builder.
    for (atom in atomsById.values) {
        addAtom(atom)
    }

    // Add the bonds to the builder.
    for (bond in bonds) {
        addBond(bond)
    }

    return this
}
