package io.qpointz.flow.data

trait Vector
  extends Iterable[AttributeValue]
{
  def get(idx:AttributeIndex):AttributeValue
  def size:Int
}

case class SeqVector(values: Seq[AttributeValue], metadata:Metadata)
  extends Vector
  with MetadataTarget {
  override def get(idx: AttributeIndex): AttributeValue = values(idx)
  override def iterator: Iterator[AttributeValue] = values.iterator
}

object Vector {

  def apply(values:Seq[AttributeValue], metadata:Metadata = Metadata.empty):Vector = {
    SeqVector(values, metadata)
  }

}