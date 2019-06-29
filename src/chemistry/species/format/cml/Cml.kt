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

package crul.chemistry.species.format.cml

import java.io.Reader
import java.io.Writer
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
import crul.chemistry.species.Bond
import crul.chemistry.species.Element
import crul.chemistry.species.Molecule
import crul.chemistry.species.MoleculeComplex
import crul.chemistry.species.MoleculeComplexBuilder
import crul.chemistry.species.SpeciesSetElement
import crul.float.Comparison.nearlyEquals
import crul.math.coordsys.Vector3D
import crul.measure.Quantity
import crul.measure.dimension.BaseDimension
import crul.measure.dimension.Dimension
import crul.measure.unit.UnitOfMeasure

/**
 *  Exports this molecule complex in CML format.
 *
 *  @param writer
 *      Writer of CML with a trailing newline.
 *
 *  @param complexId
 *      Non-empty string to use as the complex identifier in the CML output.
 *
 *  @param fromLengthUnit
 *      The unit of length that the coordinates are in.
 *
 *  @param toLengthUnit
 *      The unit of length that the coordinates in the CML output are in. Unit
 *      information is not stored in the CML output.
 *
 *  @param atomIdMapper
 *      Identifier to use for an atom in the CML output given the atom and this
 *      complex.
 */
