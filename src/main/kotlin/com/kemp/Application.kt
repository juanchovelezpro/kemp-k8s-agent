package com.kemp

import com.google.gson.Gson
import com.kemp.client.KubeClient
import com.kemp.utils.asFrameText
import com.kemp.utils.asStringJsonList
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import io.kubernetes.client.util.generic.options.ListOptions
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking

fun main() {
    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 20_000
        }
    }

    val kubeClient = KubeClient()
    val gson = Gson()

    runBlocking {
        //val kempClient = KempClient(client)
        val session = client.webSocketSession {
            url("ws://127.0.0.1:8080/link")
            headers["name"] = "kemp-agent"
            headers.build()
        }

        session.incoming.receiveAsFlow().onEach { frame ->
            if (frame is Frame.Text) {
                val data = frame.readText()
                try {
                    when (data) {
                        "version" -> session.outgoing.send(Frame.Text(gson.toJson(kubeClient.getServerVersion())))
                        "api-resources" -> session.outgoing.send(Frame.Text(gson.toJson(kubeClient.getServerResources())))
                        "list" -> session.outgoing.send(
                            kubeClient.listObjects(
                                "",
                                "v1",
                                "Pod",
                                "",
                                ListOptions()
                            ).asStringJsonList().asFrameText()
                        )
                        "delete" -> {

                        }
                        "get" -> {

                        }
                        "apply" -> {

                        }
                    }
                } catch (ex: Exception) {
                    session.outgoing.send(Frame.Text("An error has ocurred $ex"))
                }
            }
        }.launchIn(this)

        var times = 0

//        launch(Dispatchers.IO) {
//            while(isActive){
//                session.outgoing.send(Frame.Text("Testing $times"))
//                delay(1000L)
//                times++
//            }
//        }
    }
}


