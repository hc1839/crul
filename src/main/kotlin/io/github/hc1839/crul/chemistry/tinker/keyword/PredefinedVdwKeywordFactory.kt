package io.github.hc1839.crul.chemistry.tinker.keyword

import javax.json.JsonObject

import io.github.hc1839.crul.measure.Quantity
import io.github.hc1839.crul.measure.unit.SiUnitSystem
import io.github.hc1839.crul.measure.unit.UnitSystem
import io.github.hc1839.crul.chemistry.tinker.AtomClass

/**
 *  Factory for `vdw` keywords from a predefined parameter set.
 *
 *  Each instantiation causes a JSON file containing `vdw` keywords to be
 *  parsed.
 */
class PredefinedVdwKeywordFactory {
    /**
     *  Name of the predefined parameter set.
     */
    val paramsName: String

    /**
     *  `vdw` keywords as a map of JSON objects.
     */
    private val vdwJsonObjectMap: Map<AtomClass, JsonObject>

    /**
     *  Unit system that the values in [VdwKeyword] instances are in.
     */
    private val toUnitSystem: UnitSystem

    /**
     *  @param paramsName
     *      Name of a predefined parameter set (e.g., `amoeba09`). A JAR entry
     *      with the same base name as `paramsName` and with extension
     *      '`.json`' must exist under `/crul/chemistry/tinker/params`.
     *
     *  @param toUnitSystem
     *      Unit system that the values in [VdwKeyword] instances are in.
     */
    constructor(paramsName: String, toUnitSystem: UnitSystem) {
        this.paramsName = paramsName

        this.vdwJsonObjectMap = PredefinedParamsSet
            .get(paramsName)
            .getJsonObject("vdw")
            .mapKeys { (key, _) ->
                AtomClass(key.toInt())
            }
            .mapValues { (_, jsonValue) ->
                jsonValue.asJsonObject()
            }

        this.toUnitSystem = toUnitSystem
    }

    /**
     *  Atom classes of the `vdw` keywords that this factory can create.
     */
    val atomClasses: Set<AtomClass>
        get() = vdwJsonObjectMap.keys

    /**
     *  Creates the vdW definition of an atom class.
     *
     *  Each invocation constructs a new instance.
     *
     *  @param atomClass
     *      Atom class for which the definition is to be retrieved. If there is
     *      no such atom class, an exception is raised.
     *
     *  @return
     *      VdW definition of the atom class.
     */
    fun createKeyword(atomClass: AtomClass): VdwKeyword {
        if (!vdwJsonObjectMap.containsKey(atomClass)) {
            throw IllegalArgumentException(
                "No such atom class in '$paramsName': ${atomClass.code}"
            )
        }

        val vdwJsonObject = vdwJsonObjectMap[atomClass]!!

        // VdW parameters in the JSON file are in SI unit system.
        return VdwKeyword(
            atomClass = atomClass,
            rmin = Quantity.convertUnit(
                vdwJsonObject.getJsonNumber("rmin").doubleValue(),
                VdwKeywordUnits.rminDim,
                SiUnitSystem,
                toUnitSystem
            ),
            epsilon = Quantity.convertUnit(
                vdwJsonObject.getJsonNumber("epsilon").doubleValue(),
                VdwKeywordUnits.epsilonDim,
                SiUnitSystem,
                toUnitSystem
            ),
            beta = if (!vdwJsonObject.isNull("beta")) {
                vdwJsonObject.getJsonNumber("beta").doubleValue()
            } else {
                null
            }
        )
    }
}
