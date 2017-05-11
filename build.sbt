name := "AkkaMemcached"

version := "0.0.1"

scalaVersion := "2.11.8"

resolvers += "akka" at "http://repo.akka.io/snapshots"
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
	"org.scalatest"       %% "scalatest"       % "2.2.4",
	"com.typesafe.akka"   %% "akka-actor"      % "2.5.1",
	"com.typesafe.akka"   %% "akka-slf4j"      % "2.5.1",
  "ch.qos.logback"      % "logback-classic"  % "1.1.6",
	"org.specs2"          %% "specs2-core"     % "3.4"     % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")

