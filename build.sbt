version := "0.1.0-SNAPSHOT"

scalaVersion := "3.3.0"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.5.0-M4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.0-M4",
  "mysql" % "mysql-connector-java" % "8.0.33",
  "com.rabbitmq" % "amqp-client" % "5.16.0"
)

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.0"

lazy val root = (project in file("."))
  .settings(
    name := "petition_api"
  )


ThisBuild / assemblyMergeStrategy in assembly := {
  case PathList("google", "protobuf", "any.proto") => MergeStrategy.first
  case PathList("google", "protobuf", "descriptor.proto") => MergeStrategy.first
  case PathList("google", "protobuf", "empty.proto") => MergeStrategy.first
  case PathList("google", "protobuf", "struct.proto") => MergeStrategy.first
  case PathList("module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

