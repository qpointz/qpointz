/*
 * Copyright 2020 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.qpointz.flow

import MetadataMethods._
import io.qpointz.flow.ql.MetadataEntry
import io.qpointz.flow.serialization.JsonProtocol
import org.json4s.{CustomSerializer, Extraction, JArray, JObject}

import scala.math

case class Record(attributes: Attributes, meta: Metadata) {

  private def applyMeta(m: Metadata): Metadata = meta ++ m

  def keySet: Set[AttributeKey] = attributes.keySet

  def keys: Iterable[AttributeKey] = attributes.keys

  def items:Iterator[(AttributeKey, AttributeValue)] = attributes.iterator

  def contains(key: AttributeKey): Boolean = attributes.contains(key)

  def getOp(key: AttributeKey): Option[AttributeValue] = attributes.get(key)

  def get(key: AttributeKey): AttributeValue = attributes(key)

  def apply(key: AttributeKey): AttributeValue = get(key)

  def getOrElse(key: AttributeKey, default: => AttributeValue): AttributeValue = attributes.getOrElse(key, default)

  def put(attrs: Attributes, mt: Metadata): Record = {
    Record(attributes ++ attrs, applyMeta(mt))
  }

  private def exactlyOn(keys:Iterable[AttributeKey])(on:()=>Record):Record = {
    val rkeys = keys.toSet
    val missingKeys = rkeys.diff(attributes.keySet.intersect(rkeys))
    if (missingKeys.isEmpty) {
      on()
    } else {
      throw new NoSuchElementException(s"""Key(s) not found: ${missingKeys.mkString(",")}""")
    }
  }

  def set(attrs: Attributes, mt: Metadata): Record = exactlyOn(attrs.keys){()=>
    put(attrs,mt)
  }

  def append(attrs: Attributes, mt: Metadata): Record = {
    Record(
      attributes ++ (attrs -- attrs.keySet.intersect(attributes.keySet)),
      applyMeta(mt))
  }

  def drop(keys: Iterable[AttributeKey], mt:Metadata):Record = {
    Record(attributes -- keys, applyMeta(mt))
  }

  def remove(keys:Iterable[AttributeKey], mt:Metadata):Record = exactlyOn(keys){()=>
    drop(keys, mt)
  }

}

import org.json4s.JsonDSL._

object RecordSerializer extends CustomSerializer[Record] (implicit format=> (
  {case jo:JObject =>
    val values:Map[AttributeKey, AttributeValue] = (jo \ "v") match {
      case ja:JObject => ja.extract[Map[AttributeKey, AttributeValue]]
      case _ => Map()
    }
    val m : Metadata = (jo \ "m" match {
      case ja:JArray => ja.extract[Seq[MetaEntry[_]]]
      case _ => Seq()
    })
    Record(values, m)
  },
  {
    case r:Record =>
      val jv = ("v" -> Extraction.decompose(r.items.toMap))
      if (r.meta.length==0) {
        jv
      } else {
        jv ~ ("m" -> Extraction.decompose(r.meta))
      }
  }
)) {
  val jsonProtocol = JsonProtocol(RecordSerializer)
}

object Record {

  def apply(kv: (AttributeKey, AttributeValue)*): Record = Record(kv.toMap, empty)

  def apply(m:Map[AttributeKey, AttributeValue]): Record = Record(m, empty)

  def apply[T<:AttributeValue](keys: Seq[AttributeKey], values:Seq[T], meta:Metadata): Record = {

    val map = if (keys.length==values.length) {
      keys.zipWithIndex.map(z=> (z._1 -> values(z._2)))
    } else {
      (0 to math.max(keys.length, values.length))
        .map(k=>(k, keys.lift(k), values.lift(k)) match {
            case (_ , Some(key), Some(value)) => Some(key -> value)
            case (k, Some(key), None) => Some(key -> AttributeValue.Missing)
            case (k, None , Some(value)) => Some(s"Attriibute_${k}" -> value)
            case (k, None, None) => None //k -> AttributeValue.Missing //throw new RuntimeException(s"Non matching ${k}")
          }
        )
        .filter(_.isDefined)
        .map(_.get)
    }

    new Record(map.toMap, meta)
  }

  implicit class RecordOp(r: Record) {

  }

}
