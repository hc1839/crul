package io.github.hc1839.crul.chemistry.task

import java.net.URI

/**
 *  Task as a node in a dependency tree.
 */
interface TaskNode {
    /**
     *  Hash code depends only on [taskUri].
     */
    abstract override fun hashCode(): Int

    /**
     *  Equality is structural and depends only on [taskUri] and the classes of
     *  the two [TaskNode].
     */
    abstract override fun equals(other: Any?): Boolean

    /**
     *  URI of this task.
     */
    val taskUri: URI

    /**
     *  URIs of the task dependencies.
     */
    val dependencies: Set<URI>

    /**
     *  Whether the specified task is a dependency, direct or indirect, of this
     *  task.
     *
     *  Resolver must return a task such that two tasks are equal if and only
     *  if they have the same URIs.
     *
     *  A task equal to this task can never be a dependency.
     */
    fun hasDependency(
        depTaskUri: URI,
        resolver: (URI) -> TaskNode
    ): Boolean
    {
        return if (depTaskUri != taskUri) {
            depTaskUri in dependencies ||
            dependencies.any {
                resolver.invoke(it).hasDependency(depTaskUri, resolver)
            }
        } else {
            false
        }
    }

    /**
     *  Number of dependencies in the longest dependency line up to this task.
     *
     *  Resolver must return a task such that two tasks are equal if and only
     *  if they have the same URIs.
     *
     *  If there are no dependencies, the dependency level is `0`.
     */
    fun dependencyLevel(resolver: (URI) -> TaskNode): Int =
        if (dependencies.isEmpty()) {
            0
        } else {
            dependencies.map {
                resolver.invoke(it).dependencyLevel(resolver)
            }.max()!! + 1
        }

    /**
     *  Adds a task as a direct dependency.
     *
     *  Implementation may have specific restrictions and throw exceptions if
     *  they are not met.
     *
     *  @param depTaskUri
     *      URI of the task to add as a direct dependency. If it is already a
     *      direct dependency of or directly depends on this task, an exception
     *      is thrown. If it is equal to that of this task, an exception is
     *      thrown.
     *
     *  @return
     *      New task with the added direct dependency.
     */
    fun addDependency(depTaskUri: URI): TaskNode

    /**
     *  Removes a task as a direct dependency.
     *
     *  Implementation may have specific restrictions and throw exceptions if
     *  they are not met.
     *
     *  @param depTaskUri
     *      URI of the task to remove as a direct dependency. If it is equal to
     *      that of this task or is not a direct dependency, an exception is
     *      thrown.
     *
     *  @return
     *      New task without `depTaskUri` as its direct dependency.
     */
    fun removeDependency(depTaskUri: URI): TaskNode
}
