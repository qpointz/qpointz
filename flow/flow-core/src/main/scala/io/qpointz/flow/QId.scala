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

import scala.util.matching.Regex

trait QId
case class Id(ns:String, hierarchy:Seq[String], group:String, typeName:String, id:String) extends QId
case class TypeId(ns:String, hierarchy:Seq[String], group:String, typeName:String) extends QId
case class GroupId(ns:String, hierarchy:Seq[String], group:String) extends QId
case class HierarchyId(ns:String, hierarchy:Seq[String]) extends QId
case class NamespaceId(ns:String) extends QId

object QId {

  private type ParseTuple = Option[(Option[String], Seq[String], Option[String], Option[String], Option[String])]

  //scalastyle:off
  private val idr = """^(qp\:){0,1}(?<ns>[a-zA-Z][a-zA-Z0-9]*)(?<h>((?>\/)([a-zA-Z][a-zA-Z0-9]*))*)(?>\/)(?<g>[a-zA-Z][a-zA-Z0-9]*){1}(?>\/)(?<tn>[a-zA-Z][a-zA-Z0-9]*)((?>\#)(?<id>[a-zA-Z][a-zA-Z0-9]*))*$""".r
  //scalastyle:on

  //^(?>qp\:\/\/)(?<ns>[a-zA-Z][a-zA-Z0-9]*)(?<h>((?>\/)([a-zA-Z][a-zA-Z0-9]*))*)(?>\:)(?<g>[a-zA-Z][a-zA-Z0-9]*)(?>\:)(?<tn>[a-zA-Z][a-zA-Z0-9]*)((?>\#)(?<id>[a-zA-Z][a-zA-Z0-9]*))*$

  //goodone
  //^(?>qp\:)(?<ns>\/\/[a-zA-Z][a-zA-Z0-9]*){1}(?<h>(\/([a-zA-Z][a-zA-Z0-9]*))*)(?<g>(\:([a-zA-Z][a-zA-Z0-9]*)){0,2})(?<id>\#[a-zA-Z][a-zA-Z0-9]*){0,1}$

  private def parse(in:String): ParseTuple = {
    def mapToId(m: Regex.Match): ParseTuple = {
      def asOp(gn: String) = {
        m.group(gn) match {
          //scalastyle:off
          case null => None
          //scalastyle:on
          case es if es.isEmpty => None
          case s => Some(s)
        }
      }
      val hs:Seq[String] = m.group("h") match {
        case x if x.length > 0 => x.split(raw"\/").filter(_.nonEmpty).toSeq
        case _ => Seq()
      }
      Some((asOp("ns"), hs, asOp("g"), asOp("tn"), asOp("id")))
    }

    idr.findFirstMatchIn(in) match {
      case Some(r) => mapToId(r)
      case None => None
    }
  }

  def fromString(in:String):QId = fromStringOp(in) match {
    case Some(id) => id
    case _ => throw new IllegalArgumentException(s"${in} can't be parsed. expected format [qp:]<namespace>/<hierarchy>/*<group>/<typename>[#id]")
  }

  def fromStringOp(in: String): Option[QId] = {
      parse(in) match {
        case Some(x) => x match {
          case (Some(ns), h, Some(g), Some(tn), Some(id))   => Some(Id(ns, h, g, tn, id))
          case (Some(ns), h, Some(g), Some(tn), None)       => Some(TypeId(ns, h, g, tn))
          case (Some(ns), h, Some(g), None, None)           => Some(GroupId(ns, h, g))
          case (Some(ns), h, None, None, None)              => Some(HierarchyId(ns, h))
          case (Some(ns), h, None, None, None) if h.isEmpty => Some(NamespaceId(ns))
          case _                                            => None
        }
        case None => None
      }
  }

  implicit class QIdMethods(qid:QId) {

    def is(pf: PartialFunction[QId, Boolean]):Boolean = {
      if (pf.isDefinedAt(qid)) pf(qid ) else false
    }

    def isId:Boolean            = is {case _:Id => true}
    def isTypeId:Boolean        = is {case _:TypeId => true}
    def isGroupId:Boolean       = is {case _:GroupId => true}
    def isHierarchyId:Boolean   = is {case _:HierarchyId => true}
    def isNamespaceId:Boolean   = is {case _:NamespaceId => true}

  }
}

