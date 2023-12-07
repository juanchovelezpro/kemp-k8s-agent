package com.kemp

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

fun main() {
    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 20_000
        }
    }
    runBlocking {
        //val kempClient = KempClient(client)
        val session = client.webSocketSession {
            url("ws://127.0.0.1:8080/link")
            headers["name"] = "kemp-agent"
            headers.build()
        }

        session.incoming.receiveAsFlow().onEach{ frame ->
            if(frame is Frame.Text){
                val data = frame.readText()
                println(data)
            }
        }.launchIn(this)

        var times = 0

        launch(Dispatchers.IO) {
            while(isActive){
                session.outgoing.send(Frame.Text("Testing $times"))
                delay(1000L)
                times++
            }
        }
    }
}


