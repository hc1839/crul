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

import org.msgpack.core.MessagePack

import chemistry.species.base.BasicAtom as BasicAtomIntf
import chemistry.species.base.BasicComplex as BasicComplexIntf
import chemistry.species.base.BasicFragment as BasicFragmentIntf
import chemistry.species.graph.ComplexGraph
import chemistry.species.graph.ComplexGraphFactory
import hypergraph.Vertex
import serialize.BinarySerializable

/**
 *  Skeletal implementation of [chemistry.species.base.BasicComplex].
 */
abstract class AbstractBasicComplex<A, F> :
    BasicComplexIntf<A, F>,
    BinarySerializable,
    Cloneable
    where A : AbstractBasicAtom<A>,
          F : AbstractBasicFragment<A, F>
{
    override val name: String

    /**
     *  Factory for the indexer.
     */
    protected val indexerFactory: ComplexGraphFactory

    /**
     *  Indexer for the atoms and fragments of the complex.
     */
    protected val indexer: ComplexGraph

    /**
     *  @param name
     *      Name of the complex. It must conform to XML NCName production.
     *
     *  @param indexerFactory
     *      Indexer for the atoms and fragments of the complex.
     */
    constructor(
        name: String,
        indexerFactory: ComplexGraphFactory
    ) {
        if (!xml.Datatype.isNCName(name)) {
            throw IllegalArgumentException(
                "Name does not conform to XML NCName production: $name"
            )
        }

        this.name = name
        this.indexerFactory = indexerFactory
        this.indexer = indexerFactory.create(name)
    }

    @JvmOverloads
    constructor(name: String = uuid.Generator.inNCName()):
        this(name, ComplexGraphFactory())

    /**
     *  Copy constructor.
     *
     *  Fragments and atoms are cloned.
     */
    constructor(other: AbstractBasicComplex<A, F>) {
        this.name = other.name
        this.indexerFactory = other.indexerFactory
        this.indexer = this.indexerFactory.create(this.name)

        @Suppress("UNCHECKED_CAST")
        for (fragment in other.fragments.map { it.clone() as F }) {
            addFragment(fragment)
        }
    }

    /**
     *  Data-based constructor.
     */
    private constructor(ctorArgs: CtorArgs<A, F>): this(ctorArgs.name) {
        for (fragment in ctorArgs.fragments) {
            addFragment(fragment)
        }
    }

    /**
     *  Deserialization constructor.
     */
    constructor(
        msgpack: ByteArray,
        atomFactory: (ByteArray) -> A,
        fragmentFactory: (ByteArray, (ByteArray) -> A) -> F
    ): this(getCtorArgs(msgpack, atomFactory, fragmentFactory))

    override val fragments: List<F>
        get() = indexer
            .getFragmentEdges()
            .map {
                @Suppress("UNCHECKED_CAST")
                (it.proxy as Vertex).userData as F
            }
            .sortedBy { it.name }

    override fun containsFragment(fragmentName: String) =
        indexer.getFragmentEdgeByName(fragmentName) != null

    override fun getFragmentByName(fragmentName: String): F? {
        val fragmentProxy =
            indexer.getFragmentEdgeByName(fragmentName)?.proxy as? Vertex

        if (fragmentProxy == null) {
            return null
        }

        @Suppress("UNCHECKED_CAST")
        return fragmentProxy.userData as F
    }

    override val atoms: List<A>
        get() = fragments.flatMap { it.atoms }.sortedBy { it.name }

    override fun containsAtom(atomName: String) =
        indexer.getAtomVertexByName(atomName) != null

    override fun getAtomByName(atomName: String): A? {
        val atomVertex = indexer.getAtomVertexByName(atomName)

        if (atomVertex == null) {
            return null
        }

        @Suppress("UNCHECKED_CAST")
        return atomVertex.userData as A
    }

    /**
     *  Clones this complex and the fragments and atoms that it contains.
     */
    public override abstract fun clone(): AbstractBasicComplex<A, F>

    /**
     *  MessagePack serialization.
     */
    override fun serialize(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packMapHeader(1)

        packer
            .packString(this::class.qualifiedName)
            .packMapHeader(2)

        val fragmentsBuf = fragments

        packer
            .packString("fragments")
            .packArrayHeader(fragmentsBuf.count())

        for (fragment in fragmentsBuf) {
            val fragmentAsBytes = fragment.serialize()

            packer
                .packBinaryHeader(fragmentAsBytes.count())
                .writePayload(fragmentAsBytes)
        }

        packer
            .packString("name")
            .packString(name)

        packer.close()

        return packer.toByteArray()
    }

    /**
     *  Adds a fragment to the complex.
     *
     *  It is used for initializing an immutable complex with the given
     *  fragments.
     */
    protected fun addFragment(fragment: F) {
        // Whether the fragment is composed of one atom or many.
        if (fragment is AbstractBasicAtom<*>) {
            val atom = fragment

            var newFragmentName: String

            do {
                newFragmentName = uuid.Generator.inNCName()
            } while (
                indexer.getFragmentEdgeByName(newFragmentName) != null
            )

            val fragmentEdge =
                indexer.createFragmentEdge(newFragmentName)

            (fragmentEdge.proxy as Vertex).userData = fragment

            val atomVertex =
                indexer.createAtomVertex(atom.name, newFragmentName)

            atomVertex.userData = atom
        } else {
            val fragmentEdge =
                indexer.createFragmentEdge(fragment.name)

            (fragmentEdge.proxy as Vertex).userData = fragment

            for (atom in fragment.atoms) {
                val atomVertex =
                    indexer.createAtomVertex(atom.name, fragment.name)

                atomVertex.userData = atom
            }
        }
    }

    companion object {
        /**
         *  Constructor arguments.
         */
        private data class CtorArgs<A, F>(
            val fragments: List<F>,
            val name: String
        ) where A : AbstractBasicAtom<A>,
                F : AbstractBasicFragment<A, F>

        /**
         *  Gets the constructors arguments from [serialize].
         */
        private fun <A, F> getCtorArgs(
            msgpack: ByteArray,
            atomFactory: (ByteArray) -> A,
            fragmentFactory: (ByteArray, (ByteArray) -> A) -> F
        ): CtorArgs<A, F>
           where A : AbstractBasicAtom<A>,
                 F : AbstractBasicFragment<A, F>
        {
            val (unpackedMap, _) = BinarySerializable
                .getMapRestPair(
                    msgpack,
                    AbstractBasicComplex::class.qualifiedName!!
                )

            val fragments = unpackedMap["fragments"]!!
                .asArrayValue()
                .list()
                .map {
                    fragmentFactory(
                        it.asBinaryValue().asByteArray(),
                        atomFactory
                    )
                }

            val name = unpackedMap["name"]!!.asStringValue().toString()

            return CtorArgs(fragments, name)
        }
    }
}
