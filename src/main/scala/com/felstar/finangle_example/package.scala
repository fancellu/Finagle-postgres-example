package com.felstar

import java.sql.SQLException

import cats.effect.IO
import doobie.quill.DoobieContext
import io.finch.{BadRequest, Endpoint, InternalServerError, Output}
import shapeless.HNil
import io.finch.catsEffect._
import io.getquill.{Embedded, Literal}


package object finangle_example {

  val apiadmin: Endpoint[IO, HNil] = "api" :: "admin"

  def exceptionToBadRequest[T]: PartialFunction[Throwable, Output[T]]={
    case sqle: SQLException => println(s"DB $sqle");InternalServerError(sqle)
    case e: Exception => println(e);BadRequest(e)
  }

  case class VideoTag(id: Int, videoId: Int, tagId: Int)
  case class VideoCategory(id: Int, videoId: Int, categoryId: Int)

  val dc = new DoobieContext.Postgres(Literal)
  import dc._

  implicit val bytesDecoder: Decoder[Array[Byte]] =
    decoder((index, row) => row.getBytes(index))

  implicit val bytesEncoder: Encoder[Array[Byte]] =
    encoder(java.sql.Types.OTHER, (index, bytes, row) =>
      row.setBytes(index, bytes))
}
