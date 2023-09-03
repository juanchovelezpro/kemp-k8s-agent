package com.kemp.api

import com.kemp.client.KubeManager
import com.kemp.client.KubeManager.toJson
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.info() {
    get("/") {
        call.respond("Kubernetes API developed in Kotlin using the Official Kubernetes Java Client")
    }
    get("/api/{cluster}/version") {
        val version = call.parameters["cluster"]?.let {
            KubeManager.getClient(it).getServerVersion().toJson()
        }
        call.respondNullable(version)
    }
    get("/api/{cluster}/apiresources") {
        val serverResources = call.parameters["cluster"]?.let {
            KubeManager.getClient(it).getServerResources().toJson()
        }
        call.respondNullable(serverResources)
    }
}