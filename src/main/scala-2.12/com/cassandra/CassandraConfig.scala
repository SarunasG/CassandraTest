package com.cassandra

import com.typesafe.config.ConfigFactory
import java.io.File

/**
  * Created by BC0414 on 06/03/17.
  */
final class CassandraConfig(configPath : String) {

  val myConfigFile = new File(configPath)
  val fileConfig = ConfigFactory.parseFile(myConfigFile).getConfig("cassandra-config")


  protected val config = ConfigFactory.load(fileConfig)

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
