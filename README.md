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

* `config.build.dir`: Directory where the building takes place. This directory
  can be safely removed after the library is built.
* `config.doc.dir`: Directory where the generated documentation resides.
* `config.dokka.fatjar`: Path to Dokka's fat JAR.
* `config.dokka.output.format`: Output format of the generated documentation
  according to Dokka.
* `config.kotlin.lib`: Kotlin's `lib` directory that is distributed with the
  compiler.
* `config.lib.dir`: Directory where dependencies are to be stored. The
  dependencies are used in building the library and for using the packaged
  library afterward.
* `config.proj.jar.dir`: Directory where the packaged library will be stored.

For building, `config.kotlin.lib` is required. For documentation generation,
`config.dokka.fatjar` is required.

## License

CRUL is under the Apache License, Version 2.0.
