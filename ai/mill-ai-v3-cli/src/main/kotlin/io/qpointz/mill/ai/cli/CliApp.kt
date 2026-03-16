package io.qpointz.mill.ai.cli

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.qpointz.mill.ai.AgentEvent
import io.qpointz.mill.ai.capabilities.HelloWorldAgentProfile
import io.qpointz.mill.ai.capabilities.sqlquery.MockSqlExecutionService
import io.qpointz.mill.ai.capabilities.sqlquery.MockSqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.langchain4j.LangChain4jAgent
import io.qpointz.mill.ai.langchain4j.SchemaExplorationAgent
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import java.io.BufferedReader
import java.io.InputStreamReader
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

// ANSI colour helpers
private const val RESET   = "\u001B[0m"
private const val BOLD    = "\u001B[1m"
private const val DIM     = "\u001B[2m"
private const val ITALIC  = "\u001B[3m"
private const val CYAN    = "\u001B[36m"
private const val YELLOW  = "\u001B[33m"
private const val GREEN   = "\u001B[32m"
private const val RED     = "\u001B[31m"
private const val MAGENTA = "\u001B[35m"
private const val BLUE    = "\u001B[34m"

private fun bold(s: String)  = "$BOLD$s$RESET"
private fun dim(s: String)   = "$DIM$s$RESET"
private fun cyan(s: String)  = "$CYAN$s$RESET"
private fun yellow(s: String)= "$YELLOW$s$RESET"
private fun green(s: String) = "$GREEN$s$RESET"
private fun red(s: String)   = "$RED$s$RESET"

// Single shared mapper — all events serialised the same way
private val mapper = jacksonObjectMapper()

private fun protocolLabel(protocolId: String): String = protocolId.substringAfterLast('.')

private fun compactJson(raw: String): String =
    runCatching { mapper.writeValueAsString(mapper.readTree(raw)) }
        .getOrDefault(raw)

private fun elapsedSec(turnStart: Long): String {
    val secs = (System.currentTimeMillis() - turnStart) / 1000L
    return dim("+${secs}s")
}

/**
 * Generic event printer.
 *
 * Strips the `type` field (shown as label) and prints the remaining payload as
 * compact JSON.  Works for any current or future AgentEvent subtype without
 * additional when-branches.
 */
private fun printEvent(event: AgentEvent, turnStart: Long) {
    val node   = mapper.valueToTree<ObjectNode>(event)
    val type   = node.remove("type").asText()
    val payload = mapper.writeValueAsString(node)   // compact single-line JSON

    val typeColour = when {
        type.startsWith("tool.")      -> MAGENTA
        type.startsWith("protocol.")  -> GREEN
        type.startsWith("reasoning")  -> BLUE
        type.startsWith("answer.")    -> GREEN
        else                          -> CYAN
    }

    println("  ${elapsedSec(turnStart)} $DIM[$RESET$typeColour$type$RESET$DIM]$RESET  $DIM$payload$RESET")
}

@Command(
    name = "mill-ai-v3-cli",
    description = ["Mill AI v3 interactive CLI for manual agent testing"],
    mixinStandardHelpOptions = true
)
private class CliOptions {

    // Positional agent keeps startup friction low for manual testing.
    @Parameters(
        index = "0",
        arity = "0..1",
        description = ["Agent to run. Supported: \${COMPLETION-CANDIDATES}"],
        paramLabel = "agent"
    )
    var agent: String? = null

    @Option(
        names = ["-a", "--agent"],
        description = ["Agent to run. Overrides positional agent if both are provided."]
    )
    var agentOption: String? = null

    fun resolvedAgent(): String = agentOption ?: agent ?: System.getenv("AGENT") ?: "hello"

    fun supportedAgents(): List<String> = listOf("hello", "schema")
}

// ── Main ─────────────────────────────────────────────────────────────────────

