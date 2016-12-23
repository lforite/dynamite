package org.dynamite.http

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

/**
  * Trait to provide an Http Server for tests. Will be teared down at each run
  */
trait HttpServer {
  def withHttpServer[T](unitTest: WireMockServer => T): T = {
    val server = new WireMockServer(wireMockConfig().bindAddress("localhost").dynamicPort().dynamicHttpsPort())
    server.start()
    try {
      val result = unitTest(server)
      result
    } finally {
      server.shutdown()
    }
  }
}
