package io.github.hc1839.crul.chemistry.task

import java.io.IOException
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path
import java.time.Duration
import javax.json.Json
import javax.json.JsonObjectBuilder
import javax.json.stream.JsonGenerator
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 *  Task specification.
 *
 *  See the corresponding JSON schema file for description of the properties.
 */
data class TaskSpec(
    override val taskUri: URI,
    val tagUris: Set<URI>,
    val calculationComplete: Boolean,
    val estimatedDuration: Duration,
    val numProcs: Int,
    val ioPrefix: String,
    val inputSuffix: String,
    val outputSuffix: String,
    val errorSuffix: String,
    val executablePath: Path,
    val commands: List<String>,
    override val dependencies: Set<URI>
) : TaskNode
{
    init {
        if (taskUri in tagUris) {
            throw IOException(
                "Task URI is also a tag URI."
            )
        }

        if (!(estimatedDuration > Duration.ZERO)) {
            throw IllegalArgumentException(
                "Estimated duration is not greater than zero."
            )
        }

        if (numProcs < 1) {
            throw IllegalArgumentException(
                "Total number of MPI processes is not positive."
            )
        }

        val fileNameSeparator = FileSystems.getDefault().separator

        if (fileNameSeparator in inputSuffix) {
            throw IllegalArgumentException(
                "Suffix for the input file name contains " +
                "'$fileNameSeparator': $inputSuffix"
            )
        }

        if (fileNameSeparator in outputSuffix) {
            throw IllegalArgumentException(
                "Suffix for the output file name contains " +
                "'$fileNameSeparator': $outputSuffix"
            )
        }

        if (fileNameSeparator in errorSuffix) {
            throw IllegalArgumentException(
                "Suffix for the error file name contains " +
                "'$fileNameSeparator': $errorSuffix"
            )
        }

        val ioSuffixes = listOf(
            inputSuffix,
            outputSuffix,
            errorSuffix
        )

        if (ioSuffixes.any { it.isEmpty() }) {
            throw IllegalArgumentException(
                "I/O file name suffix is empty."
            )
        }

        if (
            ioSuffixes.distinct().count() !=
            ioSuffixes.count()
        ) {
            throw IllegalArgumentException(
                "I/O file name suffixes are not unique: " +
                "$ioSuffixes"
            )
        }

        if (!executablePath.isAbsolute()) {
            throw IOException(
                "Not an absolute path: $executablePath"
            )
        }

        if (commands.isEmpty()) {
            throw IllegalArgumentException(
                "Array of commands is empty."
            )
        }

        if (commands.any { it.isEmpty() }) {
            throw IllegalArgumentException(
                "Command string is empty."
            )
        }
    }

    override fun addDependency(depTaskUri: URI): TaskSpec {
        if (depTaskUri == taskUri) {
            throw IllegalArgumentException(
                "Task URI to add as a dependency is equal to that " +
                "of this task."
            )
        }

        if (depTaskUri in dependencies) {
            throw IllegalArgumentException(
                "Task URI to add as a direct dependency is already " +
                "a direct dependency."
            )
        }

        return copy(
            dependencies = dependencies.plusElement(depTaskUri)
        )
    }

    override fun removeDependency(depTaskUri: URI): TaskSpec {
        if (depTaskUri == taskUri) {
            throw IllegalArgumentException(
                "Task URI to remove as a dependency is equal to that " +
                "of this task."
            )
        }

        if (depTaskUri !in dependencies) {
            throw IllegalArgumentException(
                "Task URI to remove is not a direct dependency " +
                "of this task."
            )
        }

        return copy(
            dependencies = dependencies - depTaskUri
        )
    }

    /**
     *  Minimum number of nodes needed for the task.
     */
    fun numNodes(numProcsPerNode: Int): Int =
        ceil(
            numProcs.toDouble() / numProcsPerNode.toDouble()
        ).roundToInt()

    override fun hashCode(): Int =
        taskUri.hashCode()

    override fun equals(other: Any?): Boolean =
        other is TaskSpec &&
        this::class == other::class &&
        (
            taskUri == other.taskUri
        )
}
