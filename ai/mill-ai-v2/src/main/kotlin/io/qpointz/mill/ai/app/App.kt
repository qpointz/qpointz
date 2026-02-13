package io.qpointz.mill.ai.app

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class App

@Bean
fun runOnStartup(chatService: ChatService) = ApplicationRunner {
    chatService.tellStory("Are you penguin?")
}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
