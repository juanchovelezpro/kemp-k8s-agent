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

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            if (cause is GenericException) {
                call.respondText(
                    text = "$cause",
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.fromValue(cause.statusCode)
                )
            } else {
                call.respondText(text = "$cause")
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
