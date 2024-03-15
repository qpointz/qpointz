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
import io.qpointz.flow.{Record, ql}
import shapeless._
import shapeless.syntax.std.traversable.traversableOps

trait QlNamedFunction {
  val sqlName:String
}

trait QlFunction[R] extends QlNamedFunction {
  def apply(args:Seq[Any]):R
}

trait QlFunction0[R] extends QlFunction[R] with (() => R) {self=>
  override def apply(args:Seq[Any]):R = {
    if (args.length>0) {
      throw new NoSuchElementException("Number of passed parameters exceeds number of expected parameters 0")
    }
    self.apply()
  }
}

trait QlFunction1[T1,R] extends QlFunction[R] with ((T1) => R) {
  protected val fn: T1 => R
  override def apply(v1: T1): R = fn(v1)
  override def apply(args:Seq[Any]):R = fn(args.toHList[T1::HNil].get.head)
}

trait QlFunction2[T1, T2, R] extends QlFunction[R] with  ((T1,T2) => R) {
  protected val fn: (T1, T2) => R
  override def apply(v1: T1, v2:T2): R = fn(v1, v2)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::HNil].get.tupled)
}

trait QlFunction3[T1, T2, T3, R] extends QlFunction[R] with  ((T1,T2,T3) => R) {
  protected val fn: (T1, T2, T3) => R
  override def apply(v1: T1, v2:T2, v3:T3): R = fn(v1, v2, v3)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::T3::HNil].get.tupled)
}

trait QlFunction4[T1, T2, T3, T4, R] extends QlFunction[R] with  ((T1,T2,T3,T4) => R) {
  protected val fn: (T1, T2, T3, T4) => R
  override def apply(v1: T1, v2:T2, v3:T3, v4:T4): R = fn(v1, v2, v3, v4)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::T3::T4::HNil].get.tupled)
}

trait QlFunction5[T1, T2, T3, T4, T5, R] extends QlFunction[R] with  ((T1,T2,T3,T4,T5) => R) {
  protected val fn: (T1, T2, T3, T4,T5) => R
  override def apply(v1: T1, v2:T2, v3:T3, v4:T4, v5:T5): R = fn(v1, v2, v3, v4, v5)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::T3::T4::T5::HNil].get.tupled)
}

trait QlFunction6[T1, T2, T3, T4, T5, T6, R] extends QlFunction[R] with  ((T1, T2, T3, T4, T5, T6) => R) {
  protected val fn: (T1, T2, T3, T4, T5, T6) => R
  override def apply(v1: T1, v2:T2, v3:T3, v4:T4, v5:T5, v6:T6): R = fn(v1, v2, v3, v4, v5, v6)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::T3::T4::T5::T6::HNil].get.tupled)
}

trait QlFunction7[T1, T2, T3, T4, T5, T6, T7, R] extends QlFunction[R] with  ((T1, T2, T3, T4, T5, T6,T7) => R) {
  protected val fn: (T1, T2, T3, T4, T5, T6, T7) => R
  override def apply(v1: T1, v2:T2, v3:T3, v4:T4, v5:T5, v6:T6, v7:T7): R = fn(v1, v2, v3, v4, v5, v6,v7)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::T3::T4::T5::T6::T7::HNil].get.tupled)
}

trait QlFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R] extends QlFunction[R] with  ((T1, T2, T3, T4, T5, T6, T7, T8) => R) {
  protected val fn: (T1, T2, T3, T4, T5, T6, T7, T8) => R
  override def apply(v1: T1, v2:T2, v3:T3, v4:T4, v5:T5, v6:T6, v7:T7, v8:T8): R = fn(v1, v2, v3, v4, v5, v6,v7, v8)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::T3::T4::T5::T6::T7::T8::HNil].get.tupled)
}

trait QlFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] extends QlFunction[R] with  ((T1, T2, T3, T4, T5, T6, T7, T8, T9) => R) {
  protected val fn: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R
  override def apply(v1: T1, v2:T2, v3:T3, v4:T4, v5:T5, v6:T6, v7:T7, v8:T8, v9:T9): R = fn(v1, v2, v3, v4, v5, v6, v7, v8, v9)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::T3::T4::T5::T6::T7::T8::T9::HNil].get.tupled)
}

trait QlFunction10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] extends QlFunction[R] with  ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R) {
  protected val fn: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R
  override def apply(v1: T1, v2:T2, v3:T3, v4:T4, v5:T5, v6:T6, v7:T7, v8:T8, v9:T9, v10:T10): R = fn(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)
  override def apply(args:Seq[Any]):R = fn.tupled(args.toHList[T1::T2::T3::T4::T5::T6::T7::T8::T9::T10::HNil].get.tupled)
}

trait QlRecordFunction[R] extends QlFunction[R] {
  def apply(r:Record, args:Seq[Any]):R = apply(r +: args)
}

