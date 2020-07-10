import sbt._
import sbt.Keys._

object UGenericSettings {

  val versionSetting = Seq(version := "0.0.1-SNAP2020071003", organization := "org.scalax")

  val currentScalaVersion = "2.13.3"
  val scala_212_Version   = "2.12.11"
  val scala_211_Version   = "2.11.12"

  val scalaVersionSetting    = scalaVersion := currentScalaVersion
  val scala213VersionSetting = scalaVersion := currentScalaVersion
  val scala212VersionSetting = scalaVersion := scala_212_Version
  val scala211VersionSetting = scalaVersion := scala_211_Version

  val scala_212_213_cross     = crossScalaVersions := Seq(scala_212_Version, currentScalaVersion)
  val scala_211_212_213_cross = crossScalaVersions := Seq(scala_211_Version, scala_212_Version, currentScalaVersion)
  val byNameImplicitSourceSetting = Compile / unmanagedSourceDirectories ++= {
    if ((scalaVersion.value startsWith "2.11.") || (scalaVersion.value startsWith "2.12.")) {
      List(sourceDirectory.value / "main" / "scala-2.11-2.12")
    } else if ((scalaVersion.value startsWith "2.13.")) {
      List(sourceDirectory.value / "main" / "scala-2.13")
    } else {
      List.empty
    }
  }

  val scalaSetting = List(
    transitiveClassifiers := Seq("sources"),
    org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile := true,
    publishArtifact in packageDoc := false,
    byNameImplicitSourceSetting
  )

}
