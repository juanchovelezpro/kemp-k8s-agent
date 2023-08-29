package com.kemp.api

import com.kemp.client.KubeClient
import com.kemp.client.KubeManager
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.listResources() {

    get("/api/{cluster}/{resource}") {
        val queryNamespace = call.request.queryParameters["namespace"]
        val resources = call.parameters["cluster"]?.let {cluster ->
            call.parameters["resource"]?.let {resource ->
                KubeManager.getClient(cluster)?.listResourcesAsStringJsonList(resource, queryNamespace)
            }
        }
        call.respondNullable(resources)
    }

}