@JvmOverloads
fun <A : Atom> MoleculeComplex<A>.exportCml(
    writer: Writer,
    complexId: String,
    fromLengthUnit: UnitOfMeasure,
    toLengthUnit: UnitOfMeasure = UnitOfMeasure.parse("Ao"),
    atomIdMapper: (A, MoleculeComplex<A>) -> String
)
{
    if (complexId.isEmpty()) {
        throw IllegalArgumentException(
            "Complex identifier is empty."
        )
    }

    if (!fromLengthUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
        throw IllegalArgumentException(
            "Unit of a coordinate must be a unit of length."
        )
    }

    if (!toLengthUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
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

    val moleculeFormalCharge = charge.roundToInt()

    // Set the formal charge, converting it to an integer if it is.
    moleculeNode.setAttribute(
        "formalCharge",
        moleculeFormalCharge.toString()
    )

    moleculeNode.setAttribute("id", complexId)

    // Create and append the node for an array of atoms.
    val atomArrayNode = cmlDoc.createElement("atomArray")
    moleculeNode.appendChild(atomArrayNode)

    // For serializing the bonds.
    val atomIdsByAtom = mutableMapOf<SpeciesSetElement<A>, String>()

    // Create and append a node for each atom.
    for (atom in atoms()) {
        val atomNode = cmlDoc.createElement("atom")
        atomArrayNode.appendChild(atomNode)

        val atomId = atomIdMapper.invoke(atom, this)

        // Store the atom ID for bond serialization.
        atomIdsByAtom[SpeciesSetElement(atom)] = atomId

        atomNode.setAttribute("id", atomId)
        atomNode.setAttribute("elementType", atom.element.symbol)

        val cmptsByName = listOf("x3", "y3", "z3")
            .zip(atom.centroid.components)
            .toMap()

        // Set the centroid of the atom.
        for ((cmptName, cmpt) in cmptsByName) {
            atomNode.setAttribute(
                cmptName,
                Quantity
                    .convertUnit(cmpt, fromLengthUnit, toLengthUnit)
                    .toString()
            )
        }

        val atomFormalCharge = atom.charge.roundToInt()

        // Set the formal charge, converting it to an integer if it is.
        atomNode.setAttribute(
            "formalCharge",
            atomFormalCharge.toString()
        )
    }

    // Create and append the node for an array of bonds.
    val bondArrayNode = cmlDoc.createElement("bondArray")
    moleculeNode.appendChild(bondArrayNode)

    // Create and append a node for each bond.
    for (
        bond in mapNotNull {
            @Suppress("UNCHECKED_CAST")
            (it as? Molecule<A>)?.bonds()
        }.flatten()
    ) {
        val bondNode = cmlDoc.createElement("bond")
        bondArrayNode.appendChild(bondNode)

        bondNode.setAttribute(
            "atomRefs2",
            bond
                .atoms()
                .map { atom ->
                    atomIdsByAtom[SpeciesSetElement(atom)]!!
                }
                .joinToString(" ")
        )

        bondNode.setAttribute("order", bond.order)
    }

    val transformer = TransformerFactory
        .newInstance()
        .newTransformer()

    transformer.transform(DOMSource(cmlDoc), StreamResult(writer))

    writer.write("\n")
    writer.flush()
}

/**
 *  Parses CML format.
 *
 *  The `id` attribute of the root element, `molecule`, is ignored.
 *
 *  @param reader
 *      Reader from which CML is to be read.
 *
 *  @param fromLengthUnit
 *      Unit of length that the coordinates in the CML are in.
 *
 *  @param toLengthUnit
 *      Unit of length that the coordinates in the deserialized complex are in.
 *
 *  @param atomTagMapper
 *      Atom tag given an atom identifier from the CML input. If `null`, atom
 *      tags are not set.
 *
 *  @return
 *      Deserialized complex from `reader`.
 */
@JvmOverloads
fun MoleculeComplex.Companion.parseCml(
    reader: Reader,
    fromLengthUnit: UnitOfMeasure,
    toLengthUnit: UnitOfMeasure,
    atomTagMapper: ((String) -> Int)? = null
): MoleculeComplex<Atom>
{
    if (!fromLengthUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
        throw IllegalArgumentException(
            "Unit of a coordinate must be a unit of length."
        )
    }

    if (!toLengthUnit.isUnitOf(Dimension(BaseDimension.LENGTH))) {
        throw IllegalArgumentException(
            "Unit of a coordinate must be a unit of length."
        )
    }

    val xpath = XPathFactory.newInstance().newXPath()

    // Parse the CML.
    val cmlDoc = DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse(InputSource(reader))

    val moleculeNode = cmlDoc.firstChild as org.w3c.dom.Element

    if (moleculeNode.tagName != "molecule") {
        throw IllegalArgumentException(
            "Root element is not 'molecule'."
        )
    }

    // Get the node list of atom nodes.
    val atomsNodeList = xpath
        .evaluate("/*/atomArray/atom", cmlDoc, XPathConstants.NODESET)
        as NodeList

    val atomsById: MutableMap<String, Atom> = mutableMapOf()

    // Construct each atom using the information stored in the node.
    for (index in 0 until atomsNodeList.length) {
        val atomNode = atomsNodeList.item(index)
            as org.w3c.dom.Element

        val element = Element(atomNode.getAttribute("elementType"))

        val positionCmpts =
            listOf("x3", "y3", "z3").map { cmptName ->
                Quantity.convertUnit(
                    atomNode.getAttribute(cmptName).toDouble(),
                    fromLengthUnit,
                    toLengthUnit
                )
            }

        val centroid = Vector3D(
            positionCmpts[0],
            positionCmpts[1],
            positionCmpts[2]
        )

        val charge =
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

        val atomTag = atomTagMapper?.invoke(atomId)

        // Construct the atom.
        val atom: Atom = if (atomTag != null) {
            Atom.newInstance(
                element,
                centroid,
                charge,
                atomTag
            )
        } else {
            Atom.newInstance(
                element,
                centroid,
                charge
            )
        }

        atomsById[atomId] = atom
    }

    // Check that the formal charge specified in the root node matches the
    // sum of the formal charges of the atoms.
    if (moleculeNode.hasAttribute("formalCharge")) {
        val molecularFormalCharge = moleculeNode
            .getAttribute("formalCharge")
            .toInt()

        if (
            molecularFormalCharge !=
                atomsById
                    .values
                    .map { it.charge.roundToInt() }
                    .reduce { acc, item -> acc + item }
        ) {
            throw RuntimeException(
                "Charge of the complex does not equal " +
                "to the sum of the charges of the atoms."
            )
        }
    }

    // Get the node list of bond nodes.
    val bondsNodeList = xpath
        .evaluate("/*/bondArray/bond", cmlDoc, XPathConstants.NODESET)
        as NodeList

    val bondNodes = (0 until bondsNodeList.length)
        .map { index ->
            bondsNodeList.item(index) as org.w3c.dom.Element
        }

    val bonds: MutableList<Bond<*>> = mutableListOf()

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
            Bond.newInstance<Atom>(
                atomsById[atomRefids[0]]!!,
                atomsById[atomRefids[1]]!!,
                bondNode.getAttribute("order")
            )
        )
    }

    val complexBuilder = MoleculeComplexBuilder.newInstance()

    // Add the bonds to the builder.
    for (bond in bonds) {
        complexBuilder.addBond(bond)
    }

    // Set of wrapped atoms that are participating in a bond.
    val wrappedBondedAtoms = bonds
        .flatMap { bond -> bond.atoms() }
        .map { atom -> SpeciesSetElement(atom) }
        .toSet()

    // Add atoms that are not participating in a bond to the builder.
    for (
        atom in
        atomsById.values.filter { atom ->
            SpeciesSetElement(atom) !in wrappedBondedAtoms
        }
    ) {
        complexBuilder.addAtom(atom)
    }

    return complexBuilder.build<Atom>()
}
