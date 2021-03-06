package com.cassandra


/**
  * Created by Sarunas G on 02/03/17.
  */

import java.net.InetAddress
import javax.net.ssl.{KeyManager, KeyManagerFactory, SSLContext, TrustManagerFactory}
import java.io.{File, FileInputStream}
import java.security.KeyStore
import java.security.SecureRandom

import collection.JavaConversions._
import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.AuthenticationException
import com.datastax.driver.core.policies.{DCAwareRoundRobinPolicy, DowngradingConsistencyRetryPolicy, RoundRobinPolicy, TokenAwarePolicy}

import scala.util.Try


object TestSSLConn {

  var myResults: ResultSet = _
  var session = None: Option[Session]

  def main(args: Array[String]) {

    val configPath = Try(args(0))
    val conf = new CassandraConfig(configPath.getOrElse("./src/main/resources/application.conf"))
    import conf._


    val cipherSuites: Array[String] = Array("TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA")

    val sslContext = if (sslStatus) {
      if (sslOneWay) {
        getSSLContext(trustStore, trustStorePassword)
      } else {
        getSSLContext(trustStore, trustStorePassword, keystore, keystorePassword)
      }
    } else {
      null
    }

    val buildSSLOptions = () => {

      Some(JdkSSLOptions
        .builder()
        .withSSLContext(sslContext)
        .withCipherSuites(cipherSuites)
        .build())
    }

    val contactPointsInDC = scala.collection.mutable.Set[InetAddress]()
    contactPoints.split(",").foreach(ip => contactPointsInDC.add(InetAddress.getByName(ip)))

    val consistencyLevelDC = ConsistencyLevel.valueOf(consistencyLevel)
    val loadBalancingPolicy = consistencyLevel match {

      case "QUORUM" => new RoundRobinPolicy()
      case "LOCAL_QUORUM" => DCAwareRoundRobinPolicy.builder()
        .withLocalDc(datacenterName)
        .withUsedHostsPerRemoteDc(hostsPerRemote)
        .build()
    }


    val buildCluster = (sslStatus: Boolean) => {

      val cluster = Cluster.builder()
        .addContactPoints(contactPointsInDC)
        .withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
        .withLoadBalancingPolicy(new TokenAwarePolicy(loadBalancingPolicy))
        .withQueryOptions(new QueryOptions().setConsistencyLevel(consistencyLevelDC))
        .withCredentials(userName, userPassword)

      if (sslStatus) {
        cluster.withSSL(buildSSLOptions().get).build()
      } else {
        cluster.build()
      }
    }

    val cluster = buildCluster(sslStatus)

    try {
      println("connecting to Cassandra DB...")

      try {

        session = Some(cluster.connect())
        println(session.getOrElse(throw new RuntimeException("Cassandra DB connection error. Session is null")).getState.toString())
        println(session.get.getCluster.getClusterName)
        println(session.get.getCluster.getConfiguration.getQueryOptions)

        myResults = session.get.execute(query)

        for (myRow <- myResults) {
          println(myRow.toString)
        }

      } catch {
        case e: AuthenticationException => println("Cassandra Authentication issue - check user name or password !")
        case e: NullPointerException => println("Connection not opened due to Exception with Cassandra DB")
        case e: com.datastax.driver.core.exceptions.NoHostAvailableException =>
          println("Connection refused, please verify provided host is available")
      }
    }
    catch {
      case e: Exception => e.printStackTrace()
    }

    session match {

      case Some(_) =>
        if (!session.get.isClosed) {
          session.get.close()
          cluster.close()
        }
      case None => cluster.close()

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
            ts.load(tsf.get, trustStorePassword.toCharArray)
      val tmf =
        TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm)
      tmf.init(ts)

      if (keystorePath != null) {

        ksf = Some(new FileInputStream(keystorePath))
        ks.load(ksf.get, keystorePassword.toCharArray)

        kmf =
          Some(KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm))
        kmf.get.init(ks, keystorePassword.toCharArray)

      }

      kmf match {
        case Some(_) => ctx.init(kmf.get.getKeyManagers, tmf.getTrustManagers, new SecureRandom())
        case None => ctx.init(Array(KeyManager), tmf.getTrustManagers, new SecureRandom())
      }
    }

    ctx
  }

  def checkFileExists(filename: String): Boolean = {
    new File(filename).exists()
  }
}