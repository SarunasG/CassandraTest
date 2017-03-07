name := "CassandraTest"

version := "1.0"

scalaVersion := "2.12.1"


resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(

  "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.2",

  "com.datastax.cassandra" % "cassandra-driver-mapping" % "3.1.2",

  "com.datastax.cassandra" % "cassandra-driver-extras" % "3.1.2",

  "com.typesafe" % "config" % "1.3.1"


)




