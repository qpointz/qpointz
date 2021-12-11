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
import com.orientechnologies.orient.core.record.ORecord
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.executor.{OResult, OResultSet}
import org.json4s.{Formats, JValue, JsonFormat}

import scala.jdk.CollectionConverters._

object OrientOps {

  implicit class OResultSetImplicits(resultSet:OResultSet) {

    implicit def asIterator:Iterator[OResult] = resultSet.asScala

    def asRecords[T<:ORecord]():Iterator[Option[T]] = resultSet
      .asIterator
      .map(x=> x.getElement)
      .map{
        case elem if elem.isPresent => Some(elem.get().asInstanceOf[T])
        case _ => None
      }

    def asJsonOp:Iterator[Option[JValue]] = asRecords[ODocument].map {
      case Some(d)=> Some(d.asJValue)
      case _ => None
    }

    def asJson:Iterator[JValue] = asJsonOp.map(_.get)

    def asOp[T](implicit fmt:Formats, m:Manifest[T]): Iterator[Option[T]] = asJsonOp.map {
      case Some(jv)=> jv.extractOpt[T]
      case _ => None
    }

    def as[T](implicit fmt:Formats, m:Manifest[T]):Iterator[T] = asOp[T].map(_.get)

  }

}
