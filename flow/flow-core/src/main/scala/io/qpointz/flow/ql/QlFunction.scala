/*
 * Copyright 2022 qpointz.io
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

package io.qpointz.flow.ql

import io.qpointz.flow.Record

import scala.util.{Failure, Success, Try}

trait QRecordFunction[+R] extends (Record => R)
sealed trait QRecordProjectionFunction extends QRecordFunction[Record]
sealed trait QRecordMatchFunction extends QRecordFunction[Boolean]

trait QlFunction { self =>
  def apply(args:Seq[Any]):Try[Any]
}

object QlFunction {

  def funcimpl[FN, MAPARGS](fn: FN, map:Seq[Any]=>MAPARGS)(call:(FN,MAPARGS)=>Any)(args:Seq[Any]):Try[Any] = {
    val mayBeTry = args.map {
      case x:Try[_] => x
      case a => Success(a)
    }
    mayBeTry.find(_.isFailure) match {
      case Some(f) => f
      case None => try {
        val mapped = map(mayBeTry.map{case x:Try[_] => x.get})
        Success(call(fn,mapped))
      } catch {
        case ex:Exception => {
          Failure(ex)
        }
      }
    }
  }

  type QFuncImpl = Seq[Any] => Try[Any]

  def func0[R](fn:()=>R):QFuncImpl = (a:Seq[Any]) => try {
    Success(fn())
  } catch {
    case ex:Exception => Failure(ex)
  }

  def func[T1, R](fn:(T1)=>R, map:Any => T1):QFuncImpl = {
    funcimpl(fn,x => map(x.head))((f, a) => f(a))
  }

  def func[T1, T2, R](fn:(T1,T2)=>R, map:Seq[Any] => (T1,T2)):QFuncImpl = {
    funcimpl(fn,x => map(x))((f, a) => f.tupled(a)) _
  }

  def func[T1, T2, T3, R](fn:(T1,T2, T3)=>R, map:Seq[Any] => (T1,T2, T3)):QFuncImpl = {
    funcimpl(fn,x => map(x))((f, a) => f.tupled(a))
  }

  def func[T1, T2, T3, T4, R](fn:(T1,T2, T3, T4)=>R, map:Seq[Any] => (T1,T2,T3, T4)):QFuncImpl = {
    funcimpl(fn,x => map(x))((f, a) => f.tupled(a))
  }
  def func[T1, T2, T3, T4, T5, R](fn:(T1,T2, T3, T4, T5)=>R, map:Seq[Any] => (T1,T2, T3, T4, T5)):QFuncImpl = {
    funcimpl(fn,x => map(x))((f, a) => f.tupled(a))
  }

  def func[T1, T2, T3, T4, T5, T6, R](fn:(T1,T2, T3, T4, T5, T6)=>R, map:Seq[Any] => (T1,T2, T3, T4, T5, T6)):QFuncImpl = {
    funcimpl(fn,x=> map(x))((f,a)=>f.tupled(a))
  }

  def func[T1, T2, T3, T4, T5, T6, T7, R](fn:(T1,T2, T3, T4, T5, T6, T7)=>R, map:Seq[Any] => (T1,T2, T3, T4, T5, T6, T7)):QFuncImpl = {
    funcimpl(fn,x=> map(x))((f,a)=>f.tupled(a))
  }
  def func[T1, T2, T3, T4, T5, T6, T7, T8, R](fn:(T1,T2, T3, T4, T5, T6, T7, T8)=>R,
                                               map:Seq[Any] => (T1,T2, T3, T4, T5, T6, T7, T8)):QFuncImpl = {
    funcimpl(fn,x=> map(x))((f,a)=>f.tupled(a))
  }

}


