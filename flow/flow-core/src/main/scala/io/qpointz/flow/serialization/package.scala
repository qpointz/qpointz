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
 *  limitations under the License
 */

package io.qpointz.flow

import org.json4s.Serializer

package object serialization {

  sealed case class JsonFormat[T](group:String, hint:String, serailizer:Option[Serializer[T]])(implicit val m:Manifest[T])

  object JsonFormat {

    def apply[T](group:String, hint:String)(implicit m:Manifest[T]):JsonFormat[T] = JsonFormat[T](group, hint, None)

  }

  trait JsonFormatExtension {
    def hintNamespace: String
    def protocols:Iterable[JsonFormat[_]]
  }

}
