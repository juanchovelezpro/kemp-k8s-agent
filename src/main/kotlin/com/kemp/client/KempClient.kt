package com.kemp.client

import com.kemp.model.Request
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KempClient(
    private val client: HttpClient
) : RealtimeMessagingClient {

    private var session: WebSocketSession? = null
    override suspend fun consume() {

        session = client.webSocketSession {
            url("")
        }
        val requests = session!!
            .incoming
            .consumeAsFlow()
            .filterIsInstance<Frame.Text>()
            .mapNotNull { Json.decodeFromString<Request>(it.readText()) }


    }


    override suspend fun send(data: Any) {
        session?.outgoing?.send(
            Frame.Text(Json.encodeToString(data))
        )
    }

    override suspend fun close() {
        session?.close()
        session = null
    }
}