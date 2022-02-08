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

package io.qpointz.flow.transformations

import io.qpointz.flow.MetadataMethods._
import io.qpointz.flow.serialization.Json._
import io.qpointz.flow.serialization.JsonProtocol
import io.qpointz.flow.transformations.TransformationsMeta._
import io.qpointz.flow.{AttributeKey, Metadata, MetadataAwareWithId, QTypeId, Record}
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, Extraction, Formats, JObject, JValue}

import java.util.UUID

abstract class CustomAttributeTransform[T](
      transformName:String,
      recordTransform:(Record, MetadataAwareWithId)=>AttributeTransformResult,
      serializer: Formats => (PartialFunction[JValue, T], PartialFunction[Any, JValue]),
      gm: MetadataAwareWithId=>Metadata
)(implicit m:Manifest[T]) extends AttributeTransformation with MetadataAwareWithId
{
  object Support {
    val typeId: QTypeId = io.qpointz.flow.flowQuids.transformation(transformName)
    val jsonProtocol: JsonProtocol[T] = JsonProtocol(metaId, Serializer)
  }
  override val metaId: QTypeId = Support.typeId
  private object Serializer extends CustomSerializer[T](serializer)
  override def transform(r: Record): AttributeTransformResult = recordTransform(r,this)
  override val metadata: Metadata = gm(this)
}

final case class ConstTransform(att:AttributeKey, cnt:Any) extends CustomAttributeTransform[ConstTransform](
  "constant",
  (_, m) => {
    AttributeTransformResult(
      Map(att -> cnt),
      m.meta("constant", att)
    )},
  implicit format => (
    {case jo:JObject =>
      val att = ( jo \ "attribute").extract[String]
      val cnt = ( jo \ "constant").extract[Any]
      ConstTransform(att, cnt)},
    {case cc:ConstTransform =>
      hint[ConstTransform] ~ ("attribute" -> cc.att) ~ ("constant" -> Extraction.decompose(cc.cnt))
    }),
  _ => Seq()
)

final case class GenerateUUID(att: AttributeKey) extends AttributeTransformation {
  override def transform(r: Record): AttributeTransformResult = AttributeTransformResult(
    Map(att -> UUID.randomUUID()),
    generateUUID(att))
}

final case class GenerateUUIDString(att: AttributeKey) extends AttributeTransformation {
  override def transform(r: Record): AttributeTransformResult = AttributeTransformResult(
    Map(att -> UUID.randomUUID().toString),
    generateUUIDString(att))
}
