package com.kemp.api

import com.kemp.client.KubeManager
import com.kemp.utils.asStringJson
import com.kemp.utils.asStringJsonList
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.resources() {

    route("/{cluster}") {
        route("/{resource}/{resourceName?}") {
            get {
                val namespace = call.request.queryParameters["namespace"]
                val resourceName = call.parameters["resourceName"]
                val resources = call.parameters["cluster"]?.let { cluster ->
                    call.parameters["resource"]?.let { resource ->
                        if (resourceName.isNullOrEmpty()) {
                            KubeManager.getClient(cluster).listResources(resource, namespace)?.asStringJsonList()
                        } else {
                            KubeManager.getClient(cluster).getResource(resource, resourceName, namespace)
                                ?.asStringJson()
                        }
                    }
                }
                call.respondText(resources ?: "", ContentType.Application.Json)
            }
            delete {
                val namespace = call.request.queryParameters["namespace"]
                val resource = call.parameters["cluster"]?.let { cluster ->
                    call.parameters["resource"]?.let { resource ->
                        call.parameters["resourceName"]?.let { resourceName ->
                            KubeManager.getClient(cluster).deleteResource(resource, resourceName, namespace)
                        }
                    }
                }
                call.respondNullable(resource)
            }
        }
    }
}