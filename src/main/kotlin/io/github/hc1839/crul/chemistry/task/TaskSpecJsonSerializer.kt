package io.github.hc1839.crul.chemistry.task

import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import javax.json.Json
import javax.json.JsonString
import javax.json.stream.JsonGenerator

import org.leadpony.justify.api.JsonValidationService

import io.github.hc1839.crul.serialize.Serializer

/**
 *  Serializer of [TaskSpec] of a task.
 *
 *  Serialization of [TaskSpec] is stored in a `Reader` as a JSON object, whose
 *  schema is at `/io/github/hc1839/crul/chemistry/task/TaskSpec.json`.
 */
object TaskSpecJsonSerializer : Serializer<TaskSpec, Reader> {
    /**
     *  JSON validation service.
     */
    private val jsonService =
        JsonValidationService.newInstance()

    /**
     *  JSON schema.
     */
    private val inputSchema = jsonService.readSchema(
        this::class.java.getResourceAsStream(
            "/io/github/hc1839/crul/chemistry/task/TaskSpec.json"
        )
    )

    /**
     *  Problem handler for JSON validation.
     */
    private val jsonHandler =
        jsonService.createProblemPrinter(::println)

    /**
     *  `args` is not used.
     */
    override fun serialize(
        obj: TaskSpec,
        vararg args: Any?
    ): Reader
    {
        val taskSpecObjBuilder = Json.createObjectBuilder()

        taskSpecObjBuilder.add(
            "task_uri",
            obj.taskUri.toString()
        )

        taskSpecObjBuilder.add(
            "tag_uris",
            obj.tagUris.fold(Json.createArrayBuilder()) {
                builder, tagUri ->

                builder.add(tagUri.toString())
            }
        )

        taskSpecObjBuilder.add(
            "calculation_complete",
            obj.calculationComplete
        )

        taskSpecObjBuilder.add(
            "estimated_duration",
            obj.estimatedDuration.toString()
        )

        val parallelizationObjBuilder = Json.createObjectBuilder(
            mapOf(
                "num_procs" to obj.numProcs
            )
        )

        taskSpecObjBuilder.add(
            "parallelization",
            parallelizationObjBuilder
        )

        val ioFileNamesObjBuilder = Json.createObjectBuilder()

        ioFileNamesObjBuilder.add(
            "io_prefix",
            obj.ioPrefix
        )

        ioFileNamesObjBuilder.add(
            "input_suffix",
            obj.inputSuffix
        )

        ioFileNamesObjBuilder.add(
            "output_suffix",
            obj.outputSuffix
        )

        ioFileNamesObjBuilder.add(
            "error_suffix",
            obj.errorSuffix
        )

        taskSpecObjBuilder.add(
            "io_file_names",
            ioFileNamesObjBuilder
        )

        taskSpecObjBuilder.add(
            "executable_path",
            obj.executablePath.toString()
        )

        taskSpecObjBuilder.add(
            "commands",
            Json.createArrayBuilder(obj.commands)
        )

        taskSpecObjBuilder.add(
            "dependencies",
            obj.dependencies.fold(Json.createArrayBuilder()) {
                builder, dependency ->

                builder.add(dependency.toString())
            }
        )

        val writer = StringWriter()

        val jsonWriter = Json.createWriterFactory(
            mapOf(
                JsonGenerator.PRETTY_PRINTING to true
            )
        ).createWriter(writer)

        jsonWriter.use {
            it.write(taskSpecObjBuilder.build())
        }

        return StringReader(writer.toString())
    }

    /**
     *  `args` is not used.
     */
    override fun deserialize(
        dataFormat: Reader,
        vararg args: Any?
    ): TaskSpec
    {
        val inputJsonObj = jsonService.createReader(
            dataFormat,
            inputSchema,
            jsonHandler
        ).readObject()

        val taskUri = URI(inputJsonObj.getString("task_uri"))

        val tagUris = inputJsonObj.getJsonArray("tag_uris").map {
            it as JsonString
            URI(it.getString())
        }.toSet()

        val calculationComplete =
            inputJsonObj.getBoolean("calculation_complete")

        val estimatedDuration = Duration.parse(
            inputJsonObj.getString("estimated_duration")
        )

        val parallelization =
            inputJsonObj.getJsonObject("parallelization")

        val numProcs =
            parallelization.getInt("num_procs")

        val ioFileNamesSpec =
            inputJsonObj.getJsonObject("io_file_names")

        val ioPrefix =
            ioFileNamesSpec.getString("io_prefix")

        val inputSuffix =
            ioFileNamesSpec.getString("input_suffix")

        val outputSuffix =
            ioFileNamesSpec.getString("output_suffix")

        val errorSuffix =
            ioFileNamesSpec.getString("error_suffix")

        val commands = inputJsonObj
            .getJsonArray("commands")
            .map { (it as JsonString).getString() }

        val executablePath = Paths.get(
            inputJsonObj.getString("executable_path")
        )

        val dependencies = inputJsonObj
            .getJsonArray("dependencies")
            .map {
                it as JsonString
                URI(it.getString())
            }
            .toSet()

        return TaskSpec(
            taskUri = taskUri,
            tagUris = tagUris,
            calculationComplete = calculationComplete,
            estimatedDuration = estimatedDuration,
            numProcs = numProcs,
            ioPrefix = ioPrefix,
            inputSuffix = inputSuffix,
            outputSuffix = outputSuffix,
            errorSuffix = errorSuffix,
            executablePath = executablePath,
            commands = commands,
            dependencies = dependencies
        )
    }
}