fun main(args: Array<String>) {
    val options = CliOptions()
    val commandLine = picocli.CommandLine(options)
    commandLine.registerConverter(String::class.java) { it.trim() }
    commandLine.setCaseInsensitiveEnumValuesAllowed(true)

    val parseResult = commandLine.parseArgs(*args)
    if (picocli.CommandLine.printHelpIfRequested(parseResult)) {
        return
    }

    println()
    println(bold("Mill AI v3 — Interactive CLI"))
    println(dim("Type your message and press Enter. Commands: /help  /exit"))
    println()

    val agentName = options.resolvedAgent()
    if (agentName !in options.supportedAgents()) {
        println(red("Error: unknown agent '$agentName'. Supported: ${options.supportedAgents().joinToString(", ")}"))
        println(dim("  You can pass the agent as a positional argument, --agent, or AGENT env var."))
        return
    }
    val model = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini"

    val runTurnFn: (String, (AgentEvent) -> Unit) -> Unit = when (agentName) {
        "schema" -> {
            val schemaService = SchemaFacetServiceFactory.create()
            val dialectSpec = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")
            val sqlQueryDep = SqlQueryCapabilityDependency(
                validator = MockSqlValidationService(),
                executor = MockSqlExecutionService(),
            )
            val agent = SchemaExplorationAgent.fromEnv(schemaService, dialectSpec, sqlQueryDep)
            if (agent == null) {
                println(red("Error: OPENAI_API_KEY environment variable is not set."))
                println(dim("  Optional: OPENAI_MODEL (default: gpt-4o-mini), OPENAI_BASE_URL, SCHEMA_SOURCE (default: demo)"))
                return
            }
            println(dim("  model  : $model"))
            println(dim("  agent  : schema-exploration"))
            println(dim("  schema : ${System.getenv("SCHEMA_SOURCE") ?: "demo"}"))
            println()
            val fn1: (String, (AgentEvent) -> Unit) -> Unit = { input, listener -> agent.run(input, listener) }
            fn1
        }
        else -> {
            val agent = LangChain4jAgent.fromEnv(HelloWorldAgentProfile.profile)
            if (agent == null) {
                println(red("Error: OPENAI_API_KEY environment variable is not set."))
                println(dim("  Optional: OPENAI_MODEL (default: gpt-4o-mini), OPENAI_BASE_URL"))
                return
            }
            println(dim("  model : $model"))
            println(dim("  agent : hello-world"))
            println()
            val fn2: (String, (AgentEvent) -> Unit) -> Unit = { input, listener -> agent.run(input, listener) }
            fn2
        }
    }

    val reader = BufferedReader(InputStreamReader(System.`in`))

    while (true) {
        print(cyan("you") + " > ")
        System.out.flush()

        val line  = reader.readLine() ?: break
        val input = line.trim()

        when {
            input.isBlank() -> continue
            input == "/exit" || input == "/quit" || input == "exit" || input == "quit" -> {
                println(dim("Bye.")); break
            }
            input == "/help" -> { printHelp(); continue }
            input.startsWith("/") -> {
                println(yellow("Unknown command: $input  (try /help)")); continue
            }
        }

        println()
        runTurn(runTurnFn, input)
        println()
    }
}

// ── Turn ─────────────────────────────────────────────────────────────────────

private fun runTurn(agentFn: (String, (AgentEvent) -> Unit) -> Unit, input: String) {
    val turnStart   = System.currentTimeMillis()
    var inMessage   = false
    var inReasoning = false

    fun endMessage() {
        if (inMessage) { println(); inMessage = false }
    }
    fun endReasoning() {
        if (inReasoning) {
            print(RESET); println(); println(dim("  └─ end of reasoning"))
            inReasoning = false
        }
    }
    fun endBlocks() { endMessage(); endReasoning() }

    agentFn(input) { event ->
        when (event) {
            // ── streaming blocks — kept inline for UX ────────────────────────
            is AgentEvent.MessageDelta -> {
                endReasoning()
                if (!inMessage) { print("  ${elapsedSec(turnStart)} " + green(bold("agent")) + " > "); inMessage = true }
                print(event.text); System.out.flush()
            }
            is AgentEvent.ProtocolTextDelta -> {
                endReasoning()
                if (!inMessage) {
                    print("  ${elapsedSec(turnStart)} " + green(bold(protocolLabel(event.protocolId))) + " > ")
                    inMessage = true
                }
                print(event.text); System.out.flush()
            }
            is AgentEvent.ReasoningDelta -> {
                endMessage()
                if (!inReasoning) {
                    println(dim("  ┌─ reasoning"))
                    print("  $BLUE│$RESET $DIM$ITALIC")
                    inReasoning = true
                }
                print(event.text.replace("\n", "\n  $BLUE│$RESET $DIM$ITALIC"))
                System.out.flush()
            }
            is AgentEvent.ProtocolFinal -> {
                endBlocks()
                println("  ${elapsedSec(turnStart)} ${green("[protocol.final]")}  ${bold(event.protocolId)}")
                println("  ${dim(compactJson(event.payload))}")
            }
            is AgentEvent.ProtocolStreamEvent -> {
                endBlocks()
                println(
                    "  ${elapsedSec(turnStart)} ${green("[protocol.stream]")}  ${bold(event.protocolId)}" +
                        " ${dim(event.eventType)}"
                )
                println("  ${dim(compactJson(event.payload))}")
            }
            // ── every other event — generic JSON ─────────────────────────────
            else -> { endBlocks(); printEvent(event, turnStart) }
        }
    }
}

// ── Help ─────────────────────────────────────────────────────────────────────

private fun printHelp() {
    println()
    println(bold("Commands:"))
    println("  /help   — show this message")
    println("  /exit   — quit  (also: exit, quit, /quit)")
    println()
    println(bold("Agent selection:"))
    println("  mill-ai-v3-cli hello       — run hello-world demo agent")
    println("  mill-ai-v3-cli schema      — run schema exploration agent")
    println("  mill-ai-v3-cli --agent schema")
    println("  AGENT=schema mill-ai-v3-cli")
    println()
    println(bold("Environment:"))
    println("  AGENT=hello  (default fallback) — hello-world demo agent")
    println("  AGENT=schema                    — schema exploration agent")
    println("  SCHEMA_SOURCE=demo     — in-memory demo retail schema (default)")
    println()
    println(bold("Hello-world hints:"))
    println("  • \"say hello to Alice\"")
    println("  • \"echo back: hello world\"")
    println("  • \"what can you do?\"")
    println("  • \"run a noop\"")
    println()
    println(bold("Schema hints:"))
    println("  • \"what schemas are available?\"")
    println("  • \"list the tables in retail\"")
    println("  • \"what columns does the orders table have?\"")
    println("  • \"how are orders and customers related?\"")
    println()
}
