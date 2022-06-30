package com.felstar.finangle_example

import cats.effect.IO
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.param.Stats
import com.twitter.finagle.{Http, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import doobie._
import io.circe.{Encoder, Json}

import io.finch.catsEffect._

import io.finch._
import io.finch.circe.dropNullValues._
import io.circe.generic.auto._

import io.getquill.Literal
import org.flywaydb.core.Flyway
import pureconfig.ConfigSource

import scala.concurrent.ExecutionContext
import pureconfig._
import pureconfig.generic.auto._

object MainServer extends TwitterServer {

  val Config: Config =ConfigSource.default.loadOrThrow[Config]

  println(Config)

  val (dbConfig, httpconfig)=(Config.db,Config.http)

  implicit val encodeException: Encoder[Exception] = Encoder.instance(e =>
    Json.obj("exception" -> Json.fromString(e.getMessage)))

  private implicit val cs = IO.contextShift(ExecutionContext.global)

   val flyway = Flyway.configure.dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
     .schemas(dbConfig.schema).load

   println(flyway.migrate())

  val xa = Transactor.fromDriverManager[IO](
    dbConfig.driver, s"${dbConfig.url}?currentSchema=${dbConfig.schema}", dbConfig.user, dbConfig.password
  )

  val healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("OK")
  }

  val videos=new Videos(xa)

  val videoList=videos.dbcheck.unsafeRunSync()
  println("DB check video count="+videoList.size)

  val tags=new Tags(xa)
  val categories=new Categories(xa)

  val api: Service[Request, Response] = Bootstrap
    .serve[Text.Plain](healthcheck)
    .serve[Application.Json](
      videos.endpoints :+: tags.endpoints :+: categories.endpoints
    )
    .serve[io.finch.Image.Png](videos.image :+: videos.imagePut)
    .toService

  def main(): Unit = {
    val server = Http.server
      .configured(Stats(statsReceiver))
      .serve(s":${httpconfig.port}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }

}
