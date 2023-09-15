package io.github.hc1839.crul.chemistry.species

/**
 *  Supermolecule with its fragmentation.
 *
 *  Fragmentation is implementation-dependent.
 */
interface FragmentedSupermolecule<A : Atom> : Supermolecule<A> {
    /**
     *  Non-empty list of fragments of the supermolecule.
     *
     *  Each atom of the supermolecule exists in exactly one fragment. All
     *  atoms of all fragments exist in the supermolecule.
     *
     *  Fragments are referentially distinct. Order of the fragments is
     *  maintained between evaluations but is implementation-specific.
     */
    val fragments: List<Fragment<A>>

    /**
     *  [fragments] of the returned fragmented supermolecule have the same
     *  fragmentation and have the same ordering as that of this fragmented
     *  supermolecule.
     */
    abstract override fun <R : Atom> map(
        name: String?,
        transform: (A) -> R
    ): FragmentedSupermolecule<R>

    companion object {
        /**
         *  Constructs a new instance of [FragmentedSupermolecule].
         *
         *  @param supermol
         *      Supermolecule that is fragmented.
         *
         *  @param fragments
         *      Fragments of `supermol`.
         *
         *  @param name
         *      Arbitrary name of the fragmented supermolecule, or `null` if
         *      not applicable.
         */
        @JvmStatic
        fun <A : Atom> newInstance(
            supermol: Supermolecule<A>,
            fragments: List<Fragment<A>>,
            name: String? = null
        ): FragmentedSupermolecule<A> =
            DefaultFragmentedSupermolecule(supermol, fragments, name)
    }
}

/**
 *  Constructs a new instance of [FragmentedSupermolecule].
 *
 *  See [FragmentedSupermolecule.newInstance].
 */
fun <A : Atom> FragmentedSupermolecule(
    supermol: Supermolecule<A>,
    fragments: List<Fragment<A>>,
    name: String? = null
): FragmentedSupermolecule<A> =
    FragmentedSupermolecule.newInstance(supermol, fragments, name)
