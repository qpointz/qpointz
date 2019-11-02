/*
 * Copyright  2019 qpointz.io
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
 */

package datamodel

case class Attribute (name:String,
                      `type`:AttributeType,
                      required:Boolean,
                      defaultValue:Option[Any])

object Attribute {

  def required(name:String, `type`:AttributeType):Attribute = {
    Attribute(name, `type`, required = true,  None)
  }

  def optional(name:String, `type`:AttributeType, defaultValue: Option[Any] = None):Attribute = {
    Attribute(name, `type`, required = false,  defaultValue)
  }
}

object AttributeBuilder {
  def required(name:String, `type`:AttributeType, defaultValue: Option[Any] = None):AttributeBuilder = {
    new AttributeBuilder()
      .name(name)
      .ofType(`type`)
      .withDefault(defaultValue)
      .required()
  }

  def optional(name:String, `type`:AttributeType, defaultValue: Option[Any] = None):AttributeBuilder = {
    AttributeBuilder.required(name, `type`, defaultValue)
      .optional()
  }
}

object AttributeType extends Enumeration {

  type AttributeType = Value

  // scalastyle:off magic.number
  val Bool: AttributeType = Value(0x01, "bool")

  val String: AttributeType = Value(0x02, "string")

  val Int32: AttributeType = Value(0x03, "int32")

  val Int64: AttributeType = Value(0x04, "int64")

  val Float: AttributeType = Value(0x06, "float")

  val Double: AttributeType = Value(0x07, "double")
  // scalastyle:on magic.number
}


class AttributeBuilder() {
  private var mayBeName : Option[String] = None
  private var mayBeType:Option[AttributeType] = None
  private var isRequired: Boolean = false
  private var mayBeDefaultVal:Option[Any] = None

  def name(n:String):AttributeBuilder = {
    mayBeName = Some(n)
    this
  }

  def ofType(of:AttributeType):AttributeBuilder = {
    mayBeType = Some(of)
    this
  }

  def required():AttributeBuilder = {
    isRequired = true
    this
  }

  def optional():AttributeBuilder = {
    isRequired = false
    this
  }

  def withDefault(value:Any):AttributeBuilder = {
    mayBeDefaultVal = Some(value)
    this
  }

  def withNoDefault():AttributeBuilder = {
    mayBeDefaultVal = None
    this
  }

  def build():Attribute = {
    val undefinied = Seq[(String, Option[Any])](
      ("name", mayBeName),
      ("type", mayBeType)
    ).filter(_._2.isEmpty)

    if (undefinied.nonEmpty) {
      val und = undefinied.map(_._1).mkString(",")
      throw new DataModelException(s"Undefinied $und")
    } else {
      datamodel.Attribute(mayBeName.get, mayBeType.get, isRequired, mayBeDefaultVal)
    }
  }
}
