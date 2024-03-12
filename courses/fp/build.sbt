import Dependencies.*

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "fp",
      libraryDependencies ++= cats
  )

addCommandAlias("fmt", "; scalafmtAll ; scalafmtSbt; scalafixAll")
addCommandAlias("compileAll", "; compile ; Test / compile")
