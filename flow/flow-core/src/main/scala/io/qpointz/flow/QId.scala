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

package io.qpointz.flow

import java.net.URI
import scala.util.matching.Regex

trait QId
trait QNamespaceId extends QId {val ns:String}
trait QHierarchyId extends QNamespaceId {val hierarchy:Seq[String]}
trait QGroupId extends QHierarchyId {val group:String}
trait QTypeId extends QGroupId {val typeName:String}
trait QRefId  extends QTypeId {val rid:String}

case class NamespaceId(ns:String) extends QNamespaceId
case class HierarchyId(ns:String, hierarchy:Seq[String]) extends QHierarchyId
case class GroupId(ns:String, hierarchy:Seq[String], group:String) extends QGroupId
case class TypeId(ns:String, hierarchy:Seq[String], group:String, typeName:String) extends QTypeId
case class RefId(ns:String, hierarchy:Seq[String], group:String, typeName:String, rid:String) extends QRefId

object QId {

  implicit class QIdMethods(qid:QId) {

    def is(pf: PartialFunction[QId, Boolean]):Boolean = {
      if (pf.isDefinedAt(qid)) pf(qid ) else false
    }

    def isRefId:Boolean            = is {case _:RefId => true}
    def isTypeId:Boolean        = is {case _:TypeId => true}
    def isGroupId:Boolean       = is {case _:GroupId => true}
    def isHierarchyId:Boolean   = is {case _:HierarchyId => true}
    def isNamespaceId:Boolean   = is {case _:NamespaceId => true}

    def toURI:URI = {
      def hToString(h:Seq[String]) = h match {
        case l if l.isEmpty => ""
        case l => l.mkString("/","/","")
      }
      val s = qid match {
        case qid:RefId        => s"qp:${qid.ns}${hToString(qid.hierarchy)}:${qid.group}:${qid.typeName}#${qid.rid}"
        case qid:TypeId       => s"qp:${qid.ns}${hToString(qid.hierarchy)}:${qid.group}:${qid.typeName}"
        case qid:GroupId      => s"qp:${qid.ns}${hToString(qid.hierarchy)}:${qid.group}"
        case qid:HierarchyId  => s"qp:${qid.ns}${hToString(qid.hierarchy)}"
        case qid:NamespaceId  => s"qp:${qid.ns}"
      }
      URI.create(s)
    }
  }

  implicit class QNamespaceMethods(nid:QNamespaceId) {
    def hierarchyId(hs:String*):QHierarchyId = HierarchyId(nid.ns, hs)
    def hierarchyId():QHierarchyId = HierarchyId(nid.ns, Seq())
    def groupId(g:String):QGroupId = hierarchyId().groupId(g)
  }

  implicit class QHierarchyMethods(his:QHierarchyId) {
    def groupId(g:String):QGroupId = GroupId(his.ns, his.hierarchy, g)
    def hierarchyId(hs:String*):HierarchyId = HierarchyId(his.ns, his.hierarchy ++ hs)
  }

  implicit class QGroupMethods(gid:QGroupId) {
    def typeId(tn:String):QTypeId = TypeId(gid.ns, gid.hierarchy, gid.group, tn)
  }

  implicit class QTypeIdMethods(tid:QTypeId) {
    def metadataGroupKey:String = tid.toURI.toString
    def jsonTypeHint:String = tid.toURI.toString
    def refId(rid:String):RefId = RefId(tid.ns, tid.hierarchy, tid.group, tid.typeName, rid)
  }

  def fromStringOp(in:String):Option[QId] = QIdParser.parseOp(in)

  def fromString(in:String):QId = QIdParser.parse(in)

}

