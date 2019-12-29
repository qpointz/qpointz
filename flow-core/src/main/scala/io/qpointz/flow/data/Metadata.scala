/*
 * Copyright 2019 qpointz.io
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

package io.qpointz.flow.data

import scala.reflect.ClassTag


object Metadata {
  val empty: Metadata = List()
}

trait MetadataTarget {
  val metadata : Metadata
}


class MetadataItemOps[T](val meta:Metadata, val group: MetadataGroupKey, val key: MetadataKey )(implicit val tt:ClassTag[T]) {

  def apply(v:T):Metadata = put(v)

  def apply():T = get()

  def getOp: Option[T] = meta.find(x => x._1 == group && x._2 == key) match {
    case Some((_,_,v:T)) => Some(v)
    case _ => None
  }

  def get(): T = getOp match {
    case Some(t) => t
    case _ => throw new NoSuchElementException(s"No such ${group}::${key} element exists")
  }

  def getOr(or: T): T = getOp match {
    case Some(x) => x
    case _ => or
  }

  def put(v:T):Metadata = meta :+ (group, key, v)
}

class MetadataOps(val groupKey:String) {

  def item[T](m:Metadata, key:MetadataKey)(implicit tt:ClassTag[T]):MetadataItemOps[T] = new MetadataItemOps[T](m, groupKey, key)

}
