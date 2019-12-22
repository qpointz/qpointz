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

package io.qpointz.flow.data

trait Record
  extends ValuesVector
  with Iterable[Attribute]
{
  def get(key:AttributeKey):AttributeValue
}


object Record {
  def apply(attributeValue: Map[AttributeKey, AttributeValue], metadata:Metadata = Metadata.empty) : Record= {
    SeqRecord(attributeValue.toSeq, metadata)
  }
}


case class SeqRecord(private val record:Seq[Attribute], metadata:Metadata)
  extends Record
  with MetadataTarget {

  override def iterator: Iterator[Attribute] = record.iterator

  override def get(idx: AttributeIndex): AttributeValue = record(idx)._2

  override def get(key: AttributeKey): AttributeValue = record.indexWhere(_._1==key) match {
    case -1 => throw  new NoSuchElementException(s"Attribute '${key}' doesn't exists")
    case x => get(x)
  }

  override lazy val size : Int = record.size
}

case class MapRecord(private val record:Map[AttributeKey, AttributeValue], metadata:Metadata)
  extends Record
  with MetadataTarget {

  override def get(key: AttributeKey): AttributeValue = record(key)

  private lazy val keys = iterator.map(_._1).toSeq

  override def get(idx: AttributeIndex): AttributeValue = get(keys(idx))

  override def iterator: Iterator[(AttributeKey, AttributeValue)] = record.iterator
}