UGenericSettings.scalaSetting
UGenericSettings.scala_212_213_cross

Dependencies.fsg
libraryDependencies += Dependencies.shapeless

libraryDependencies ++= Dependencies.circe(scalaVersion.value)

libraryDependencies ++= Dependencies.scalaTest.map(_ % Test)

resolvers += Resolver.bintrayRepo("djx314", "maven")
libraryDependencies += "net.scalax" %% "poi-collection" % "0.4.0-M8"
