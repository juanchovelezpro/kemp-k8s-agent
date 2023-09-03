package com.kemp.api

import com.kemp.client.KubeManager
import com.kemp.utils.asStringJson
import com.kemp.utils.asStringJsonList
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
                            KubeManager.getClient(cluster).listResources(resource, namespace)?.asStringJsonList()
                        } else {
                            KubeManager.getClient(cluster).getResource(name, resource, namespace)?.asStringJson()
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