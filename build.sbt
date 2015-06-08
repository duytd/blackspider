name := "blackspider"

organization := "com.portia"

version := "1.0"
assemblyJarName in assembly := "portia.jar"

scalaVersion := "2.11.6"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
resolvers += "SBT Repository" at "http://repo.scala-sbt.org/scalasbt/repo/"
resolvers += "MongoDB Cashbah" at "https://oss.sonatype.org/content/repositories/releases/"

libraryDependencies += "org.mongodb" %% "casbah" % "2.8.0"
libraryDependencies += "com.novus" %% "salat" % "1.9.9"
libraryDependencies += "org.jsoup" % "jsoup" % "1.8.1"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"
libraryDependencies += "org.apache.lucene" % "lucene-analyzers" % "3.4.0"
libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.0.0"
