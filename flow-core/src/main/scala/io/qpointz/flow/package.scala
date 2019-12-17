package io.qpointz

package object flow {

  class FlowException(private val message: String = "",
                      private val cause: Throwable = None.orNull
                      ) extends Exception(message, cause)

}
