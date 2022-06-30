
package com.felstar.finangle_example

import java.net.URL
import java.time.LocalDateTime

import cats.effect.IO
import com.twitter.finagle.http.Status
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray
import doobie._
import doobie.implicits._
import doobie.quill.DoobieContext
import io.circe.generic.auto._
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._

import io.getquill.{idiom => _, _}
import org.apache.commons.io.IOUtils
import shapeless.HNil

case class Video(id: Int, name: String, source: String, link: String, description: String,
                 eventId: Option[Int]=None, hidden: Boolean=false,
                 noOfViews: Int=0, noOfUpvotes: Int=0, noOfDownvotes:Int=0, updloadTimestamp: Option[LocalDateTime]=Option(LocalDateTime.now()),
                 bytes: Option[Array[Byte]]=None)

class Videos(xa: Transactor[IO]) extends DoobieSyntax{

  val videoadmin: Endpoint[IO, HNil] = apiadmin :: "video"

  val titles=new Markov("/film_titles_corpus.txt")

  val description=new Markov("/shakespeare_corpus.txt")

  val dc = new DoobieContext.Postgres(Literal) // Literal naming scheme

  import dc._

  implicit val videoInsertMeta = insertMeta[Video](_.id, _.updloadTimestamp)
  implicit val videotagInsertMeta = insertMeta[VideoTag](_.id)
  implicit val videocategoryInsertMeta = insertMeta[VideoCategory](_.id)

  implicit val videoUpdateMeta = updateMeta[Video](_.id, _.bytes, _.updloadTimestamp)

  // we don't want video to return bytes array, that is what image endpoints are for
  implicit val videoQueryMeta =
    queryMeta((q: Query[Video]) =>q) {
      case v => v.copy(bytes=None)
    }

  val dbcheck: IO[List[Video]] =run {
    query[Video]
  }.transact(xa)

