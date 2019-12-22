package io.qpointz.flow.data

trait ValuesVector
  extends Iterable[AttributeValue]
{
  def get(idx:AttributeIndex):AttributeValue
  def size:Int
}

case class SeqValuesVector(values: Seq[AttributeValue], metadata:Metadata)
  extends ValuesVector
  with MetadataTarget {
  override def get(idx: AttributeIndex): AttributeValue = values(idx)
  override def iterator: Iterator[AttributeValue] = values.iterator
}

object ValuesVector {

  def apply(values:Seq[AttributeValue], metadata:Metadata = Metadata.empty):ValuesVector = {
    SeqValuesVector(values, metadata)
  }

}