package com.kemp

import com.kemp.client.KubeClient
import com.kemp.model.Command
import com.kemp.model.Response
import com.kemp.utils.asStringJsonList
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun main() {

    val kubeClient = KubeClient()

    val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
            maxFrameSize = Long.MAX_VALUE
            //pingInterval = 5.toDuration(DurationUnit.SECONDS)
        }
    }

    val commandHandlers: Map<String, suspend (DefaultClientWebSocketSession, String, KubeClient) -> Unit> = mapOf(
        "request" to ::request
    )

    runBlocking {
        client.webSocket("ws://localhost:8080/connect") {
            //while (true) {
            incoming.consumeEach {
                if (it !is Frame.Text) {
                    val command = receiveDeserialized<Command>()
                    val handler = commandHandlers[command.type]
                    println(handler)
                    if (handler != null) {
                        handler(this, command.details, kubeClient)
                    } else {
                        println("No handler found for command type: ${command.type}")
                    }
                } else {
                    val request = it.readText()
                    request(this, request, kubeClient)
                }
            }
            //}
        }
    }
}

suspend fun request(session: DefaultClientWebSocketSession, details: String, kubeClient: KubeClient) {
    println("Handle request of details: $details")
    val response = Response("pong", "Pong Response")
    //session.sendSerialized(response)
    session.send(kubeClient.listObjects(details).asStringJsonList())
}