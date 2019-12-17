package io.qpointz.flow

package object excel {

  class FlowExcelException(private val message: String = "",
                      private val cause: Throwable = None.orNull
                     ) extends FlowException(message, cause)

}
