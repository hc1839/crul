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

package chemistry.species

import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import chemistry.species.base.Atom as AtomIntf
import chemistry.species.base.BasicAtom as BasicAtomIntf
import chemistry.species.base.Complex as ComplexIntf
import chemistry.species.base.Fragment as FragmentIntf
import chemistry.species.graph.MoleculeGraph
import chemistry.species.graph.MoleculeGraphFactory
import float.Comparison.nearlyEquals
import hypergraph.Edge
import hypergraph.Vertex
import math.coordsys.Vector3D
import measure.Quantity
import measure.unit.UnitOfMeasure

/**
 *  Implementation of [chemistry.species.base.Complex] for a complex of
 *  molecules.
 *
 *  For this class and subclasses, a molecule is considered to be a fragment
 *  and vice versa.
 */
open class MoleculeComplex<A, F> :
    ComplexIntf<A, F>,
    Cloneable
    where A : AbstractAtom<A>,
          F : AbstractFragment<A, F>
{
    override val name: String

    /**
     *  Factory for a fragment.
     */
    protected val fragmentFactory: (Iterable<A>, String) -> F

    /**
     *  Factory for the indexer.
     */
    protected val indexerFactory: MoleculeGraphFactory

    /**
     *  Factory for the type of indexer to use.
     */
    protected val indexer: MoleculeGraph

    /**
     *  For a description of the rest of the arguments, see other
     *  constructor(s).
     *
     *  @param indexerFactory
     *      Factory for the type of indexer to use.
     */
    protected constructor(
        fragmentFactory: (Iterable<A>, String) -> F,
        name: String,
        indexerFactory: MoleculeGraphFactory
    ) {
        if (!xml.Datatype.isNCName(name)) {
            throw IllegalArgumentException(
                "Name does not conform to XML NCName production: $name"
            )
        }

        this.fragmentFactory = fragmentFactory
        this.name = name
        this.indexerFactory = indexerFactory
        this.indexer = indexerFactory.create(name)
    }

    /**
     *  @param fragmentFactory
     *      Factory that creates a fragment given an iterable of atoms and a
     *      name for the fragment, respectively.
     *
     *  @param name
     *      Name of the complex. It must conform to XML NCName production.
     */
    @JvmOverloads
    constructor(
        fragmentFactory: (Iterable<A>, String) -> F,
        name: String = uuid.Generator.inNCName()
    ): this(fragmentFactory, name, MoleculeGraphFactory())

    /**
     *  Copy constructor.
     */
    constructor(other: MoleculeComplex<A, F>) {
        this.fragmentFactory = other.fragmentFactory
        this.name = other.name
        this.indexerFactory = other.indexerFactory
        this.indexer = this.indexerFactory.create(this.name)

        for (atom in other.atoms.map { it.clone() }) {
            addAtom(atom)
        }

        for (bond in other.bonds) {
            val atomNames = bond.atoms.map { it.name }

            if (atomNames.count() != 2) {
                throw RuntimeException(
                    "[Internal Error] " +
                    "Bond does not contain exactly two atoms."
                )
            }

            addBond(atomNames[0], atomNames[1], bond.order)
        }
    }

    /**
     *  Deserializes from the Chemical Markup Language (CML) format.
     *
     *  @param cmlString
     *      CML as a string.
     *
     *  @param fromLengthUnit
     *      The unit of length that the coordinates in the CML string are in.
     *
     *  @param toLengthUnit
     *      The unit of length that the coordinates in the deserialized complex
     *      are in.
     *
     *  @param atomFactory
     *      Factory that creates an atom given the element, formal charge,
     *      position, and name, respectively.
     *
     *  @param fragmentFactory
     *      Factory that creates a fragment given an iterable of atoms and a
     *      name for the fragment, respectively.
     *
     *  @param name
     *      Name of the complex. If `null`, the `id` attribute of the
     *      `molecule` root node is used. If such attribute does not exist and
     *      this parameter is `null`, a UUID is used.
     */
    @JvmOverloads
    constructor(
        cmlString: String,
        fromLengthUnit: UnitOfMeasure,
        toLengthUnit: UnitOfMeasure,
        atomFactory: (Element, Double, Vector3D, String) -> A,
        fragmentFactory: (Iterable<A>, String) -> F,
        name: String? = null
    ) {
        this.fragmentFactory = fragmentFactory

        if (name != null && !xml.Datatype.isNCName(name)) {
            throw IllegalArgumentException(
                "Name does not conform to XML NCName production: $name"
            )
        }

        val siLengthUnit = UnitOfMeasure.create("m")

        if (!fromLengthUnit.isCommensurable(siLengthUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        if (!toLengthUnit.isCommensurable(siLengthUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        val xpathEvaluator = XPathFactory.newInstance().newXPath()

        // Parse the CML string.
        val cmlDoc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(cmlString)))

        val moleculeNode = cmlDoc.firstChild as org.w3c.dom.Element

        if (moleculeNode.getTagName() != "molecule") {
            throw IllegalArgumentException(
                "Root element is not 'molecule'."
            )
        }

        // Determine the name to use for the complex.
        val nameToUse =
            if (name != null) {
                name
            } else if (moleculeNode.hasAttribute("id")) {
                moleculeNode.getAttribute("id")
            } else {
                uuid.Generator.inNCName()
            }

        this.name = nameToUse
        this.indexerFactory = MoleculeGraphFactory()
        this.indexer = this.indexerFactory.create(nameToUse)

        // Get the node list of atom nodes.
        val atomsNodeList = xpathEvaluator
            .evaluate("/*/atomArray/atom", cmlDoc, XPathConstants.NODESET)
            as NodeList

        // Construct each atom using the information stored in the node.
        val atoms = (0 until atomsNodeList.length)
            .map { index ->
                val atomNode = atomsNodeList.item(index)
                    as org.w3c.dom.Element

                val element = Element(atomNode.getAttribute("elementType"))

                val formalCharge =
                    if (atomNode.hasAttribute("formalCharge")) {
                        atomNode.getAttribute("formalCharge").toDouble()
                    } else {
                        0.0
                    }

                val position = Vector3D(
                    listOf("x3", "y3", "z3").map { cmptName ->
                        Quantity.convertUnit(
                            atomNode.getAttribute(cmptName).toDouble(),
                            fromLengthUnit,
                            toLengthUnit
                        )
                    }
                )

                val atomName = atomNode.getAttribute("id")

                // Construct the atom.
                val atom = atomFactory(
                    element,
                    formalCharge,
                    position,
                    atomName
                )

                // Add the atom to this complex.
                addAtom(atom)

                atom
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
                    atoms
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

        // Add each bond.
        for (bondNode in bondNodes) {
            val atomNames = kotlin.text.Regex("\\s+").split(
                bondNode.getAttribute("atomRefs2").trim()
            )

            if (atomNames.count() != 2) {
                throw RuntimeException(
                    "'bond' element node does not specify " +
                    "exactly two atom names."
                )
            }

            addBond(
                atomNames[0],
                atomNames[1],
                bondNode.getAttribute("order")
            )
        }
    }

    /**
     *  Fragments are dynamically constructed upon each call with different
     *  names.
     */
    override val fragments: List<F>
        get() = indexer
            .graph
            .getEdgesByType(indexer.fragmentEdgeType)
            .map { fragmentEdge ->
                fragmentFactory(
                    fragmentEdge
                        .vertices
                        .map { atomVertex ->
                            @Suppress("UNCHECKED_CAST")
                            (atomVertex.userData as A)
                        },
                    fragmentEdge.id
                )
            }
            .sortedBy { it.name }

    /**
     *  Always raises an exception, since fragments and and their names are
     *  dynamically generated.
     */
    override fun containsFragment(fragmentName: String): Boolean {
        throw RuntimeException(
            "Fragment names are undefined for this implementation."
        )
    }

    /**
     *  Always raises an exception, since fragments and and their names are
     *  dynamically generated.
     */
    override fun getFragmentByName(fragmentName: String): F? {
        throw RuntimeException(
            "Fragment names are undefined for this implementation."
        )
    }

    override val atoms: List<A>
        get() = indexer
            .graph
            .getVerticesByType(indexer.atomVertexType)
            .map {
                @Suppress("UNCHECKED_CAST")
                (it.userData as A)
            }
            .sortedBy { it.name }

    override fun containsAtom(atomName: String) =
        indexer.graph.getVertexByName(atomName)
            ?.isInstanceOf(indexer.atomVertexType) ?: false

    override fun getAtomByName(atomName: String): A? {
        val atomVertex = indexer.graph.getVertexByName(atomName)

        if (atomVertex == null) {
            return null
        } else {
            @Suppress("UNCHECKED_CAST")
            return atomVertex.userData as A
        }
    }

    /**
     *  Adds an atom to the complex.
     */
    fun addAtom(atom: A) {
        val atomVertex = indexer.createAtomVertex(atom.name)
        atomVertex.userData = atom
    }

    /**
     *  Adds (i.e., creates) a bond between two atoms.
     *
     *  The two atoms must already exist in the complex.
     */
    @JvmOverloads
    fun addBond(
        atom1Name: String,
        atom2Name: String,
        order: String = "other"
    ) {
        indexer.createBondEdge(atom1Name, atom2Name, order)
    }

    /**
     *  Bonds of this complex.
     *
     *  Bonds are constructed upon each call.
     */
    open val bonds: List<Bond<A, F>>
        get() {
            val bondEdges = indexer.getBondEdges()

            return bondEdges.map { bondEdge ->
                val atomVertices = bondEdge.vertices

                val atomPair = atomVertices.map {
                    @Suppress("UNCHECKED_CAST")
                    (it.userData as A)
                }

                val order = bondEdge
                    .type
                    .getPropertiesByType(
                        indexer.bondOrderPropertyType
                    )
                    .first()
                    .value

                Bond<A, F>(
                    Friendship,
                    atomPair[0],
                    atomPair[1],
                    order,
                    atomPair.joinToString("_"),
                    this
                )
            }
        }

    /**
     *  Gets the bonds that an atom is participating in.
     *
     *  Bonds are constructed upon each call.
     */
    open fun getBondsByAtom(atomName: String): List<Bond<A, F>> {
        val atomVertex = indexer.getAtomVertexByName(atomName)

        if (atomVertex == null) {
            return listOf<Bond<A, F>>()
        }

        val bondEdges = atomVertex
            .edges
            .filter { it ->
                (it.type as Vertex)
                    .isInstanceOf(indexer.bondEdgeTypeType)
            }

        return bondEdges.map { bondEdge ->
            val atomPair = bondEdge.vertices.map {
                @Suppress("UNCHECKED_CAST")
                (it.userData as A)
            }

            val order = bondEdge
                .type
                .getPropertiesByType(
                    indexer.bondOrderPropertyType
                )
                .first()
                .value

            Bond<A, F>(
                Friendship,
                atomPair[0],
                atomPair[1],
                order,
                atomPair.joinToString("_"),
                this
            )
        }
    }

    /**
     *  Gets the bond edge that two atoms are participating if there is one.
     */
    private fun getBondEdgeByAtomPair(
        atom1Name: String,
        atom2Name: String
    ): Edge?
    {
        val atom1Vertex = indexer.getAtomVertexByName(atom1Name)

        if (atom1Vertex == null) {
            return null
        }

        // Find the bond edges that one of the atoms is participating in.
        val bond1Edges = atom1Vertex.edges.filter { it ->
            (it.type as Vertex).isInstanceOf(indexer.bondEdgeTypeType)
        }

        // Find the bond edge that both atoms are participating in.
        val sharedBondEdges = bond1Edges.filter { it ->
            it.getVertexByName(atom2Name) != null
        }

        return when (sharedBondEdges.count()) {
            0 -> null

            1 -> sharedBondEdges.first()

            else ->
                throw RuntimeException(
                    "[Internal Error] " +
                    "More than one bond edge contain the two atom vertices."
                )
        }
    }

    /**
     *  Gets the bond edge that two atoms are participating if there is one.
     *
     *  Bond is constructed upon each call.
     */
    open fun getBondByAtomPair(
        atom1Name: String,
        atom2Name: String
    ): Bond<A, F>?
    {
        val sharedBondEdge = getBondEdgeByAtomPair(atom1Name, atom2Name)

        return if (sharedBondEdge != null) {
            val atomPair = sharedBondEdge.vertices.map {
                @Suppress("UNCHECKED_CAST")
                (it.userData as A)
            }

            val order = sharedBondEdge
                .type
                .getPropertiesByType(
                    indexer.bondOrderPropertyType
                )
                .first()
                .value

            Bond<A, F>(
                Friendship,
                atomPair[0],
                atomPair[1],
                order,
                atomPair.joinToString("_"),
                this
            )
        } else {
            null
        }
    }

    /**
     *  Gets the atoms that are bonded to a given atom.
     */
    open fun getBondedAtoms(atomName: String): List<A> {
        val bondEdges = indexer
            .getBondEdges()
            .filter { bondEdge ->
                bondEdge.vertices.any { it.names.contains(atomName) }
            }

        return bondEdges.map { bondEdge ->
            @Suppress("UNCHECKED_CAST")
            bondEdge
                .vertices
                .filter { atomVertex ->
                    !atomVertex.names.contains(atomName)
                }
                .first()
                .userData as A
        }
    }

    /**
     *  Gets the fragment that a given atom is in if the atom exists.
     */
    open fun getFragmentWithAtom(atomName: String): F? {
        val atomVertex = indexer.getAtomVertexByName(atomName)

        if (atomVertex == null) {
            return null
        }

        val fragmentEdges = atomVertex
            .getEdgesByType(indexer.fragmentEdgeType)

        return when (fragmentEdges.count()) {
            1 -> {
                val fragmentEdge = fragmentEdges.first()

                fragmentFactory(
                    fragmentEdge.vertices.map {
                        @Suppress("UNCHECKED_CAST")
                        (it.userData as A)
                    },
                    fragmentEdge.id
                )
            }

            else -> throw RuntimeException(
                "[Internal Error] " +
                "Atom vertex is not participating in a fragment edge."
            )
        }
    }

    /**
     *  @suppress
     *
     *  Sets the bond order for two atoms that are bonded to each other.
     *
     *  @param atom1Name
     *      Name of one of the two atoms.
     *
     *  @param atom2Name
     *      Name of the other atom.
     *
     *  @param newOrder
     *      New bond order as an arbitrary string. It must conform to XML
     *      NCName production.
     */
    fun setBondOrder(
        @Suppress("UNUSED_PARAMETER")
        friendAccess: Bond.FriendAccess,
        atom1Name: String,
        atom2Name: String,
        newOrder: String
    ) {
        val sharedBondEdge = getBondEdgeByAtomPair(atom1Name, atom2Name)

        if (sharedBondEdge == null) {
            throw IllegalArgumentException(
                "Atoms are not participating in a bond: " +
                "'$atom1Name' and '$atom2Name'"
            )
        }

        if (!xml.Datatype.isNCName("_" + newOrder)) {
            throw IllegalArgumentException(
                "Order does not conform to XML NCName production " +
                "when prepended with an underscore: _$newOrder"
            )
        }

        sharedBondEdge
            .type
            .getPropertiesByType(indexer.bondOrderPropertyType)
            .first()
            .value = newOrder
    }

    /**
     *  Clones this complex.
     */
    public override fun clone(): MoleculeComplex<A, F> =
        MoleculeComplex<A, F>(this)

    /**
     *  Converts to [Complex].
     *
     *  Atoms are cloned.
     *
     *  This conversion is useful if the fragment names need to remain constant
     *  and bond information is not important.
     */
    fun toComplex(): Complex<A, F> =
        Complex<A, F>(
            fragments.map {
                @Suppress("UNCHECKED_CAST")
                it.clone() as F
            },
            name
        )

    /**
     *  Serializes this complex to the Chemical Markup Language (CML) format.
     *
     *  @param fromLengthUnit
     *      The unit of length that the coordinates are in.
     *
     *  @param toLengthUnit
     *      The unit of length that the coordinates in the CML output are in.
     */
    @JvmOverloads
    fun toCml(
        fromLengthUnit: UnitOfMeasure,
        toLengthUnit: UnitOfMeasure = UnitOfMeasure.create("Ao")
    ): String
    {
        val siLengthUnit = UnitOfMeasure.create("m")

        if (!fromLengthUnit.isCommensurable(siLengthUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        if (!toLengthUnit.isCommensurable(siLengthUnit)) {
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

        moleculeNode.setAttribute("id", name)

        // Create and append the node for an array of atoms.
        val atomArrayNode = cmlDoc.createElement("atomArray")
        moleculeNode.appendChild(atomArrayNode)

        // Create and append a node for each atom.
        for (atom in atoms) {
            val atomNode = cmlDoc.createElement("atom")
            atomArrayNode.appendChild(atomNode)

            atomNode.setAttribute("id", atom.name)
            atomNode.setAttribute("elementType", atom.element.symbol)

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

            val cmptsByName = listOf("x3", "y3", "z3")
                .zip(atom.centroid.components)
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
        }

        // Create and append the node for an array of bonds.
        val bondArrayNode = cmlDoc.createElement("bondArray")
        moleculeNode.appendChild(bondArrayNode)

        // Create and append a node for each bond.
        for (bond in bonds) {
            val bondNode = cmlDoc.createElement("bond")
            bondArrayNode.appendChild(bondNode)

            bondNode.setAttribute(
                "atomRefs2",
                bond.atoms.map { it.name }.joinToString(" ")
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
     *  Serializes this complex to the XYZ format.
     *
     *  @param fromLengthUnit
     *      The unit of length that the coordinates are in.
     *
     *  @param toLengthUnit
     *      The unit of length that the coordinates in the XYZ output are in.
     *
     *  @param separator
     *      Separator to use between columns in the output.
     */
    @JvmOverloads
    fun toXyz(
        fromLengthUnit: UnitOfMeasure,
        toLengthUnit: UnitOfMeasure = UnitOfMeasure.create("Ao"),
        separator: String = " "
    ): String
    {
        val siLengthUnit = UnitOfMeasure.create("m")

        if (!fromLengthUnit.isCommensurable(siLengthUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        if (!toLengthUnit.isCommensurable(siLengthUnit)) {
            throw IllegalArgumentException(
                "Unit of a coordinate must be a unit of length."
            )
        }

        var xyzBuilder = ""

        val atomsBuf = atoms

        xyzBuilder += atomsBuf.count().toString() + "\n"
        xyzBuilder += name + "\n"

        for (atom in atomsBuf) {
            xyzBuilder += atom.element.symbol + separator
            xyzBuilder += atom
                .centroid
                .components
                .map { it.toString() }
                .joinToString(separator)
            xyzBuilder += "\n"
        }

        xyzBuilder = xyzBuilder.trim() + "\n"

        return xyzBuilder
    }

    private object FriendKey : visaccess.FriendKey()

    open class FriendAccess(key: visaccess.FriendKey) :
        visaccess.FriendAccess
    {
        init {
            if (key != FriendKey) {
                throw IllegalArgumentException(
                    "Invalid friend key."
                )
            }
        }
    }

    private object Friendship : FriendAccess(FriendKey)
}
