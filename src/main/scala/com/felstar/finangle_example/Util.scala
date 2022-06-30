package com.felstar.finangle_example

import scala.io.{Codec, Source}
import scala.util.Random

object Util{

  val words= Source.fromInputStream(getClass.getResourceAsStream("/The_Oxford_3000.txt"))(Codec.UTF8).getLines().toList

  def randomWord=words(Random.nextInt(words.size))
}
