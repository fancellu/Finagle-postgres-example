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


case class Category(id: Int, name: String)

class Categories(xa: Transactor[IO]) {

  val categoryadmin: Endpoint[IO, HNil] = apiadmin :: "category"

  val dc = new DoobieContext.Postgres(Literal) // Literal naming scheme

  import dc._

  implicit val categoryInsertMeta = insertMeta[Category](_.id)

  val categories: Endpoint[IO, List[Category]] = get(categoryadmin) {
    run {
      query[Category]
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val category: Endpoint[IO, List[Category]] = get(categoryadmin :: path[Int]) { id: Int =>
    run {
      query[Category].filter(_.id == lift(id)).take(1)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val categoryPost: Endpoint[IO, Category] = post(categoryadmin :: jsonBody[Category]) { category: Category =>
    run {
      query[Category].insert(lift(category)).returning(r => r)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val categoryPut: Endpoint[IO, Category] = put(categoryadmin :: jsonBody[Category]) { category: Category =>
    run {
      query[Category].filter(_.id == lift(category.id)).update(lift(category)).returning(r => r)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val categoryDelete: Endpoint[IO, Long] = delete(categoryadmin :: path[Int]) { id: Int =>
    run {
      query[Category].filter(_.id == lift(id)).delete
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val categoryPopulate: Endpoint[IO, List[Category]] = get(categoryadmin :: path("populate")) {

    val categories =List.fill(100)(Category(0, Util.randomWord))
    run {
      liftQuery(categories).foreach { v =>
        query[Category].insert(v).onConflictIgnore.returning(r => r)
      }
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val videos: Endpoint[IO, List[Video]] = get(categoryadmin :: "videos" :: path[Int] :: paramOption[Int]("page")) {
    (id: Int, page: Option[Int]) =>
    run {
      query[VideoCategory].join(query[Video]).on(_.videoId == _.id).filter(_._1.categoryId == lift(id)).map(_._2).
        drop(lift(page.getOrElse(0)*4)).take(4)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val endpoints =
    categories :+: category :+: categoryPost :+: categoryDelete :+: categoryPut :+: categoryPopulate :+:
    videos
}
