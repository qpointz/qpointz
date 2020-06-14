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

trait Record extends MetadataObject {

  override val metadataGroupKey: MetadataGroupKey = Record.metadataGroupKey

  def apply(key:String):Any
  def apply(idx:Integer):Any

  val attributes:Seq[String]
  val values:Seq[Any]

  def size:Int
}



case class SeqRecord(private val _values:Seq[(String, Any)], override val metadata:Metadata) extends Record {

  override def apply(idx: Integer): Any = values(idx)
  override def apply(key: String): Any = values(attributes.indexOf(key))
  override lazy val attributes: Seq[String] = _values.map(_._1)
  override lazy val values: Seq[Any] = _values.map(_._2)
  override lazy val size: Int = _values.size
}

case class KeysValuesRecord(override val attributes:Seq[String], override val values:Seq[Any], override val metadata:Metadata) extends Record {

  override def apply(idx: Integer): Any = values(idx)
  override def apply(key: String): Any = values(attributes.indexOf(key))
  override lazy val size:Int = attributes.size

}

object Record {

  val metadataGroupKey: MetadataGroupKey = "datamodel:record"

  def apply(values:Seq[(String, Any)]):Record = {
    Record(values, List())
  }

  def apply(values:Seq[(String, Any)], metadata:Metadata):Record = {
    SeqRecord(values,metadata)
  }

  def apply(keys:Seq[String], values:Seq[Any]):Record = {
    Record(keys, values, List())
  }

  def apply(keys:Seq[String], values:Seq[Any], metadata:Metadata):Record = {
    KeysValuesRecord(keys, values, metadata)
  }
}