  val videos: Endpoint[IO, List[Video]] = get(videoadmin) {
    val pp: doobie.ConnectionIO[List[Video]] = run {
      query[Video]
    }
      pp.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  private val videoPageAndSize=(page: Option[Int], pageSize: Option[Int])=>{
    val p=page.map(Math.max(_,1)).getOrElse(1)
    val size=pageSize.map(Math.max(_,1)).getOrElse(10)
    val offset=(p-1)*size
    quote {
      query[Video].drop(lift(offset)).take(lift(size))
    }
  }

  val pageAndSize=paramOption[Int]("page") :: paramOption[Int]("pagesize")

  val videosOrderByUpload: Endpoint[IO, List[Video]] = get(videoadmin:: "orderBy"::"upload" :: pageAndSize) {
    (page: Option[Int], pageSize: Option[Int])=>
    run {
      videoPageAndSize(page, pageSize).sortBy(_.updloadTimestamp)(Ord.desc)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val videosOrderByUpvotes: Endpoint[IO, List[Video]] = get(videoadmin:: "orderBy"::"upvotes" :: pageAndSize){
    (page: Option[Int], pageSize: Option[Int])=>
      run {
        videoPageAndSize(page, pageSize).sortBy(_.noOfUpvotes)(Ord.desc)
      }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val video: Endpoint[IO, Video] = get(videoadmin :: path[Int]) { id: Int =>
    run {
      query[Video].filter(_.id == lift(id)).take(1)
    }.unique.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val tags: Endpoint[IO, List[Tag]] = get(videoadmin :: "tags" :: path[Int]) { id: Int =>
    run {
      query[VideoTag].join(query[Tag]).on(_.tagId == _.id).filter(_._1.videoId == lift(id)).map(_._2)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val categories: Endpoint[IO, List[Category]] = get(videoadmin :: "categories" :: path[Int]) { id: Int =>
    run {
      query[VideoCategory].join(query[Category]).on(_.categoryId == _.id).filter(_._1.videoId == lift(id)).map(_._2)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)


  val image: Endpoint[IO, Buf] = get(videoadmin :: "image" ::path[Int]) { id: Int =>
    run {
      query[Video].filter(_.id == lift(id)).take(1).map(_.bytes)
    }.transact(xa).map{
      case Some(bytes):: _=> Ok(ByteArray(bytes: _*))
      case _=> Output.failure(new Exception("Image not found"), Status.NotFound)
    }
  }.handle(exceptionToBadRequest)

  val imagePut: Endpoint[IO, Buf] = put(videoadmin :: "image" :: path[Int] :: param("url")){  (id: Int, url: String) =>

    val ba=IOUtils.toByteArray(new URL(url))
    val bytea= Option(ba)

    run {
      quote {
        query[Video].filter(_.id == lift(id)).update(_.bytes->lift(bytea)).returning(_.bytes)
      }
    }.transact(xa).map{
      case Some(bytes)=> Ok(ByteArray(bytes: _*))
      case _=> Output.failure(new Exception("Image not found"), Status.NotFound)
    }
  }.handle(exceptionToBadRequest)

  def makeVideos(n:Int): IO[Output[List[Video]]] ={
    val videos =List.fill(n)(Video(0,titles.nextSentenceCapitalized,"imdb gen", "some link",description.nextSentenceCapitalized))
    run {
      liftQuery(videos).foreach { v =>
        query[Video].insert(v).onConflictIgnore.returning(r => r)
      }
    }.transact(xa).map(Ok)
  }

  val videoPopulateN: Endpoint[IO, List[Video]] = get(videoadmin :: "populate" :: path[Int] ) { n: Int=>
    makeVideos(n)
  }.handle(exceptionToBadRequest)

  val videoPopulate: Endpoint[IO, List[Video]] = get(videoadmin :: "populate") {
    makeVideos(100)
  }.handle(exceptionToBadRequest)

  val tagsPost: Endpoint[IO, List[Int]] = post(videoadmin :: "tags" :: path[Int] :: param("tags")) { (id: Int, tagsString: String)=>
    val tags=tagsString.split(",").map(Integer.parseInt).toList

    val videotags =tags.map(i=>VideoTag(0,id,i))

    val pp: doobie.ConnectionIO[Long] =run(query[VideoTag].filter(_.videoId == lift(id)).delete)

    val transaction= for {
      _ <- run(query[VideoTag].filter(_.videoId == lift(id)).delete)
      insert <- run(liftQuery(videotags).foreach { vt =>
        query[VideoTag].insert(vt).onConflictIgnore.returning(_.tagId)
      })
    } yield insert
    transaction.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val categoriesPost: Endpoint[IO, List[Int]] = post(videoadmin :: "categories" :: path[Int] :: param("categories")) { (id: Int, categoriesString: String)=>
    val categories=categoriesString.split(",").map(Integer.parseInt).toList

    val videocategories =categories.map(i=>VideoCategory(0,id,i))

    val transaction= for {
      _ <- run(query[VideoCategory].filter(_.videoId == lift(id)).delete)
      insert <- run(liftQuery(videocategories).foreach { vt =>
        query[VideoCategory].insert(vt).onConflictIgnore.returning(_.categoryId)
      })
    } yield insert
    transaction.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)


  val videoPost: Endpoint[IO, Video] = post(videoadmin :: jsonBody[Video]) { video: Video =>
    run {
      query[Video].insert(lift(video)).returning(r => r)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val videoPut: Endpoint[IO, Video] = put(videoadmin :: jsonBody[Video]) { video: Video =>
    run {
      query[Video].filter(_.id == lift(video.id)).update(lift(video)).returning(r => r)
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val videoDelete: Endpoint[IO, Long] = delete(videoadmin :: path[Int]) { id: Int =>
    run {
      query[Video].filter(_.id == lift(id)).delete
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)

  val videoDeleteMulti: Endpoint[IO, Long] = delete(apiadmin :: "videos" :: param("ids")) { (idsString: String)=>
    val idsToDelete = idsString.split(",").map(Integer.parseInt).toList

    run {
      query[Video].filter(v => liftQuery(idsToDelete).contains(v.id)).delete
    }.transact(xa).map(Ok)
  }.handle(exceptionToBadRequest)


  val endpoints = videos :+: video :+: videoPost :+: videoDelete :+:
    videoDeleteMulti :+: videoPut :+: videoPopulateN :+: videoPopulate :+:
    tags :+: tagsPost :+: categoriesPost :+: categories :+:
    videosOrderByUpload :+: videosOrderByUpvotes
}
