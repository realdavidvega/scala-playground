import sbt.*

object Dependencies {
  object Versions {
    val cats                   = "2.10.0"
    val catsEffect             = "3.5.3"
    val catsEffectLaws         = "3.5.4"
    val catsRetry              = "3.1.3"
    val catsScalacheck         = "0.3.2"
    val log4cats               = "2.6.0"
  }

  val cats = Seq(
    "org.typelevel"    %% "cats-core"   % Versions.cats,
    "org.typelevel"    %% "cats-effect" % Versions.catsEffect,
    "org.typelevel" %% "cats-effect-laws"   % Versions.catsEffectLaws,
    "com.github.cb372" %% "cats-retry"  % Versions.catsRetry
  )
}
