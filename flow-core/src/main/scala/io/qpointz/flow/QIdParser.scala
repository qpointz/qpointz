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

private[flow] object QIdParser {

  //scalastyle:off
  private val idr = """^(?<ns>(?>qp\:)([a-zA-Z][a-zA-Z0-9]*)){1}(?<h>(\/([a-zA-Z][a-zA-Z0-9]*))*)(?<g>(\:([a-zA-Z][a-zA-Z0-9]*)){0,2})(?<id>\#[a-zA-Z][a-zA-Z0-9]*){0,1}$""".r
  //scalastyle:on

  def parse(in:String):QId = parseOp(in) match {
    case Some(id) => id
    case _ => throw new IllegalArgumentException(s"${in} can't be parsed. expected format [qp:]//<namespace>/<hierarchy>/*<group>/<typename>[#id]")
  }

  def parseOp(in:String):Option[QId] = {
    idr.findFirstMatchIn(in) match {
      case Some(m) => matchToQId(m)
      case _ =>  None
    }
  }

  def matchToQId(m: Regex.Match):Option[QId] = {
    val ns = namespace(m)
    val h = hierarchy(m)
    val (g,t) = groupAndType(m)
    val ref = id(m)
    (ns, h, g, t, ref) match {
      case (ns, h, Some(g), Some(tn), Some(id))   => Some(RefId(ns, h, g, tn, id))
      case (ns, h, Some(g), Some(tn), None)       => Some(TypeId(ns, h, g, tn))
      case (ns, h, Some(g), None, None)           => Some(GroupId(ns, h, g))
      case (ns, h, None, None, None) if h.isEmpty => Some(NamespaceId(ns))
      case (ns, h, None, None, None)              => Some(HierarchyId(ns, h))
      case _                                            => None
    }
  }

  def emptyOrOp[T](in:String, pr: String=> T):Option[T] = {
    in match {
      //scalastyle: off
      case null => None
      case s if s.isEmpty => None
      case h => Some(pr(h))
      //scalastyle: on
    }
  }

  def emptyOr[T](in:String, pr: String=> T):T = {
    emptyOrOp(in, pr) match {
      case Some(v) => v
      case _ => throw new IllegalArgumentException(s"String is null or empty ${in}")
    }
  }

  private def namespace(m: Regex.Match) = emptyOr[String](m.group("ns"), in=> {
    in.substring(3)
  })

  private def hierarchy(m: Regex.Match) = {
    val mayBeHierarcy = emptyOrOp[Seq[String]](m.group("h"), in => {
      in.substring(1)
        .split(raw"\/")
        .toSeq
    })
    mayBeHierarcy match {
      case Some(s)=> s
      case _ => Seq()
    }
  }

  private def groupAndType(m: Regex.Match) = {
    val mayBegt = emptyOrOp(m.group("g"), in => {
      in.substring(1)
        .split(raw"\:")
        .toList
    })
    mayBegt match {
      case Some(g :: Nil)      => (Some(g), None)
      case Some(g :: t :: Nil) => (Some(g), Some(t))
      case _                   => (None, None)
    }
  }

  private def id(m: Regex.Match) = emptyOrOp(m.group("id"), in => {
    in.substring(1)
  })

}
