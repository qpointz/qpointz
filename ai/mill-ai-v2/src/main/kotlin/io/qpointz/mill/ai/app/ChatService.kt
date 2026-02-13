package io.qpointz.mill.ai.app

import io.qpointz.mill.ai.ContextDescriptor
import io.qpointz.mill.ai.ChatConversation
import io.qpointz.mill.ai.streaming.Transformations.content
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ChatService (@Autowired val chatModel: ChatModel) {

    val prmt = """
    You are a storytelling assistant that streams story text in chunks.

    Your task is to write a fictional story of a user-specified length
    (e.g. number of paragraphs, sentences, or words).

    You MUST output the story as a stream of JSON Lines (JSONL).
    Each line must be a single, valid JSON object.

    Event protocol:
    - Emit exactly ONE "story:begin" event at the start.
    - Emit ONE OR MORE "story:content" events, each containing a chunk of story text.
    - Emit exactly ONE "story:end" event at the end.

    JSON format (strict):
    {
      "event": "<event-type>",
      "content": "<text chunk>"
    }

    Rules:
    - Output ONLY JSONL (one JSON object per line).
    - Do NOT use markdown.
    - use text delimiters \n \t in content such that delimiters understood by json parser and can be used to format text in console
    - each json chunks should be about same size to user getting live low latency response   
    - you can split same paragraph on multiple chunks
    - Do NOT include explanations or extra text.
    - Use double quotes only.
    - Do NOT add any fields other than "event" and "content".
    - The story text must appear ONLY inside "story:content" events.
    - "story:begin" and "story:end" may contain short meta or framing text, or be empty strings.
    

    Streaming rules:
    - Do not repeat text between chunks.
    - Preserve correct story order.

    The stream must end with "story:end".
""".trimIndent()


    data class Event(val event: String, val content: String?)

    fun tellStory(topic: String?) {

        val c = ContextDescriptor
            .fromResource("ai/capabilities/talk/talk.yml")
            .createCapability();

        val conv = ChatConversation(listOf(c),
            ChatOptions.builder().build())
        println("begin story")

        val chatClient = conv
            .chatClient(chatModel)

        val a = chatClient
            .prompt(conv.capabilitiesSystem())
            .user(topic)
            .stream()
            .content()
            .content()
            .subscribe { println(it)}
    }

    @PostConstruct
    fun hallo() {
        tellStory("Hello my friend")
    }

}
