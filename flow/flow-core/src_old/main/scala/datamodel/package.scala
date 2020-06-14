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

package object datamodel {

  type MetadataGroupKey = String

  type MetadataGroupValues = Map[String, Any]

  type MetadataGroup = (MetadataGroupKey, MetadataGroupValues)

  type Metadata = List[MetadataGroup]

  object Metadata {
    val empty:Metadata = List()
  }

  type AttributesList = Seq[Attribute]

  type AttributeType = io.qpointz.datamodel.AttributeType.AttributeType

  class DataModelException(private val message: String = "",
                         private val cause: Throwable = None.orNull
                          ) extends EnzymeException(message, cause)

}
