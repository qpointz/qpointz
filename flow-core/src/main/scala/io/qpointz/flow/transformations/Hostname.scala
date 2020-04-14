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

import java.net.InetAddress

import io.qpointz.flow.MetadataMethods._
import io.qpointz.flow.transformations.TransformationsMeta._
import io.qpointz.flow.{AttributeKey, Record}


final case class LocalhostNameShort(att:AttributeKey) extends AttributeTransformation {
  private lazy val name =  InetAddress.getLocalHost.getHostName

  override def transform(r: Record): AttributeTransformResult = AttributeTransformResult(
    Map(att -> name),
    hostnameShort(att)
  )
}

final case class LocalhostNameCanonical(att:AttributeKey) extends AttributeTransformation {
  private lazy val name =  InetAddress.getLocalHost.getCanonicalHostName

  override def transform(r: Record): AttributeTransformResult = AttributeTransformResult(
    Map(att -> name),
    hostnameCanonical(att)
  )
}