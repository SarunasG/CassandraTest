package com.cassandra


/**
  * Created by BC0414 on 02/03/17.
  */

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.io.{File, FileInputStream}
import java.security.KeyStore
import java.security.SecureRandom

import collection.JavaConversions._
import com.datastax.driver.core._


object TestSSLConn {

  val conf = new CassandraConfig
  var myResults: ResultSet = _
  var session = None: Option[Session]
  var sslOptions = None: Option[JdkSSLOptions]

  import conf._

  def main(args: Array[String]) {


    val cipherSuites: Array[String] = Array("TLS_RSA_WITH_AES_128_CBC_SHA",
      "TLS_RSA_WITH_AES_256_CBC_SHA")

    val sslContext = sslStatus match {

      case true => {

        sslOneWay match {

          case true =>

            getSSLContext(trustStore, trustStorePassword)

          case false =>

            getSSLContext(trustStore,
              trustStorePassword,
              keystore,
              keystorePassword)

        }

      }

      case _ => {

        null

      }

    }


    val cluster =  sslStatus match {

      case true => {

        sslOptions = Some(JdkSSLOptions
          .builder()
          .withSSLContext(sslContext)
          .withCipherSuites(cipherSuites)
          .build())

        Cluster.builder()
          .addContactPoints(contactPoints)
          .withSSL(sslOptions.get)
          .withCredentials(userName, userPassword)
          .build()

      }


      case false => {

        Cluster.builder()
          .addContactPoints(contactPoints)
          .withCredentials(userName, userPassword)
          .build()

      }


    }


    /*    val cluster = Cluster.builder()
          .addContactPoints(contactPoints)
          .withSSL(sslOptions.get)
          .withCredentials(userName, userPassword)
          .build()*/

    try {
      println("connecting to Cassandra DB...")

      try {

        session = Some(cluster.connect())

        println(session.getOrElse(throw new RuntimeException("Cassandra DB connection error. Session is null")).getState().toString())

        myResults = session.get.execute(query)

        for (myRow <- myResults) {
          println(myRow.toString)
        }

        if (!session.get.isClosed) {
          session.get.close()
          cluster.close()
        }

      } catch {
        case e: NullPointerException => println("Connection not opened due to Exception with Cassandra DB")
        case e: com.datastax.driver.core.exceptions.NoHostAvailableException =>
          println("Connection refused, please verify provided host is available")
      }


    }
    catch {
      case e: Exception => (e.printStackTrace())

    }

  }


  def getSSLContext(trustStorePath: String,
                    trustStorePassword: String,
                    keystorePath: String = null,
                    keystorePassword: String = null): SSLContext = {

    val ctx = SSLContext.getInstance("SSL")
    val ts = KeyStore.getInstance("JKS")
    val ks = KeyStore.getInstance("JKS")
    var tsf = None: Option[FileInputStream]
    var ksf = None: Option[FileInputStream]
    var kmf = None: Option[KeyManagerFactory]

    if (checkFileExists(trustStorePath)) {

      tsf = Some(new FileInputStream(trustStorePath))
      ksf = Some(new FileInputStream(keystorePath))

      ts.load(tsf.get, trustStorePassword.toCharArray)
      val tmf =
        TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm)
      tmf.init(ts)

      if (keystorePath != null) {

        ks.load(ksf.get, keystorePassword.toCharArray)

        kmf =
          Some(KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm))
        kmf.get.init(ks, keystorePassword.toCharArray)

      }


      ctx.init(kmf.get.getKeyManagers, tmf.getTrustManagers, new SecureRandom())

    }

    ctx
  }

  def checkFileExists(filename: String): Boolean = {
    new File(filename).exists()
  }
}