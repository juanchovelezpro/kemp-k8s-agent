package com.kemp.api

import com.kemp.client.KubeClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.listResources() {

    get("/{resource}") {
        val resources = call.parameters["resource"]?.let { KubeClient.listResourcesAsStringJsonList(it) }
        call.respondNullable(resources)
    }

}