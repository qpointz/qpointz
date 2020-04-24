/*
 * Copyright 2020 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.qpointz.flow

package object transformations {

  object TransformationsMeta extends MetadataGroup("transform:record") {

    val generateUUIDString: EntryDefinition[String] = entry[String]("generate:uuid:value")
    val generateUUID: EntryDefinition[String] = entry[String]("generate:uuid:value")
    val hostnameShort: EntryDefinition[String] = entry[String]("hostname:short")
    val hostnameCanonical: EntryDefinition[String] = entry[String]("hostname:canonical")
  }

  trait Transformation {
  }

  trait RecordTransformation extends Transformation {
    def transform(r:Record):Record
  }

  case class AttributeTransformResult(attributes:Attributes, meta:Metadata)

  trait AttributeTransformation extends Transformation {
    def transform(r:Record):AttributeTransformResult
  }

}
