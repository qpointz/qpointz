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
 *  limitations under the License.
 */

package io.qpointz.flow.utils


import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success, Try}

object ReflectUtils {
  val log = Logger("default")

  def instancesOf[T](all:Iterable[Any])(implicit m:Manifest[T]):Seq[T] = {
    all.map{
      case x:T => Some(x)
      case _ => None
    }
      .filter(_.isDefined)
      .map(_.get)
      .toSeq
  }

  def tryNewInstanceByName[T](cn: String)(implicit m: Manifest[T]): Try[T] = {
    tryClassForName(cn) match {
      case Success(cl: Class[_]) => try {
        val i = cl.getConstructor()
          .newInstance()
          .asInstanceOf[T]
        Success(i)
      }
        catch {
        case ex : Throwable => Failure(ex)
      }
      case Success(value) => Failure(new RuntimeException(s"Non class ${value}"))
      case Failure(th) => Failure(th)
    }
  }

  def tryClassForName(cn: String): Try[Class[_]] = {
    try {
      val cl = Class.forName(cn)
      Success(cl)
    } catch {
      case ex : Throwable => {
        log.error(s"${cn} class not found")
        Failure(ex)
      }
    }
  }

}
