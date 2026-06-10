package io.qpointz.mill.ai.data.chroma.it

/**
 * Gates [ChromaSkymillDistinctVectorIT] so CI stays green without local Postgres/Chroma/OpenAI.
 *
 * Postgres, embedding model, and Chroma endpoint are defined in configuration (YAML). OpenAI credentials:
 * use a user-profile `OPENAI_API_KEY` (YAML uses `${OPENAI_API_KEY}` without an empty default so nothing is
 * overridden with a blank), or `MILL_AI_PROVIDERS_OPENAI_API_KEY` if `api-key` is omitted from YAML.
 *
 * - `MILL_CHROMA_IT_ENABLED=true`
 * - non-blank `OPENAI_API_KEY` or `MILL_AI_PROVIDERS_OPENAI_API_KEY`
 */
object ChromaSkymillVectorItConditions {

    @JvmStatic
    fun enabled(): Boolean {
//        if (System.getenv("MILL_CHROMA_IT_ENABLED") != "true") {
//            return false
//        }
        val openAi = System.getenv("OPENAI_API_KEY")
        val millAi = System.getenv("MILL_AI_PROVIDERS_OPENAI_API_KEY")
        if (openAi.isNullOrBlank() && millAi.isNullOrBlank()) {
            return false
        }
        return true
    }
}
