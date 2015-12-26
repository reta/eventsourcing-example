name := "eventsourcing-example"

version := "0.0.1-SNAPSHOT"

organization := "com.example"

scalaVersion := "2.11.7"

parallelExecution in Test := false

libraryDependencies ++= {
  val logbackVersion = "1.1.3"
  val akkaStreamVersion = "1.0"
  val akkaVersion = "2.4.1"
  
  Seq(
    "org.slf4j" % "slf4j-api" % "1.7.7",
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion,
    "org.iq80.leveldb" % "leveldb" % "0.7",
    "com.typesafe.slick" %% "slick" % "3.1.1",
    "com.h2database" % "h2" % "1.4.190",
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "org.dmonix.akka" %% "akka-persistence-mock" % "1.1" % "test",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  )
}

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18)
