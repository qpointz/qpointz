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

package io.qpointz.flow.store.objects

import java.net.URI

trait ObjectStore {


}

trait ObjectKeyLike {
  val key:URI 
}

case class ObjectKey(key:URI)

case class LocalSource(key:URI)

case class UploadFiles(source:ObjectStore, target:ObjectStore, sourceFiles:List[URI])

case class Object(uri:URI, metadata:Map[String, Any]=Map()) {

}