/*
 *
 *  Copyright 2022 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.qpointz.shape.sql

import java.sql.ResultSet


final case class CompactMap(columns:Array[String], data:Seq[Array[Any]])

object ResultSetMethods {
  implicit class ResultSetMethods(resultSet: ResultSet) {
    def iterator:Iterator[ResultSet] = new Iterator[ResultSet] {
      override def hasNext: Boolean = resultSet.next()
      override def next(): ResultSet = resultSet
    }
    def columns:List[(Int,String)] = {
      val md = resultSet.getMetaData
      (1 to md.getColumnCount).map(idx => idx -> md.getColumnName(idx)).sortBy(_._1).toList
    }
    def asSeq[T](loop:(List[(Int, String)], ResultSet)=>T):Seq[T]= {
      iterator.map(loop(columns, _)).toSeq
    }
    def asMapSeq:Seq[Map[String, Any]] = asSeq {(cols, rs)=>
        cols.map(k => k._2 -> ub(rs.getObject(k._1))).toMap
    }
    def asArraySeq:Seq[Array[Any]] = asSeq {(cols, rs)=>
      (1 to cols.length).map(v=>ub(rs.getObject(v))).toArray
    }
    def asCompactMap:CompactMap = CompactMap(columns.map(_._2).toArray, asArraySeq)
    protected def ub(anyRef: AnyRef):Any = anyRef match {
      case xs:Any => xs
      case _ => throw new Exception("Not supported")
    }
  }
}
