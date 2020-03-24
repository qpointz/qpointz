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
 */

package io.qpointz.flow.transformations

import java.util.UUID

import io.qpointz.flow.{AttributeKey, Attributes, Metadata, Record}
import io.qpointz.flow.transformations.TransformationsMeta.empty
import TransformationsMeta._

final case class GenerateUUID(att: AttributeKey) extends RecordTransformationFunc {
  override def transform(r: Record): (Attributes, Metadata) = (
    Map(att -> UUID.randomUUID()),
    empty.generateUUID.put(att))
}

final case class GenerateUUIDString(att: AttributeKey) extends RecordTransformationFunc {
  override def transform(r: Record): (Attributes, Metadata) = (
    Map(att -> UUID.randomUUID().toString),
    empty.generateUUIDString.put(att))
}
