case class MetadataKey(group:String, key:String) {}

trait MedataItem {
  val key:MetadataKey
  val value:Any
}

class MatadataItemDef[T](val key:MetadataKey) {

}

case class MetadataItem[T](key:MetadataKey, value:T) extends MedataItem

implicit class MetadataKeyOp(mk:MetadataKey) {
  def ~>[T](v:T):MetadataItem[T] = {
    MetadataItem(mk, v)
  }
}




val k = MetadataKey("rook", "fook")

(k ~> 12).value

