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

import scala.reflect.ClassTag

trait MetadataProvider {
  val metadata : Metadata
}

trait MetadataGroupOwner {
  val metadataGroupKey : String
}

case class MetaKey(group:String, key:String) {}

case class MetaEntry[T<:Any](key:MetaKey, value:T) {}

class EntryDefinition[T](val key:MetaKey) {
  def apply(value:T):MetaEntry[T] = MetaEntry[T](key, value)
}

object EntryDefinition {
  def apply[T](group:String, key:String ):EntryDefinition[T] = new EntryDefinition[T](MetaKey(group, key))
}

abstract class MetadataGroup(val groupKey:String) {
  def key(key:String): MetaKey = MetaKey(groupKey, key)
  def entry[T](k:String):EntryDefinition[T] = new EntryDefinition[T](key(k))
}

object MetadataMethods {

  val empty:Metadata = Seq()

  implicit def entry2Meta(e:MetaEntry[_]):Metadata = Seq(e)

  implicit def seq2Meta(itms:Seq[(String, String, _)]):Metadata = itms.map(k=>t32Entry(k._1, k._2, k._3))

  implicit def t32Entry[T](t:(String, String, T)):MetaEntry[T] = MetaEntry(MetaKey(t._1, t._2), t._3)

  implicit def tdf2Entry[T](kv:(EntryDefinition[T], T)):MetaEntry[T] = MetaEntry(kv._1.key, kv._2)

  implicit class MetadataObjectMethods(val m:Metadata) {

    def getAll[T](df:EntryDefinition[T])(implicit tag:ClassTag[T]):Seq[MetaEntry[_]] = {
      m.filter(_.key==df.key)
    }

    def getOp[T](df:EntryDefinition[T])(implicit tag:ClassTag[T]) :Option[T] = getAll(df) match {
      case Seq(MetaEntry(df.key, v:T), _ @ _*) => Some(v)
      case _ => None
    }

    def get[T](df:EntryDefinition[T])(implicit tag:ClassTag[T]):T = getOp(df) match {
      case Some(v:T) => v
      case None => throw new NoSuchElementException(s"Key ${df.key} not found")
    }

    def getOr[T](df:EntryDefinition[T], default:T)(implicit tag:ClassTag[T]):T = getOp(df).getOrElse(default)

    def put[T](df:EntryDefinition[T], value:T)(implicit tag:ClassTag[T]):Metadata = m :+ MetaEntry(df.key, value)

    //scalastyle:off method.name
    def >+[T](e:MetaEntry[T])(implicit tag:ClassTag[T]):Metadata = m :+ e
    //scalastyle:on method.name

    def apply[T](df:EntryDefinition[T])(implicit tag:ClassTag[T]):T = get(df)

  }

}

object Metadata {
  import MetadataMethods._

  def apply[T](sq:Seq[(EntryDefinition[T],T)])(implicit tag:ClassTag[T]):Metadata = sq.map(tdf2Entry)

  def apply(sq:Seq[(String, String,_)]):Metadata = sq.map(k=>t32Entry(k._1, k._2, k._3))

}
