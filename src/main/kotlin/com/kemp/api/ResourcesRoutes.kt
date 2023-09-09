package com.kemp.api

import com.kemp.client.KubeManager
import com.kemp.model.KubeFormatType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.kubernetes.client.util.generic.options.PatchOptions

fun Route.resources() {

    route("/{cluster}") {
        route("/{resource}/{resourceName?}") {
//            get {
//                val resourceName = call.parameters["resourceName"]
//                val resources = call.parameters["cluster"]?.let { cluster ->
//                    call.parameters["resource"]?.let { resource ->
//                        if (resourceName.isNullOrEmpty()) {
//                            KubeManager.getClient(cluster).listObjects(resource, namespace).asStringJsonList()
//                        } else {
//                            KubeManager.getClient(cluster).getObject(resource, resourceName, namespace)
//                                ?.asStringJson()
//                        }
//                    }
//                }
//                call.respondText(resources ?: "", ContentType.Application.Json)
//            }
//            delete {
//                val namespace = call.request.queryParameters["namespace"]
//                val resource = call.parameters["cluster"]?.let { cluster ->
//                    call.parameters["resource"]?.let { resource ->
//                        call.parameters["resourceName"]?.let { resourceName ->
//                            KubeManager.getClient(cluster).deleteObject(resource, resourceName, namespace)
//                        }
//                    }
//                }
//                call.respondNullable(resource)
//            }
        }
        route("/resource") {
            post("/yaml") {
                handleApplyRequest(call, KubeFormatType.YAML)
            }
            post("/json") {
                handleApplyRequest(call, KubeFormatType.JSON)
            }
        }
//    }
    }
}

suspend fun handleApplyRequest(call: ApplicationCall, formatType: KubeFormatType) {
    val resource = call.receive<String>()
    val result = call.parameters["cluster"]?.let { cluster ->
        KubeManager.getClient(cluster).applyObject(resource, formatType, PatchOptions())
    } ?: false
    if (result) call.respondText(
        "The resource was applied",
        ContentType.Application.Json,
        status = HttpStatusCode.OK
    )
    else call.respondText(
        "The resource could not be applied",
        ContentType.Application.Json,
        status = HttpStatusCode.BadRequest
    )
}