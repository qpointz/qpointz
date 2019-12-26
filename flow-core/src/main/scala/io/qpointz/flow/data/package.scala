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
  type Metadata         = Seq[MetadataItem]

  object AttributeValue {
    object Null  {}
    object Error {}
    object Empty {}
    object Missing {}
  }

  object RecordTags {
    val OK:String  = "OK"
    val NOK:String = "NOK"
    val MissingValue:String = "Missing value"
    val UnexpectedValue:String = "Unexpected value"

  }

  implicit class RecordOps(r:Record) {

    def metadata():Metadata = r match {
      case mt:MetadataTarget => mt.metadata
      case _ => Seq()
    }

  }

}
