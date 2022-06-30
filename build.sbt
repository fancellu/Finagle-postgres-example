name := "finagle-postgres-example"

version := "0.1"

scalaVersion := "2.12.16"

scalacOptions += "-Ypartial-unification" // 2.11.9+

val finatraVersion= "20.3.0"
val circeVersion = "0.11.0"
val finchVersion = "0.31.0"
val doobieVersion= "0.8.8"

libraryDependencies += "com.twitter" %% "finatra-http" % finatraVersion

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % finatraVersion % "test" classifier "tests",
  "com.twitter" %% "inject-core" % finatraVersion % "test" classifier "tests",
  "com.twitter" %% "inject-app" % finatraVersion % "test" classifier "tests",
  "com.twitter" %% "inject-server" % finatraVersion % "test" classifier "tests",
  "com.twitter" %% "inject-modules" % finatraVersion % "test" classifier "tests",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finchx-core" % finchVersion,
  "com.github.finagle" %% "finchx-generic" % finchVersion,
  "com.github.finagle" %% "finchx-circe" % finchVersion,

  "io.circe" %% "circe-generic" % circeVersion,
  "io.getquill" %% "quill-jdbc" % "3.5.1",

  "org.tpolecat" %% "doobie-core"  % doobieVersion,

  "org.tpolecat" %% "doobie-quill" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,          // Postgres driver 42.2.9 + type mappings.
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test",  // ScalaTest support for typechecking statements.
   "org.flywaydb" % "flyway-core" % "6.3.2",
   "com.github.pureconfig" %% "pureconfig" % "0.12.3",

  "com.opentable.components" % "otj-pg-embedded" % "0.13.3"
)


libraryDependencies += "commons-io" % "commons-io" % "2.6"