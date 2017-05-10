name := "AkkaMemcached"

version := "0.0.1"

scalaVersion := "2.11.8"

resolvers += "akka" at "http://repo.akka.io/snapshots"

libraryDependencies ++= Seq(
	"org.scalatest"       %% "scalatest"       % "2.2.4",
	"com.typesafe.akka"   %% "akka-actor"      % "2.5.1",
	"com.typesafe.akka"   %% "akka-slf4j"      % "2.5.1",
  "ch.qos.logback"      % "logback-classic"  % "1.1.6"
)

