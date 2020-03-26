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
 */

package io.qpointz.flow

case class Record(attributes: Attributes, meta: Metadata) {

  private def applyMeta(m: Metadata): Metadata = m match {
    case Metadata.empty => meta
    case _ => meta ++ m
  }

  def keySet: Set[AttributeKey] = attributes.keySet

  def keys: Iterable[AttributeKey] = attributes.keys

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
      throw new NoSuchElementException(s"""Key(s) not found: ${missingKeys.concat(",")}""")
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

object Record {

  def apply(kv: (AttributeKey, AttributeValue)*): Record = Record(kv.toMap, Metadata.empty)

  implicit class RecordOp(r: Record) {

  }

}