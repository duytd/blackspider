name := "blackspider"

organization := "org.qmlstudio"

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies += "org.mongodb" %% "casbah" % "2.8.0"
libraryDependencies += "com.novus" %% "salat" % "1.9.9"
libraryDependencies += "org.jsoup" % "jsoup" % "1.8.1"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "1.8.0"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"