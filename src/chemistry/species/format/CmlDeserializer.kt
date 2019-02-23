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

package crul.chemistry.species.format

import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
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
 *  Deserializer for Chemical Markup Language (CML).
 *
 *  To construct an instance of this class, use [newInstance].
 */
open class CmlDeserializer<B : CmlDeserializer<B>>:
    AbstractDeserializer<B>
{
    /**
     *  @param atomBuilder
     *      Builder for constructing atoms. Identifier in this builder is
     *      ignored.
     *
     *  @param bondBuilder
     *      Builder for constructing bonds.
     *
     *  @param complexBuilder
     *      Builder for constructing the complex. Identifier in this builder is
     *      ignored.
     */
    protected constructor(
        atomBuilder: AtomBuilder<*>,
        bondBuilder: BondBuilder<*>,
        complexBuilder: MoleculeComplexBuilder<*>
    ): super(atomBuilder, bondBuilder, complexBuilder)

    protected var _id: String? = null
        private set

    /**
     *  Configures the identifier.
     *
     *  If it is not set, the `id` attribute of the root element, `molecule`,
     *  is used.
     */
    fun id(value: String?): B {
        _id = value
        return _this
    }

    protected var _fromLengthUnit: UnitOfMeasure? = null
        private set

    /**
     *  Configures the unit of length that the coordinates in the CML text are
     *  in.
     *
     *  It must be set before [build] is called.
     */
    fun fromLengthUnit(value: UnitOfMeasure): B {
        if (!value.isCommensurable(BaseDimension.LENGTH.siUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        _fromLengthUnit = value
        return _this
    }

    protected var _toLengthUnit: UnitOfMeasure? = null
        private set

    /**
     *  Configures the unit of length that the coordinates in the deserialized
     *  complex are in.
     *
     *  It must be set before [build] is called.
     */
    fun toLengthUnit(value: UnitOfMeasure): B {
        if (!value.isCommensurable(BaseDimension.LENGTH.siUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        _toLengthUnit = value
        return _this
    }

    override fun <A : Atom> deserialize(text: String):
        MoleculeComplex<A>
    {
        val xpathEvaluator = XPathFactory.newInstance().newXPath()

        // Parse the CML string.
        val cmlDoc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(text)))

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
                        _fromLengthUnit!!,
                        _toLengthUnit!!
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

        complexBuilder.clear()
        complexBuilder.id(_id ?: moleculeNode.getAttribute("id"))

        // Add the atoms to the complex builder.
        for (atom in atomsById.values) {
            complexBuilder.addAtom(atom)
        }

        // Add the bonds to the complex builder.
        for (bond in bonds) {
            complexBuilder.addBond(bond)
        }

        return complexBuilder.build<A>()
    }

    companion object {
        private class CmlDeserializerImpl(
            atomBuilder: AtomBuilder<*>,
            bondBuilder: BondBuilder<*>,
            complexBuilder: MoleculeComplexBuilder<*>
        ): CmlDeserializer<CmlDeserializerImpl>(
            atomBuilder,
            bondBuilder,
            complexBuilder
        )

        /**
         *  Creates an instance of [CmlDeserializer].
         *
         *  For descriptions of the parameters, see [CmlDeserializer].
         */
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            atomBuilder: AtomBuilder<*> =
                AtomBuilder.newInstance(),
            bondBuilder: BondBuilder<*> =
                BondBuilder.newInstance(),
            complexBuilder: MoleculeComplexBuilder<*> =
                MoleculeComplexBuilder.newInstance()
        ): CmlDeserializer<*> =
            CmlDeserializerImpl(
                atomBuilder,
                bondBuilder,
                complexBuilder
            )
    }
}
