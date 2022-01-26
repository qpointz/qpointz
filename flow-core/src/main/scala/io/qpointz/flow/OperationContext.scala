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

import com.typesafe.scalalogging.Logger

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait ProgressContext {
  var state:Int = 0
  var tot:Int = 0

  def total(t: Int): Unit = {
    tot = t
  }

  def reset(): Unit = {
    state = 0
  }

  def progress(i: Int): Unit = {
    state = if (i>tot) {
      tot
    } else {
      i
    }
    logProgress()
  }

  def inc(i: Int): Unit = {
    val ns = state + i
    state = if (ns > tot) {
      tot
    }else {
      ns
    }
    logProgress()
  }

  def log(state: Int, tot: Int): Unit

  def logProgress(): Unit = log(state, tot)

  def dec(i: Int): Unit = {
    val ns = state - i
    state = if (ns < 0) {
      0
    }else {
      ns
    }
    logProgress()
  }

}

trait StatisticMessage {}
trait StatisticContext {
  def log(sm:StatisticMessage):Unit
}

trait AuditMessage {}
trait AuditContext {
  def log(am:AuditMessage):Unit
}

trait ControlMessage {}
trait ControlContext {
  def log(cm:ControlMessage):Unit
}

trait OperationContext {
  val log : Logger
  val progress: ProgressContext
  val statistic: StatisticContext
  val audit: AuditContext
  val control : ControlContext
}

trait WithOperationContext {
  implicit val ctx : OperationContext
}

object OperationContext {

  private class ProgressContextImpl(private val l:Logger) extends ProgressContext {
    override def log(state: AttributeIndex, tot: AttributeIndex): Unit = l.info(s"Progress: ${state}/${tot}")
  }

  private class DefaultContextBase(private val logger: Logger) {
      protected def defaultLog(x:Any): Unit = {
        logger.info(x.toString)
      }
  }

  private class StatisticContextImpl(private val l:Logger)
    extends DefaultContextBase(l)
    with StatisticContext
  {
    override def log(sm: StatisticMessage): Unit = defaultLog(sm)
  }

  private class AuditContextImpl(private val l:Logger)
    extends DefaultContextBase(l)
    with AuditContext{
    override def log(am: AuditMessage): Unit = defaultLog(s"{AUDIT} $am")
  }

  private class ControlContextImpl(private val l:Logger)
    extends DefaultContextBase(l)
    with ControlContext{
    override def log(cm: ControlMessage): Unit = defaultLog(cm)
  }


  implicit val defaultContext: OperationContext = new OperationContext {
   override val log: Logger = Logger("default")
   override val progress: ProgressContext = new ProgressContextImpl(log)
   override val statistic: StatisticContext = new StatisticContextImpl(log)
   override val audit: AuditContext = new AuditContextImpl(log)
   override val control: ControlContext = new ControlContextImpl(log)
 }

}

object InMemoryOperationContext extends OperationContext {

  private val l : Logger = Logger("default")
  override val log: Logger = l

  override val progress: ProgressContext = new ProgressContext {
    override def log(state: AttributeIndex, tot: AttributeIndex): Unit = l.info(s"Progress: ${state}/${tot}")
  }

  val stats = new ArrayBuffer[StatisticMessage]()

  override val statistic: StatisticContext = new StatisticContext {
    override def log(sm: StatisticMessage): Unit = {
      stats += sm
    }
  }

  val audits = new ArrayBuffer[AuditMessage]()
  override val audit: AuditContext = new AuditContext {
    override def log(am: AuditMessage): Unit = {
      audits += am
    }
  }

  val controls = new ArrayBuffer[ControlMessage]()
  override val control: ControlContext = new ControlContext {
    override def log(cm: ControlMessage): Unit = {
      controls+=cm
    }
  }
}
