package com.kemp.plugins

import com.kemp.api.info
import com.kemp.api.managerActions
import com.kemp.api.resources
import com.kemp.model.GenericException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.kubernetes.client.openapi.ApiException

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is GenericException -> {
                    call.respondText(
                        text = "$cause ${cause.errorDetail}",
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.fromValue(cause.statusCode)
                    )
                }

                is ApiException -> {
                    call.respondText(
                        text = "${cause.message}",
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.fromValue(cause.code)
                    )
                }

                else -> {
                    call.respondText(text = "$cause")
                }
            }
        }
    }
    routing {
        info()
        route("/api/clusters") {
            managerActions()
            resources()
        }
    }
}
