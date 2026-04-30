package io.qpointz.mill.ai.cli

import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private val mapper = jacksonObjectMapper()

/**
 * Entry point for the HTTP-only Mill AI v3 developer test bench (no in-process LangChain4j agent).
 */
fun main(args: Array<String>) {
    val cmd = CommandLine(RootCommand())
    cmd.executionStrategy = CommandLine.RunLast()
    val exit = cmd.execute(*args)
    kotlin.system.exitProcess(exit)
}

@Command(
    name = "mill-ai-v3-cli",
    description = ["HTTP test bench for mill-ai-v3-service (REST + SSE). No embedded agent."],
    mixinStandardHelpOptions = true,
)
private class RootCommand : Runnable {

    @Option(
        names = ["--base-url"],
        description = ["Base URL of mill-ai-v3-service (default: \${DEFAULT-VALUE})"],
        defaultValue = "http://localhost:8080",
    )
    lateinit var baseUrl: String

    @Option(
        names = ["--profile-id"],
        description = ["Agent profile id for new chats (default: env MILL_AI_PROFILE or hello-world)"],
    )
    var profileId: String? = null

    @Option(
        names = ["--list-profiles"],
        description = ["Fetch GET /api/v1/ai/profiles and print JSON, then exit"],
    )
    var listProfiles: Boolean = false

    @Option(names = ["--user-id"], description = ["Reserved for future authenticated requests"])
    var userId: String? = null

    @Option(names = ["--password"], description = ["Reserved for future authenticated requests"])
    var password: String? = null

    @Option(
        names = ["--verbose-sse"],
        description = ["Log each SSE line and JSON event type (helps verify text/event-stream handling)"],
    )
    var verboseSse: Boolean = false

    private val httpCustomizer: HttpRequestCustomizer =
        NoOpHttpRequestCustomizer

    override fun run() {
        val root = baseUrl.trimEnd('/')
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build()

        if (listProfiles) {
            listProfiles(client, root)
            return
        }

        val pid = profileId
            ?: System.getenv("MILL_AI_PROFILE")
            ?: "hello-world"

        // ASCII-only banner so Windows consoles using legacy code pages do not mangle em dash / unicode
        println("Mill AI v3 - HTTP test bench (SSE client; no in-process agent)")
        println("  baseUrl   : $root")
        println("  profileId : $pid")
        println("  (Type /exit to quit, /profiles to list profiles; add --verbose-sse to trace SSE events)")
        println()

        val chatId = createChat(client, root, pid)
        println("Chat: $chatId")
        println()

        val input = System.`in`.bufferedReader()
        while (true) {
            print("you > ")
            System.out.flush()
            val line = input.readLine() ?: break
            val text = line.trim()
            when {
                text.isEmpty() -> continue
                text == "/exit" || text == "exit" -> break
                text == "/profiles" -> {
                    listProfiles(client, root)
                    println()
                    continue
                }
            }
            sendMessageStream(client, root, chatId, text, httpCustomizer, verboseSse)
            println()
        }
        println("Bye.")
    }

    private fun listProfiles(client: HttpClient, root: String) {
        val uri = URI.create("$root/api/v1/ai/profiles")
        val req = httpCustomizer.customize(
            HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofSeconds(60))
                .header("Accept", "application/json"),
        ).build()
        val body = client.send(req, HttpResponse.BodyHandlers.ofString()).body()
        println(prettyJson(body))
    }

    private fun createChat(client: HttpClient, root: String, profileId: String): String {
        val uri = URI.create("$root/api/v1/ai/chats")
        val payload = mapper.writeValueAsString(mapOf("profileId" to profileId))
        val req = httpCustomizer.customize(
            HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json"),
        ).build()
        val res = client.send(req, HttpResponse.BodyHandlers.ofString())
        if (res.statusCode() !in 200..299) {
            error("Create chat failed: HTTP ${res.statusCode()} — ${res.body()}")
        }
        val tree = mapper.readTree(res.body())
        return tree["chatId"].asText()
    }

    private fun sendMessageStream(
        client: HttpClient,
        root: String,
        chatId: String,
        message: String,
        customizer: HttpRequestCustomizer,
        verboseSse: Boolean,
    ) {
        val uri = URI.create("$root/api/v1/ai/chats/$chatId/messages")
        val payload = mapper.writeValueAsString(mapOf("message" to message))
        val req = customizer.customize(
            HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream"),
        ).build()
        val response = client.send(req, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() == 404) {
            val err = response.body().readAllBytes().toString(Charsets.UTF_8)
            println("HTTP 404 - $err")
            return
        }
        if (response.statusCode() !in 200..299) {
            val err = response.body().readAllBytes().toString(Charsets.UTF_8)
            error("Send message failed: HTTP ${response.statusCode()} — $err")
        }
        response.body().bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (!line.startsWith("data:")) {
                    if (verboseSse && line.isNotEmpty()) {
                        println("[sse] $line")
                    }
                    return@forEach
                }
                val jsonText = line.removePrefix("data:").trim()
                if (jsonText.isEmpty()) return@forEach
                val node: JsonNode = try {
                    mapper.readTree(jsonText)
                } catch (_: Exception) {
                    return@forEach
                }
                val eventType = node.path("type").asText("")
                if (verboseSse && eventType.isNotEmpty()) {
                    println("[sse] type=$eventType")
                }
                when (eventType) {
                    "item.diagnostic" -> {
                        val code = node.path("code").asText("?")
                        val msg = node.path("message").asText("")
                        println("[diag] $code — $msg")
                    }
                    "item.tool.call" -> {
                        val name = node.path("toolName").asText("?")
                        val args = node.path("arguments").toString()
                        println("[tool] $name $args")
                    }
                    "item.tool.result" -> {
                        val name = node.path("toolName").asText("?")
                        val res = node.path("result").toString()
                        println("[tool result] $name -> $res")
                    }
                    "item.part.updated" -> {
                        val partType = node.path("partType").asText("text")
                        if (partType != "text") {
                            if (verboseSse) {
                                println("[sse] partType=$partType raw: ${jsonText.take(300)}")
                            }
                            return@forEach
                        }
                        val c = node.path("content").asText("")
                        print(c)
                        System.out.flush()
                    }
                    "item.completed" -> {
                        val content = node.get("content")?.asText()
                        if (!content.isNullOrEmpty()) {
                            println(content)
                        } else {
                            println()
                        }
                    }
                    "item.failed" -> {
                        println()
                        println("[error] ${node.path("code").asText()} - ${node.path("reason").asText()}")
                    }
                    else -> {
                        if (verboseSse && eventType.isNotEmpty()) {
                            println("[sse] (unhandled type; use --verbose-sse) raw: ${jsonText.take(200)}")
                        }
                    }
                }
            }
        }
    }
}

private fun prettyJson(raw: String): String =
    runCatching {
        val tree: JsonNode = mapper.readTree(raw)
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree)
    }.getOrDefault(raw)
