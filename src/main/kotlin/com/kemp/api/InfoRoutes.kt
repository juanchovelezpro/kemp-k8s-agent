package com.kemp.api

import com.kemp.client.KubeManager
import com.kemp.utils.toJson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.info() {
    get("/") {
        call.respond("Kubernetes API developed in Kotlin using the Official Kubernetes Java Client")
    }
    route("/api/cluster/{cluster}") {
        get("/version") {
            val version = call.parameters["cluster"]?.let {
                KubeManager.getClient(it).getServerVersion().toJson()
            }
            call.respondText(version ?: "", ContentType.Application.Json)
        }
        get("/api-resources") {
            val serverResources = call.parameters["cluster"]?.let {
                KubeManager.getClient(it).getServerResources().toJson()
            }
            call.respondText(serverResources ?: "", ContentType.Application.Json)
        }
    }

}