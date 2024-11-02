package com.kemp

import com.kemp.model.Command
import com.kemp.model.Response
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main() {
    val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
            maxFrameSize = Long.MAX_VALUE
            //pingInterval = 5.toDuration(DurationUnit.SECONDS)
        }
    }

    val commandHandlers: Map<String, suspend (DefaultClientWebSocketSession, String) -> Unit> = mapOf(
        "ping" to ::ping
    )

    runBlocking {
        client.webSocket("ws://localhost:8080/connect") {
            while (true) {
                incoming.consumeEach {
                    val command = receiveDeserialized<Command>()
                    val handler = commandHandlers[command.type]
                    println(handler)
                    if (handler != null) {
                        handler(this, command.details)
                    } else {
                        println("No handler found for command type: ${command.type}")
                    }
                }
            }
        }
    }
}

suspend fun ping(session: DefaultClientWebSocketSession, details: String) {
    println("Handle Ping with details: $details")
    val response = Response("pong", "Pong Response")
    session.sendSerialized(response)
}