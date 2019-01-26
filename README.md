#   Chemistry Research Utility Library

##  Introduction

Chemistry Research Utility Library (CRUL) is a utility library containing
classes and functions that facilitate day-to-day research tasks in
computational chemistry, such as converting units of measure, representing a
chemical species, rotating coordinates, rudimentary fuzzy numbers, and others.
CRUL is developed in Kotlin and uses Apache Ant for building.


##  Building

### Dependencies

* [Apache Ant](http://ant.apache.org/)
* [Kotlin compiler](http://kotlinlang.org/docs/tutorials/command-line.html)
* [Dokka](https://github.com/Kotlin/dokka) (for generating the documentation)

Apache Ivy is used to retrieve the remaining dependencies. It is not necessary
to install Ivy: Ant will automatically download Ivy if needed.


### From the Command Line with Ant

CRUL uses Apache Ant for building. Available targets can be listed with `ant
-p`. The default target, `build`, builds the library as a JAR file.

There are several build options that can be configured. `ant
list.config.options` lists the configurable options along with their defaults.
The options are

* `config.kotlin.home`: Root directory of Kotlin's compiler distribution. By
  default, it uses the environment variable, `KOTLIN_HOME`.
* `config.target.path`: Directory where the target files (such as the packaged
  JAR file and the documentation) will be stored.
* `config.lib.path`: Directory of the JAR dependencies managed by Ivy. The JAR
  dependencies are used in both building and using the library.
* `config.dokka.fatjar`: Path to Dokka's fat JAR.
* `config.dokka.output.format`: Output format of the generated documentation
  according to Dokka.

For building, `config.kotlin.lib` is required. For documentation generation,
`config.dokka.fatjar` is required.

## License

CRUL is under the Apache License, Version 2.0.
