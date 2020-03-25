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

package io.qpointz.flow.transformations
import io.qpointz.flow.{Attributes, Metadata, Record}

trait RecordTransformationGroup extends RecordTransformation {
  val transformations:Seq[RecordTransformation]

  override def transform(r: Record): Record = {
    transformations.foldLeft(r)( (record,transform)=> transform.transform(record))
  }
}

trait AttributeTransformationGroup extends RecordTransformation {
  val transformations:Seq[AttributeTransformation]

  def applyTransform(r:Record, tr:(Attributes, Metadata)):Record

  override def transform(r: Record): Record = {
    transformations.foldLeft(r)((record,transform)=> applyTransform(record, transform.transform(record)))
  }
}