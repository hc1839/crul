package io.github.hc1839.crul.chemistry.tinker.keyword

import java.nio.file.Path
import java.nio.file.Paths
import javax.json.Json
import javax.json.JsonObject

/**
 *  Predefined parameter sets.
 */
internal object PredefinedParamsSet {
    /**
     *  Path to the directory containing the predefined parameter sets as JSON
     *  files.
     */
    val paramsSetDirPath: Path =
        Paths.get("/io/github/hc1839/crul/chemistry/tinker/params")

    /**
     *  Gets a predefined parameter set.
     *
     *  @param paramsName
     *      Name of a predefined parameter set (e.g., `amoeba09`). A JAR entry
     *      with the same base name as `paramsName` and with extension
     *      '`.json`' must exist under
     *      `/io/github/hc1839/crul/chemistry/tinker/params`.
     *
     *  @return
     *      Predefined parameter set as a new JSON object.
     */
    fun get(paramsName: String): JsonObject =
        Json.createReader(
            this::class.java.getResourceAsStream(
                paramsSetDirPath.resolve("$paramsName.json").toString()
            )
        ).readObject()
}
