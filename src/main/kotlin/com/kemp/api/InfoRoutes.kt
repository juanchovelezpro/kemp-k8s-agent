package com.kemp.api

import com.google.gson.Gson
import com.kemp.client.KubeClient
import com.kemp.client.KubeClient.toJson
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.info() {
    get("/") {
        call.respond("Kubernetes API developed in Kotlin using the Official Kubernetes Java Client")
    }
    get("/serverInfo") {
        call.respond(KubeClient.getServerVersion().toJson())
    }
    get("/apiresources"){
        call.respond(KubeClient.getServerResources().toJson())
    }
}