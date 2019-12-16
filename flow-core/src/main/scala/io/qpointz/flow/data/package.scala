package io.qpointz.flow

package object data {

  type AttributeIndex   = Int
  type AttributeValue   = Any
  type AttributeKey     = String
  type Attribute        = (AttributeKey, AttributeValue)

  type MetadataGroupKey = String
  type MetadataKey      = String
  type MetadataValue    = Any
  type MetadataItem     = (MetadataGroupKey, MetadataKey, MetadataValue)
  type Metadata         = List[MetadataItem]

}
