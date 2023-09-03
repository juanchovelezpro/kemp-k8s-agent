package com.kemp

import com.kemp.plugins.configureHTTP
import com.kemp.plugins.configureMonitoring
import com.kemp.plugins.configureRouting
import com.kemp.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}
