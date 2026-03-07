package io.qpointz.mill.sql.dialect

class DialectValidationException(
    message: String,
    val errors: List<String> = emptyList(),
    cause: Throwable? = null
) : RuntimeException(message, cause)
