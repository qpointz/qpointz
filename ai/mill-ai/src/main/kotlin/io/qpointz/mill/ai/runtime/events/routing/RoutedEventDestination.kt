package io.qpointz.mill.ai.runtime.events.routing

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

/** Explicit downstream destination for a routed agent event. */
enum class RoutedEventDestination {
    /** Live streaming events shown during execution. */
    CHAT_STREAM,
    /** Canonical user/assistant turns persisted as durable transcript. */
    CHAT_TRANSCRIPT,
    /** Model-facing memory fed into the LLM context window. */
    MODEL_MEMORY,
    /** Machine-readable structured outputs from protocol execution. */
    ARTIFACT,
    /** Diagnostics, token stats, and audit-oriented signals. */
    TELEMETRY,
}





