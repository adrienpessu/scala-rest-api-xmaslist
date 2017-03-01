name := """play-reactive-mongo-db"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

//routesGenerator := InjectedRoutesGenerator //I bet it's useless since play 2.5

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.12"
)

libraryDependencies ++= Seq(
  "org.bitbucket.b_c" % "jose4j" % "0.5.4"
  // other dependencies separated by commas
)

libraryDependencies += filters

fork in run := false