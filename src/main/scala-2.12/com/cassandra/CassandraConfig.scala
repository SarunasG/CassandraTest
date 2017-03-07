package com.cassandra

import com.typesafe.config.ConfigFactory

/**
  * Created by BC0414 on 06/03/17.
  */
final class CassandraConfig {


  protected val config = ConfigFactory.load.getConfig("cassandra-config")

  val sslStatus = config.getString("ssl.status").toBoolean
  val sslOneWay = config.getString("ssl.oneway").toBoolean
  val trustStore = config.getString("ssl.truststore")
  val trustStorePassword = config.getString("ssl.trustStorePassword")
  val keystore = config.getString("ssl.keystore")
  val keystorePassword = config.getString("ssl.keystorePassword")
  val contactPoints = config.getString("contactPoints")
  val userName = config.getString("cassandra.username")
  val userPassword = config.getString("cassandra.password")
  val query = config.getString("cassandra.query")


}
