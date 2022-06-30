package com.felstar.finangle_example

import doobie._
import doobie.implicits._
import cats.data.NonEmptyList
import cats.implicits._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

sealed abstract class UnexpectedResultSetSize(msg: String) extends Exception(msg)

final case object UnexpectedOptionalContinuation
  extends UnexpectedResultSetSize(s"Expected ResultSet with zero or one row, but more rows were available.")

final case object UnexpectedUniqueContinuation
  extends UnexpectedResultSetSize(s"Expected ResultSet with exactly one row, but more rows were available.")

final case object UnexpectedUniqueEnd
  extends UnexpectedResultSetSize(s"Expected ResultSet with exactly one row, but was empty.")

final case object UnexpectedNonEmptyResults
  extends UnexpectedResultSetSize(s"Expected ResultSet have at least one row, but was empty.")

trait DoobieSyntax {

  implicit class ConnectionIOListOps[A](self: ConnectionIO[List[A]]) {

    /**
     * Program in `[[doobie.free.connection.ConnectionIO ConnectionIO]]` yielding a unique `A` and
     * raising an exception if the resultset does not have exactly one row. See also `option`.
     * @group Results
     */
    def unique: ConnectionIO[A] = self.flatMap {
      case head :: Nil => FC.pure(head)
      case Nil         => FC.raiseError(UnexpectedUniqueEnd)
      case _           => FC.raiseError(UnexpectedUniqueContinuation)
    }

    def unique(ifEmpty: => Throwable): ConnectionIO[A] = self.flatMap {
      case head :: Nil => FC.pure(head)
      case Nil         => FC.raiseError(ifEmpty)
      case _           => FC.raiseError(UnexpectedUniqueContinuation)
    }

    /**
     * Program in `[[doobie.free.connection.ConnectionIO ConnectionIO]]` yielding an optional `A`
     * and raising an exception if the resultset has more than one row. See also `unique`.
     * @group Results
     */
    def option: ConnectionIO[Option[A]] = self.flatMap {
      case head :: Nil => FC.pure(Some(head))
      case Nil         => FC.pure(None)
      case _           => FC.raiseError(UnexpectedOptionalContinuation)
    }

    /**
     * Program in `[[doobie.free.connection.ConnectionIO ConnectionIO]]` yielding a `NonEmptyList[A]`
     * and raising an exception if the resultset does not have at least one row. See also `unique`.
     * @group Results
     */
    def nel: ConnectionIO[NonEmptyList[A]] = self.map(_.toNel).flatMap {
      case Some(a) => FC.pure(a)
      case None    => FC.raiseError(UnexpectedNonEmptyResults)
    }

    /**
     * Program in `[[doobie.free.connection.ConnectionIO ConnectionIO]]` yielding an `F[A]`
     * accumulated via the provided `CanBuildFrom`. This is the fastest way to accumulate a
     * collection.
     * @group Results
     */
    def to[F[_]](implicit cbf: CanBuildFrom[Nothing, A, F[A]]): ConnectionIO[F[A]] = self.map(_.to[F])
  }
}