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
}