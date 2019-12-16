package io.qpointz.flow.data


object Metadata {
  val empty: Metadata = List()
}

trait MetadataTarget {
  val metadata : Metadata
}