trait QlRecordFunction0[R] extends QlRecordFunction[R] with ((Record) => R) {self=>
  override def apply(r:Record, args:Seq[Any]):R = self.apply(r)
}
trait QlRecordFunction1[T1, R] extends QlRecordFunction[R]  with QlFunction2[Record, T1, R] with ((Record, T1) => R) {
  override def apply(v1: Record, t1:T1): R = fn(v1, t1)
}
trait QlRecordFunction2[T1, T2, R] extends QlRecordFunction[R]  with QlFunction3[Record, T1, T2, R] with ((Record, T1, T2) => R) {
  override def apply(v1: Record, t1:T1, t2:T2): R = fn(v1, t1, t2)
}

trait QlRecordFunction3[T1, T2, T3, R] extends QlRecordFunction[R]  with QlFunction4[Record, T1, T2, T3, R] with ((Record, T1, T2, T3) => R) {
  override def apply(v1: Record, t1:T1, t2:T2, t3:T3): R = fn(v1, t1, t2, t3)
}

trait QlRecordFunction4[T1, T2, T3, T4, R] extends QlRecordFunction[R]  with QlFunction5[Record, T1, T2, T3, T4, R] with ((Record, T1, T2, T3, T4) => R) {
  override def apply(v1: Record, t1:T1, t2:T2, t3:T3, t4:T4): R = fn(v1, t1, t2, t3, t4)
}

trait QlRecordFunction5[T1, T2, T3, T4, T5, R] extends QlRecordFunction[R]  with QlFunction6[Record, T1, T2, T3, T4, T5, R] with ((Record, T1, T2, T3, T4, T5) => R) {
  override def apply(v1: Record, t1:T1, t2:T2, t3:T3, t4:T4, t5:T5): R = fn(v1, t1, t2, t3, t4, t5)
}

trait QlRecordFunction6[T1, T2, T3, T4, T5, T6, R] extends QlRecordFunction[R]  with QlFunction7[Record, T1, T2, T3, T4, T5, T6, R] with ((Record, T1, T2, T3, T4, T5, T6) => R) {
  override def apply(v1: Record, t1:T1, t2:T2, t3:T3, t4:T4, t5:T5, t6:T6): R = fn(v1, t1, t2, t3, t4, t5, t6)
}

trait QlRecordFunction7[T1, T2, T3, T4, T5, T6, T7, R] extends QlRecordFunction[R]  with QlFunction8[Record, T1, T2, T3, T4, T5, T6, T7, R] with ((Record, T1, T2, T3, T4, T5, T6, T7) => R) {
  override def apply(v1: Record, t1:T1, t2:T2, t3:T3, t4:T4, t5:T5, t6:T6, t7:T7): R = fn(v1, t1, t2, t3, t4, t5, t6, t7)
}

trait QlRecordFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R] extends QlRecordFunction[R]  with QlFunction9[Record, T1, T2, T3, T4, T5, T6, T7, T8, R] with ((Record, T1, T2, T3, T4, T5, T6, T7, T8) => R) {
  override def apply(v1: Record, t1:T1, t2:T2, t3:T3, t4:T4, t5:T5, t6:T6, t7:T7, t8:T8): R = fn(v1, t1, t2, t3, t4, t5, t6, t7, t8)
}

trait QlRecordFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] extends QlRecordFunction[R]  with QlFunction10[Record, T1, T2, T3, T4, T5, T6, T7, T8, T9, R] with ((Record, T1, T2, T3, T4, T5, T6, T7, T8, T9) => R) {
  override def apply(v1: Record, t1:T1, t2:T2, t3:T3, t4:T4, t5:T5, t6:T6, t7:T7, t8:T8, t9:T9): R = fn(v1, t1, t2, t3, t4, t5, t6, t7, t8, t9)
}

object QlFunction {
  def apply[R](name:String, f:()=>R):QlFunction0[R]= new QlFunction0[R] {
    override def apply(): R = f()
    override val sqlName: String = name }

  def apply[T1,R](name:String, f: T1=>R):QlFunction1[T1,R] = new QlFunction1[T1 , R] {
    override protected val fn: T1 => R = f
    override val sqlName: String = name }

  def apply[T1, T2, R](name:String, f: (T1,T2)=>R):QlFunction2[T1,T2,R] = new QlFunction2[T1 , T2, R] {
    override protected val fn: (T1,T2) => R = f
    override val sqlName: String = name }

  def apply[T1, T2, T3, R](name:String, f: (T1,T2,T3)=>R):QlFunction3[T1,T2,T3, R] = new QlFunction3[T1 , T2, T3, R] {
    override protected val fn: (T1,T2,T3) => R = f
    override val sqlName: String = name }

  def apply[T1, T2, T3, T4, R](name:String, f: (T1,T2,T3, T4)=>R):QlFunction4[T1,T2,T3, T4, R] = new QlFunction4[T1 , T2, T3, T4, R] {
    override protected val fn: (T1,T2,T3, T4) => R = f
    override val sqlName: String = name }

  def apply[T1, T2, T3, T4, T5, R](name:String, f: (T1, T2, T3, T4, T5)=>R):QlFunction5[T1, T2, T3, T4, T5, R] = new QlFunction5[T1 , T2, T3, T4, T5, R] {
    override protected val fn: (T1, T2, T3, T4, T5) => R = f
    override val sqlName: String = name }

