package io.qpointz.mill.metadata.domain

/** Outcome of facet or entity validation with optional error messages. */
data class ValidationResult(val valid: Boolean, val errors: List<String>) {
    companion object {
        @JvmStatic fun ok(): ValidationResult = ValidationResult(true, emptyList())
        @JvmStatic fun fail(error: String): ValidationResult = ValidationResult(false, listOf(error))
        @JvmStatic fun fail(errors: List<String>): ValidationResult = ValidationResult(false, errors)
        @JvmStatic fun merge(results: List<ValidationResult>): ValidationResult {
            val errors = results.filter { !it.valid }.flatMap { it.errors }
            return if (errors.isEmpty()) ok() else fail(errors)
        }
    }
}
