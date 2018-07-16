name := "Dragon Creator"

version := "0.1"

scalaVersion := "2.12.6"

// https://mvnrepository.com/artifact/org.scalafx/scalafx
libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12"

// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.6"

// https://mvnrepository.com/artifact/org.controlsfx/controlsfx
libraryDependencies += "org.controlsfx" % "controlsfx" % "8.40.14"

libraryDependencies += "org.fxmisc.easybind" % "easybind" % "1.0.3"

fork in run := true