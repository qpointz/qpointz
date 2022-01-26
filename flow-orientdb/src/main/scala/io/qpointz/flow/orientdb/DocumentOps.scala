/*
 * Copyright 2021 qpointz.io
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

package io.qpointz.flow.orientdb

import com.orientechnologies.orient.core.db.ODatabaseSession
import com.orientechnologies.orient.core.record.impl.ODocument
import org.json4s.{Extraction, Formats}

import scala.jdk.CollectionConverters._


trait OrientFormat[T] {
  val jsonFormat :Formats
  def className: String
  def keyFields : Set[String]
  def key(v:T) : Map[String, Any]
}

class DocumentOps[T](implicit val db:ODatabaseSession, implicit val fmt:OrientFormat[T]) {

  implicit val jsonFormat: Formats = fmt.jsonFormat

  lazy val keyCondition= fmt.keyFields.map(x=> s"${x} = ?").concat(" AND ")
  lazy val byKeySelect = s"SELECT FROM ${fmt.className} WHERE ${keyCondition}"

  def query(q:String, args: Any*):Seq[Option[ODocument]] = {
    db
      .query(q, args)
      .asScala
      .map (_.getElement match {
        case x if x.isEmpty => None
        case y => Some(y.get())
      })
      .map ({
        case Some(d:ODocument) => Some(d)
        case x => None
      })
      .toSeq
  }


  def insert(v:T):Unit = {
    val n = db.newInstance[ODocument]()
    val jv = Extraction.decompose(v)
  }

}
