package com.kemp.plugins

import com.kemp.api.info
import com.kemp.api.listResources
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        info()
        listResources()
    }
}
