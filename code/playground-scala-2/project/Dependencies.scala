import sbt.*

object Dependencies {
  object Versions {
    val kafka                   = "3.8.0"
    val circe                   = "0.14.10"
  }

  val kafka = Seq(
    "org.apache.kafka" % "kafka-clients" % Versions.kafka,
    "org.apache.kafka" % "kafka-streams" % Versions.kafka,
    "org.apache.kafka" %% "kafka-streams-scala" % Versions.kafka
  )

  val circe = Seq(
    "io.circe" %% "circe-core" % Versions.circe,
    "io.circe" %% "circe-generic" % Versions.circe,
    "io.circe" %% "circe-parser" % Versions.circe
  )
}
