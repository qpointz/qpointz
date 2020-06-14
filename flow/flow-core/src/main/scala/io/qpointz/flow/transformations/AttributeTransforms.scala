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

/*
package io.qpointz.flow.transform

import io.qpointz.flow.{AttributeKey, AttributeValue, Metadata, MetadataItem, MetadataItemOps, MetadataKey, MetadataOps, MetadataValue, Record}

object AttributeTransformsMetadata extends MetadataOps("transform:attribute") {
    implicit class AttributeTransformMetaOps(val m:Metadata) {
        def dropAttributeMeta: MetadataItemOps[Set[AttributeKey]] = item[Set[AttributeKey]](m, "drop:attributes")
        def retainAttributeMeta: MetadataItemOps[Set[AttributeKey]] = item[Set[AttributeKey]](m, "retain:attributes")
    }
}

object AttributeTransformsMethods {

  import AttributeTransformsMetadata._

  def attributeOperation(r:Record, enrichMeta:Boolean)(applyMeta:Metadata=>Metadata, applyRecord:(Record,Metadata)=>Record):Record = {
    val meta = if (enrichMeta) {
      applyMeta(r.metadata)
    } else {
      r.metadata
    }
    applyRecord(r, meta)
  }

  private def retainMeta(m:Metadata):Metadata = m

  def dropAttribute(r:Record, attribute:AttributeKey, updateMeta:Boolean=true):Record = dropAttributes(r, Set(attribute), updateMeta)

  def dropAttributes(r:Record, attributes:Set[AttributeKey], updateMeta:Boolean=true):Record = attributeOperation(r, updateMeta)(
      {m=> m.dropAttributeMeta(attributes) },
      {(r,m) => Record(
        r.filter(a=> !attributes.contains(a._1)).toMap,
        m
      )})

  def retainAttributes(r:Record, attributes:Set[AttributeKey], updateMeta:Boolean=true):Record = attributeOperation(r, updateMeta)(
    {m=> m.retainAttributeMeta(attributes) },
    {(r,m) => Record(
      r.filter(a=> attributes.contains(a._1)).toMap,
      m
    )})

  type ValueItem = (()=>AttributeValue, Seq[(MetadataKey,MetadataValue)])

  def upsertValues(r:Record, values:Map[AttributeKey, ValueItem], updateMeta:Boolean=true):Record= attributeOperation(r, updateMeta)(
    {m => m ++ values.values
         .flatten(_._2)
         .map(x=> (AttributeTransformsMetadata.groupKey, x._1, x._2))
    },{
     (r,nm) => Record(
        values.foldLeft(r.toMap)((m,v)=> {m + (v._1 -> v._2._1())}),
        nm)
    })

  def updateValues(r:Record, values:Map[AttributeKey, ValueItem], updateMeta:Boolean=true):Record= attributeOperation(r, updateMeta)(
    {m => m ++ values
        .filter(x=> r.contains(x._1))
        .flatten(_._2._2)
        .map(x=> (AttributeTransformsMetadata.groupKey, x._1, x._2))
    }, {
      (r, nm) => Record(r.map{
         case (k,_) if values.contains(k) => (k,values(k)._1())
         case (k,v) => (k,v)
        }, nm)
    }
  )

  def appendValues(r:Record, values:Map[AttributeKey, ValueItem], updateMeta:Boolean=true):Record= attributeOperation(r, updateMeta)(
    {m => m ++ values
      .filter(x=> !r.contains(x._1))
      .flatten(_._2._2)
      .map(x=> (AttributeTransformsMetadata.groupKey, x._1, x._2))
    }, {
      (r, nm) => Record(
        r ++ values.filter(k=> !r.contains(k._1)).map(x=> (x._1,x._2._1())),
        nm)
      }
  )

}


*/
