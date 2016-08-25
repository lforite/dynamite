package org.dynamite.http

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.specs2.matcher.MatchResult

/**
  * Trait to provide an Http Server for tests. Will be teared down at each run
  */
trait HttpServer {
  def withHttpServer(unitTest: WireMockServer => MatchResult[Any]): MatchResult[Any] = {
    val server = new WireMockServer(wireMockConfig().bindAddress("localhost").dynamicPort().dynamicHttpsPort())
    server.start()
    val result = unitTest(server)
    server.shutdownServer()
    result
  }
}
