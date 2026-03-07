package io.qpointz.mill.sql.v2.dialect

object DialectValidator {
    private val requiredFunctionCategories = setOf(
        "strings",
        "regex",
        "numerics",
        "math",
        "aggregates",
        "statistics",
        "window",
        "dates-times",
        "conditionals"
    )

    fun validate(spec: SqlDialectSpec, sourceName: String) {
        val errors = mutableListOf<String>()

        if (spec.id.isBlank()) errors += "id is required"
        if (spec.name.isBlank()) errors += "name is required"
        if (spec.featureFlags.isEmpty()) errors += "feature-flags section is required and must be non-empty"
        if (spec.operators.isEmpty()) errors += "operators section is required and must be non-empty"
        if (spec.functions.isEmpty()) {
            errors += "functions section is required and must be non-empty"
        } else {
            val missing = requiredFunctionCategories - spec.functions.keys
            missing.forEach { errors += "functions category missing: $it" }
        }
        if (spec.paging.styles.isEmpty()) {
            errors += "paging.styles must contain at least one style"
        }

        if (errors.isNotEmpty()) {
            throw DialectValidationException(
                message = "Dialect validation failed for $sourceName (${spec.id})",
                errors = errors
            )
        }
    }
}
