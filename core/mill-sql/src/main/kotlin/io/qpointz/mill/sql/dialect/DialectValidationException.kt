package io.qpointz.mill.sql.v2.dialect

class DialectValidationException(
    message: String,
    val errors: List<String> = emptyList(),
    cause: Throwable? = null
) : RuntimeException(message, cause)
