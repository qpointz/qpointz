package io.qpointz.mill.ai.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

/**
 * Gates AI v3 Spring components when `mill.ai.enabled` is false.
 *
 * Shared by `mill-ai-v3-autoconfigure` ([org.springframework.boot.autoconfigure.AutoConfiguration]
 * classes) and the HTTP surface in this module ([io.qpointz.mill.ai.service.AiChatController], etc.).
 *
 * Absent property defaults to enabled (`matchIfMissing = true`) so existing applications
 * behave unchanged.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@ConditionalOnProperty(
    prefix = "mill.ai",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
annotation class ConditionalOnAiEnabled
