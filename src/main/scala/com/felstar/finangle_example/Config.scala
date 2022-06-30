package com.felstar.finangle_example

case class Config(db: DBConfig, http: HttpConfig)

case class DBConfig(
  host: String,
  dbname: String,
  driver:String,
  schema: String,
  user: String,
  password: String
){
  val url=s"jdbc:postgresql://$host/$dbname"
}

case class HttpConfig(
  port: Int
)
