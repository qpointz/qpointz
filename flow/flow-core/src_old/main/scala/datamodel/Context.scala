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

trait Context {

  //scalastyle:off regex
  def info(m:String):Unit = println(s"INFO: $m")
  def warn(m:String):Unit = println(s"WARN: $m")
  def debug(m:String):Unit = println(s"DEBUG: $m")
  def error(m:String):Unit = println(s"ERROR: $m")
  //scalastyle:on regex

}


trait ContextAware[T <: Context] {
  val ctx : T
}
