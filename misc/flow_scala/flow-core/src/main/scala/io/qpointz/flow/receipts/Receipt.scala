/*
 * Copyright 2022 qpointz.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.qpointz.flow.receipts

import io.qpointz.flow.receipts.impl.Convert
import org.json4s.jackson.Serialization._
import org.json4s.{AsJsonInput, Extraction}

import java.io.{BufferedReader, File, FileInputStream, InputStream, InputStreamReader}
import java.nio.file.Path

trait Receipt {
  def run():Unit
}

object Receipt {

  import io.qpointz.flow.serialization.Json._
  import org.json4s.jackson.JsonMethods._

  def fromPath(p:Path, props:Map[String, String]=Map()): Receipt = {
    val content = scala.io.Source.fromFile(p.toFile).mkString
    fromString(content, props)
  }

  def fromInputStream(ins:InputStream, props:Map[String, String]=Map()):Receipt = {
    val content = scala.io.Source.fromInputStream(ins).mkString
    fromString(content, props)
  }

  def fromReader(isr:InputStreamReader, props:Map[String, String]=Map()):Receipt = {
    val br = new BufferedReader(isr)
    val content = Iterator.continually(br.readLine()).takeWhile(_!=null).mkString
    fromString(content, props)
  }

  def fromString(cnt:String, props:Map[String, String]=Map()):Receipt = {
    implicit val fmts = formats

    def loop(cnt:String, key:String, value:String):String = {
      val r = ("""(?s)\$\{""" + key + """+\}""").r
      r.replaceAllIn(cnt, value)
    }
    val nc = props.foldLeft(cnt)((x,k)=>loop(x, k._1, k._2))
    read[Receipt](nc)
  }

  def fromFile(file:File, props:Map[String, String]=Map()):Receipt = {
    fromInputStream(new FileInputStream(file), props)
  }
}