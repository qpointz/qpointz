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

package io.qpointz

package object flow {



  class FlowException(private val message: String = "",
                      private val cause: Throwable = None.orNull
                      ) extends Exception(message, cause)

  type AttributeIndex   = Int
  type AttributeValue   = Any
  type AttributeKey     = String
  type Attribute        = (AttributeKey, AttributeValue)
  type Attributes       = Map[AttributeKey, AttributeValue]
  type Metadata         = Seq[MetaEntry[_]]
  type MetadataGroupKey = String

  object AttributeValue {
    object Null  {}
    object Error {}
    object Empty {}
    object Missing {}
  }

  case class MissingAttributeKey(idx:AttributeIndex)

  object RecordTags {
    val OK:String  = "OK"
    val NOK:String = "NOK"
    val MissingValue:String = "Missing value"
    val UnexpectedValue:String = "Unexpected value"
  }

  object RecordMetadata extends MetadataGroup ("record"){
    val recordLabel:EntryDefinition[String] = entry[String]("label")
    val recordTags:EntryDefinition[Set[String]] = entry[Set[String]]("tags")
  }

  implicit class RecordOps(r:Record) {

    def metadata():Metadata = r match {
      case mt:MetadataProvider => mt.metadata
      case _ => Seq()
    }

  }

  lazy val flowQuids = new PackageQIds {
    override protected val nsName: String = "flow"
  }

}
