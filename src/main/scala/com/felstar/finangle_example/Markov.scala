package com.felstar.finangle_example

import scala.io.{Codec, Source}
import scala.util.Random


class Markov(corpusname: String) {

  import scala.collection.mutable

  val MARKOV_MAP: mutable.Map[Seq[String], mutable.Map[String, Int]] = new mutable.HashMap()
  val CHAIN_SIZE = 2

  def adjustProbabilities(sentence: String): Unit = {
    val segments = sentence.split(" ").+:("").:+("").sliding(CHAIN_SIZE + 1).toList
    for (segment <- segments) {
      val key = segment.take(CHAIN_SIZE)
      val probs = MARKOV_MAP.getOrElse(key, scala.collection.mutable.Map())
      probs(segment.last) = probs.getOrElse(segment.last, 0) + 1
      MARKOV_MAP(key) = probs
    }
  }

  def normalize(line: String): String = line.stripLineEnd.toLowerCase.filterNot("\\.,\";:&" contains _)

  val source = Source.fromInputStream(getClass.getResourceAsStream(corpusname))(Codec.UTF8)

  source.getLines().map(normalize).map(_.trim).foreach(adjustProbabilities)

  source.close()

  val startWords: Seq[Seq[String]] = MARKOV_MAP.keys.filter(_.head == "").toList

  val r = new Random()

  def nextWord(seed: Seq[String]): String = {
    val possible = MARKOV_MAP.getOrElse(seed, List())
    r.shuffle(possible.flatMap(pair => List.fill(pair._2)(pair._1))).head
  }

  def nextSentence(): List[String] = {
    import scala.collection.mutable.ArrayBuffer
    val seed = startWords(r.nextInt(startWords.size))
    val sentence: ArrayBuffer[String] = ArrayBuffer()
    sentence.appendAll(seed)
    while (sentence.last != "") {
      sentence.append(nextWord(sentence.view(sentence.size - CHAIN_SIZE, sentence.size)))
    }
    sentence.view(1, sentence.size - 1).toList
  }

  def nextSentenceCapitalized: String =nextSentence().mkString(" ").capitalize
}
