package io.github.hc1839.crul.chemistry.task

/**
 *  Builder for creating directive lists.
 *
 *  @param D
 *      Type of directives in the list.
 */
interface DirectiveListBuilder<D : Directive> {
    /**
     *  Adds a directive to the list.
     */
    fun add(directive: D): DirectiveListBuilder<D>

    /**
     *  Adds all directives of the list in the specified `builder` to the list.
     */
    fun addAll(builder: DirectiveListBuilder<D>): DirectiveListBuilder<D>

    /**
     *  Returns the current list.
     */
    fun build(): List<D>

    companion object {
        /**
         *  Constructs a new instance of [DirectiveListBuilder].
         *
         *  @param D
         *      Type of directives in the list.
         */
        @JvmStatic
        fun <D : Directive> newInstance(): DirectiveListBuilder<D> =
            object : DirectiveListBuilder<D> {
                private val directiveList = mutableListOf<D>()

                override fun add(directive: D): DirectiveListBuilder<D> {
                    directiveList.add(directive)
                    return this
                }

                override fun addAll(builder: DirectiveListBuilder<D>):
                    DirectiveListBuilder<D>
                {
                    directiveList.addAll(builder.build())
                    return this
                }

                override fun build(): List<D> =
                    directiveList.toList()
            }

        /**
         *  Constructs a new instance of [DirectiveListBuilder] initialized
         *  with the specified `list`.
         *
         *  @param D
         *      Type of directives in the list.
         */
        @JvmStatic
        fun <D : Directive> newInstance(list: List<D>):
            DirectiveListBuilder<D>
        {
            val builder = newInstance<D>()

            for (directive in list) {
                builder.add(directive)
            }

            return builder
        }
    }
}

/**
 *  @see [DirectiveListBuilder.newInstance]
 */
fun <D : Directive> DirectiveListBuilder(): DirectiveListBuilder<D> =
    DirectiveListBuilder.newInstance()

/**
 *  @see [DirectiveListBuilder.newInstance]
 */
fun <D : Directive> DirectiveListBuilder(list: List<D>):
    DirectiveListBuilder<D>
{
    return DirectiveListBuilder.newInstance(list)
}
