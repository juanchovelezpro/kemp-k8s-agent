package com.kemp.api

import com.kemp.client.KubeManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.resources() {

    route("/api/cluster/{clusterName}") {
        route("/{resource}/{resourceName?}") {
            get {
                val namespace = call.request.queryParameters["namespace"]
                val name = call.parameters["resourceName"]
                val resources = call.parameters["clusterName"]?.let { cluster ->
                    call.parameters["resource"]?.let { resource ->
                        if (name.isNullOrEmpty()) {
                            KubeManager.getClient(cluster).listResourcesAsStringJsonList(resource, namespace)
                        } else {
                            KubeManager.getClient(cluster).getResourceAsJson(name, resource, namespace)
                        }
                    }
                }
                call.respondText(resources ?: "", ContentType.Application.Json)
            }
            delete {
                val queryNamespace = call.request.queryParameters["namespace"]
                val resource = call.parameters["clusterName"]?.let { cluster ->
                    call.parameters["resource"]?.let { resource ->
                        call.parameters["resourceName"]?.let { resourceName ->
                            KubeManager.getClient(cluster).deleteResource(resourceName, resource, queryNamespace)
                        }
                    }
                }
                call.respondNullable(resource)
            }
        }
    }
}