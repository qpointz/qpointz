package io.qpointz.mill.metadata.domain

/** Exception raised by facet implementations when validation fails. */
class ValidationException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
