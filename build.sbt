name := "CassandraTest"

version := "1.0"

//scalaVersion := "2.12.1"
scalaVersion in ThisBuild := "2.12.1"


resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases",
                  "Bintray sbt plugin releases" at "http://dl.bintray.com/sbt/sbt-plugin-releases")

libraryDependencies ++= Seq(

  "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.2",

  "com.datastax.cassandra" % "cassandra-driver-mapping" % "3.1.2",

  "com.datastax.cassandra" % "cassandra-driver-extras" % "3.1.2",

  "com.typesafe" % "config" % "1.3.1"


)

/*assemblyMergeStrategy in assembly := {
  case PathList("META-iNF/MANIFEST.MF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
  }*/

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) => MergeStrategy.discard
      case _ => MergeStrategy.discard
    }
  case _ => MergeStrategy.first
}






