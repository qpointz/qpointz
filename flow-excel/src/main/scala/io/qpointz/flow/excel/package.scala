package io.qpointz.flow

package object excel {

  case class SheetColumn(index:Int, header:String) {}

  type SheetColumnCollection = Seq[SheetColumn]

  implicit class SheetColumnCollectionOps(sc:SheetColumnCollection) {
    def byColIndexOp(idx:Int): Option[SheetColumn] = {
      sc.find(_.index==idx)
    }

    def colIndexMap(): Map[Int, SheetColumn] = { sc
      .map(k=>(k.index, k))
      .toMap
    }

  }

  class FlowExcelException(private val message: String = "",
                      private val cause: Throwable = None.orNull
                     ) extends FlowException(message, cause)
}
