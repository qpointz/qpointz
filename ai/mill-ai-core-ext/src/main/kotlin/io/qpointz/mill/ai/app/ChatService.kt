package io.qpointz.mill.ai.app

import io.qpointz.mill.ai.streaming.Transformations.content
import io.qpointz.mill.ai.streaming.Transformations.json
import io.qpointz.mill.ai.streaming.Transformations.to
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
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
        println("begin story")
        val chatClient = ChatClient.builder(chatModel)
            .build()
        val a = chatClient
            .prompt(Prompt.builder().content(prmt).build())
            .user(topic)
            .stream()
            .content()
            .content()
            .json()
            .to<Event>()
            .subscribe { println(it?.content)}
    }

    @PostConstruct
    fun hallo() {
        tellStory("sTORY OF flower.10 paragraphs")
    }

}
