/*
 * Copyright 2021 qpointz.io
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
 *  limitations under the License.
 */

package io.qpointz.flow

import com.orientechnologies.orient.core.record.impl.ODocument
import io.qpointz.flow.orientdb.implicits.{JValueImplicits, ODocumentImplicits}
import org.json4s.JsonAST._

package object orientdb {
  import scala.language.implicitConversions

  nio.FileStreamSource
  implicit def jValueToOdocument(jn:JValue):ODocument = jn.asODocument
  implicit def oDOcToJValue(dov:ODocument):JValue = dov.asJValue
  implicit def jvalueimplicits(jv: JValue):JValueImplicits = new JValueImplicits(jv)

  implicit def odocumentImplicits(doc:ODocument):ODocumentImplicits= new ODocumentImplicits(doc)

}
