package com.kemp.api

import com.kemp.client.KubeManager
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.resources() {

    get("/api/{cluster}/{resource}") {
        val namespace = call.request.queryParameters["namespace"]
        val name = call.request.queryParameters["name"]
        val resources = call.parameters["cluster"]?.let { cluster ->
            call.parameters["resource"]?.let { resource ->
                if (name.isNullOrEmpty()) {
                    KubeManager.getClient(cluster)?.listResourcesAsStringJsonList(resource, namespace)
                } else {
                    KubeManager.getClient(cluster)?.getResourceAsJson(name, resource, namespace)
                }
            }
        }
        call.respondNullable(resources)
    }

    delete("/api/{cluster}/{resource}/{resourceName}") {
        val queryNamespace = call.request.queryParameters["namespace"]
        val resource = call.parameters["cluster"]?.let { cluster ->
            call.parameters["resource"]?.let { resource ->
                call.parameters["resourceName"]?.let { resourceName ->
                    KubeManager.getClient(cluster)?.deleteResource(resource, resourceName, queryNamespace)
                }
            }
        }
        call.respondNullable(resource)
    }
}