  def apply[T1, T2, T3, T4, T5, T6, R](name:String, f: (T1, T2, T3, T4, T5, T6)=>R):QlFunction6[T1, T2, T3, T4, T5, T6, R] = new QlFunction6[T1 , T2, T3, T4, T5, T6, R] {
    override protected val fn: (T1, T2, T3, T4, T5, T6) => R = f
    override val sqlName: String = name }

  def apply[T1, T2, T3, T4, T5, T6, T7, R](name:String, f: (T1, T2, T3, T4, T5, T6, T7)=>R):QlFunction7[T1, T2, T3, T4, T5, T6, T7, R] = new QlFunction7[T1 , T2, T3, T4, T5, T6, T7, R] {
    override protected val fn: (T1, T2, T3, T4, T5, T6, T7) => R = f
    override val sqlName: String = name }

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, R](name:String, f: (T1, T2, T3, T4, T5, T6, T7, T8)=>R):QlFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R] = new QlFunction8[T1 , T2, T3, T4, T5, T6, T7, T8, R] {
    override protected val fn: (T1, T2, T3, T4, T5, T6, T7, T8) => R = f
    override val sqlName: String = name }

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](name:String, f: (T1, T2, T3, T4, T5, T6, T7, T8, T9)=>R):QlFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] = new QlFunction9[T1 , T2, T3, T4, T5, T6, T7, T8, T9, R] {
    override protected val fn: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R = f
    override val sqlName: String = name }

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](name:String, f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)=>R):QlFunction10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] = new QlFunction10[T1 , T2, T3, T4, T5, T6, T7, T8, T9, T10, R] {
    override protected val fn: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R = f
    override val sqlName: String = name }
}

object QlRecordFunction {
  def apply[R](name:String, f:(Record)=>R):QlRecordFunction0[R] = new QlRecordFunction0[R] {
    override def apply(v1: Record): R = f(v1)
    override def apply(args: Seq[Any]): R = apply(args.toHList[Record::HNil].get.head)
    override val sqlName: String = name
  }

  def apply[T1, R](name:String,f:(Record, T1)=>R):QlRecordFunction1[T1,R] = new QlRecordFunction1[T1, R] {
    override protected val fn: (Record, T1) => R = f
    override val sqlName: String = name
  }

  def apply[T1, T2, R](name:String,f:(Record, T1, T2)=>R):QlRecordFunction2[T1,T2,R] = new QlRecordFunction2[T1, T2, R] {
    override protected val fn: (Record, T1, T2) => R = f
    override val sqlName: String = name
  }

  def apply[T1, T2, T3, R](name:String,f:(Record, T1, T2, T3)=>R):QlRecordFunction3[T1,T2,T3, R] = new QlRecordFunction3[T1, T2, T3, R] {
    override protected val fn: (Record, T1, T2, T3) => R = f
    override val sqlName: String = name
  }

  def apply[T1, T2, T3, T4, R](name:String,f:(Record, T1, T2, T3, T4)=>R):QlRecordFunction4[T1,T2,T3,T4, R] = new QlRecordFunction4[T1, T2, T3, T4, R] {
    override protected val fn: (Record, T1, T2, T3, T4) => R = f
    override val sqlName: String = name
  }

  def apply[T1, T2, T3, T4, T5, R](name:String,f:(Record, T1, T2, T3, T4, T5)=>R):QlRecordFunction5[T1, T2, T3, T4, T5, R] = new QlRecordFunction5[T1, T2, T3, T4, T5, R] {
    override protected val fn: (Record, T1, T2, T3, T4, T5) => R = f
    override val sqlName: String = name
  }

  def apply[T1, T2, T3, T4, T5, T6, R](name:String,f:(Record, T1, T2, T3, T4, T5, T6)=>R):QlRecordFunction6[T1, T2, T3, T4, T5, T6, R] = new QlRecordFunction6[T1, T2, T3, T4, T5, T6, R] {
    override protected val fn: (Record, T1, T2, T3, T4, T5, T6) => R = f
    override val sqlName: String = name
  }

  def apply[T1, T2, T3, T4, T5, T6, T7, R](name:String,f:(Record, T1, T2, T3, T4, T5, T6, T7)=>R):QlRecordFunction7[T1, T2, T3, T4, T5, T6, T7, R] = new QlRecordFunction7[T1, T2, T3, T4, T5, T6, T7, R] {
    override protected val fn: (Record, T1, T2, T3, T4, T5, T6, T7) => R = f
    override val sqlName: String = name
  }

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, R](name:String,f:(Record, T1, T2, T3, T4, T5, T6, T7, T8)=>R):QlRecordFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R] = new QlRecordFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R] {
    override protected val fn: (Record, T1, T2, T3, T4, T5, T6, T7, T8) => R = f
    override val sqlName: String = name
  }

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](name:String,f:(Record, T1, T2, T3, T4, T5, T6, T7, T8, T9)=>R):QlRecordFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] = new QlRecordFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] {
    override protected val fn: (Record, T1, T2, T3, T4, T5, T6, T7, T8, T9) => R = f
    override val sqlName: String = name
  }
}