import Dependencies.*

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "playground-scala-3",
      libraryDependencies ++= cats
  )

addCommandAlias("fmt", "; scalafmtAll ; scalafmtSbt; scalafixAll")
addCommandAlias("compileAll", "; compile ; Test / compile")
