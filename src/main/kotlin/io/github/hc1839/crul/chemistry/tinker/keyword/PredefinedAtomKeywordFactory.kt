package io.github.hc1839.crul.chemistry.tinker.keyword

import javax.json.JsonObject

import io.github.hc1839.crul.chemistry.tinker.AtomClass

/**
 *  Factory for `atom` keywords from a predefined parameter set.
 *
 *  Each instantiation causes a JSON file containing `atom` keywords to be
 *  parsed.
 */
class PredefinedAtomKeywordFactory {
    /**
     *  Name of the predefined parameter set.
     */
    val paramsName: String

    /**
     *  `atom` keywords as a map of JSON objects.
     */
    private val atomJsonObjectMap: Map<Int, JsonObject>

    /**
     *  @param paramsName
     *      Name of a predefined parameter set (e.g., `amoeba09`). A JAR entry
     *      with the same base name as `paramsName` and with extension
     *      '`.json`' must exist under
     *      `/io/github/hc1839/crul/chemistry/tinker/params`.
     */
    constructor(paramsName: String) {
        this.paramsName = paramsName

        this.atomJsonObjectMap = PredefinedParamsSet
            .get(paramsName)
            .getJsonObject("atom")
            .mapKeys { (key, _) ->
                key.toInt()
            }
            .mapValues { (_, jsonValue) ->
                jsonValue.asJsonObject()
            }
    }

    /**
     *  Atom types of the `atom` keywords that this factory can create.
     */
    val atomTypeCodes: Set<Int>
        get() = atomJsonObjectMap.keys

    /**
     *  Creates the definition of an atom type.
     *
     *  Each invocation constructs a new instance.
     *
     *  @param atomTypeCode
     *      Integer code of the atom type for which the definition is to be
     *      retrieved. If there is no such atom type, an exception is raised.
     *
     *  @return
     *      Definition of the atom type.
     */
    fun createKeyword(atomTypeCode: Int): AtomKeyword {
        if (!atomJsonObjectMap.containsKey(atomTypeCode)) {
            throw IllegalArgumentException(
                "No such atom type in '$paramsName': ${atomTypeCode}"
            )
        }

        val atomJsonObject = atomJsonObjectMap[atomTypeCode]!!

        return AtomKeyword(
            atomTypeCode = atomTypeCode,
            atomClass = AtomClass(atomJsonObject.getInt("atom_class")),
            typeName = atomJsonObject.getString("type_name"),
            description = atomJsonObject.getString("description"),
            atomicNumber = atomJsonObject.getInt("atomic_number"),
            bondCount = atomJsonObject.getInt("bond_count")
        )
    }

    /**
     *  Creates the `atom` keywords of an atom class.
     *
     *  @param atomClass
     *      Atom class whose `atom` keywords are to be retrieved. If there is
     *      no such atom class, an exception is raised.
     *
     *  @return
     *      `atom` keywords of the atom class.
     */
    fun createKeywordsOfClass(atomClass: AtomClass): Set<AtomKeyword> =
        atomJsonObjectMap.mapNotNull {
            (atomTypeCode, atomJsonObject) ->

            if (atomJsonObject.getInt("atom_class") == atomClass.code) {
                createKeyword(atomTypeCode)
            } else {
                null
            }
        }.toSet()
}
