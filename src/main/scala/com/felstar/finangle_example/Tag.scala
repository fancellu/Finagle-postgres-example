package com.felstar.finangle_example

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.quill.DoobieContext
import io.circe.generic.auto._
import io.finch._
import io.finch.catsEffect._
import io.finch.circe.dropNullValues._
import io.getquill.{idiom => _, _}
import shapeless.HNil

import scala.util.Random


case class Tag(id: Int, name: String)

class Tags(xa: Transactor[IO]) {

  val tagadmin: Endpoint[IO, HNil] = apiadmin :: "tag"

  val dc = new DoobieContext.Postgres(Literal) // Literal naming scheme

  import dc._

  implicit val tagInsertMeta = insertMeta[Tag](_.id)

  val tags: Endpoint[IO, List[Tag]] = get(tagadmin) {
    run {
      query[Tag]
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val tag: Endpoint[IO, List[Tag]] = get(tagadmin :: path[Int]) { id: Int =>
    run {
      query[Tag].filter(_.id == lift(id)).take(1)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val tagPost: Endpoint[IO, Tag] = post(tagadmin :: jsonBody[Tag]) { tag: Tag =>
    run {
      query[Tag].insert(lift(tag)).returning(r => r)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val tagPut: Endpoint[IO, Tag] = put(tagadmin :: jsonBody[Tag]) { tag: Tag =>
    run {
      query[Tag].filter(_.id == lift(tag.id)).update(lift(tag)).returning(r => r)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val tagDelete: Endpoint[IO, Long] = delete(tagadmin :: path[Int]) { id: Int =>
    run {
      query[Tag].filter(_.id == lift(id)).delete
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val tagPopulate: Endpoint[IO, List[Tag]] = get(tagadmin :: path("populate")) {

    val tags =List.fill(100)(Tag(0, Util.randomWord))

    run {
      liftQuery(tags).foreach { v =>
        query[Tag].insert(v).onConflictIgnore.returning(r => r)
      }
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val videos: Endpoint[IO, List[Video]] = get(tagadmin :: "videos" :: path[Int]) { id: Int =>
    run {
      query[VideoTag].join(query[Video]).on(_.videoId == _.id).filter(_._1.tagId == lift(id)).map(_._2)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val endpoints =
    tags :+: tag :+: tagPost :+: tagDelete :+: tagPut :+: tagPopulate :+: videos
}
